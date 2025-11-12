package com.carrot.controller;

import com.carrot.entity.Agreement;
import com.carrot.entity.DealMethod;
import com.carrot.repository.AgreementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.JdbcTemplate;          // [NEW]
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 폼 렌더링 + 저장 + 목록/단건 조회 + 삭제/갱신 API
 * - 일부 프로젝트에서 엔티티 필드가 다를 수 있어 BeanWrapper로 동적 접근
 * - [NEW] 삭제 후 ID를 1..N으로 재배열하고, 다음 삽입을 N+1에서 시작하도록 시퀀스 리셋(H2)
 */
@Controller
public class AgreementController {

    private final AgreementRepository repo;
    private final ObjectMapper om = new ObjectMapper();
    private final JdbcTemplate jdbc;              // [NEW]

    // [CHANGED] JdbcTemplate 주입 (ID 리넘버링용)
    public AgreementController(AgreementRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;                         // [NEW]
    }

    /* ----------------------------
     * 판매자 폼
     * ---------------------------- */
    @GetMapping({"/seller", "/seller/first"})
    public String sellerForm(Model model) {
        model.addAttribute("agreement", new Agreement());
        model.addAttribute("dealMethods", com.carrot.entity.DealMethod.values());
        return "SellerFirstPage";
    }

    @PostMapping("/seller/submit")
    public String submit(@Valid @ModelAttribute("agreement") Agreement agreement,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("dealMethods", DealMethod.values());
            return "SellerFirstPage";
        }

        // 엔티티 구조가 달라도 안전: seller 속성이 있을 때만 기본값 세팅
        BeanWrapper bw = new BeanWrapperImpl(agreement);
        if (bw.isWritableProperty("seller") && bw.getPropertyValue("seller") == null) {
            try { bw.setPropertyValue("seller", ""); } catch (Exception ignored) {}
        }

        Agreement saved = repo.save(agreement);
        // 저장 후 구매자 첫 화면으로
        return "redirect:/BuyerFirstPage";
    }

    /* ----------------------------
     * 구매자 첫 화면
     * ---------------------------- */
    @GetMapping({"/BuyerFirstPage", "/buyerfirstpage"})
    public String buyerFirstPage(Model model) throws JsonProcessingException {
        List<Agreement> all = repo.findAll();

        List<Map<String, Object>> buyerItems = all.stream()
                .map(this::toItemJson)
                .toList();

        // "판매자로부터 요청" 섹션은 제거한 상태이므로 buyerItems만 사용
        model.addAttribute("buyerItemsJson", om.writeValueAsString(buyerItems));
        return "BuyerFirstPage";
    }

    /* ----------------------------
     * 단건 보기 (선택)
     * ---------------------------- */
    //@GetMapping("/buyer/{id}")
    //public String view(@PathVariable("id") Long id, Model model) {
    //    Agreement a = repo.findById(id).orElse(null);
    //    model.addAttribute("agreement", a);
    //    return "BuyerFirstPage2";
    //}
    // ① 정확히 이 URL로 들어왔을 때 SellerSecondPage.html 반환

    // @GetMapping({"/BuyerFirstPage", "/buyerfirstpage"})

    @GetMapping("/SellerSecondPage")
    public String sellerSecondPage(@RequestParam(name="id", required = false) Long id, Model model) {
        // id가 제공되면 DB에서 불러와 모델에 넣어 템플릿에서 쓰게 함(선택)
        if (id != null) {
            Agreement ag = repo.findById(id).orElse(null);
            model.addAttribute("agreement", ag);
        }
        return "SellerSecondPage"; // 파일명과 일치 (확장자 제외, 대소문자 주의)
    }

    // ② 만약 기존 흐름이 /seller/second/{id}라면, /SellerSecondPage로도 접근 가능하게 리다이렉트
    //@GetMapping("/seller/second/{id}")
    //public String sellerSecondPageCompat(@PathVariable Long id) {
    //    return "redirect:/SellerSecondPage?id=" + id;
    //}


    /* ----------------------------
     * 삭제 API
     * ---------------------------- */
    @DeleteMapping("/api/agreements/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();

        repo.deleteById(id);

        // [NEW] 삭제 후 ID를 1..N으로 재배열 + 다음 삽입이 N+1이 되도록 시퀀스 리셋
        renumberAgreementIds();   // [NEW]

        return ResponseEntity.ok(Map.of("ok", true, "id", id));
    }

    /* ----------------------------
     * 갱신 API (PATCH)
     *  - 프론트 모달의 "판매자에게 보내기"가 호출
     *  - method / price / direct(date,time,place) / shipping(wishDate,address,recipient) 반영
     * ---------------------------- */
    @PatchMapping("/api/agreements/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAgreement(@PathVariable("id") Long id,
                                             @RequestBody Map<String, Object> body) {
        Agreement a = repo.findById(id).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();

        BeanWrapper bw = new BeanWrapperImpl(a);

        // method
        String method = asString(body.get("method"));
        if (method != null && bw.isWritableProperty("method")) {
            Class<?> t = bw.getPropertyType("method");
            try {
                if (t != null && t.isEnum() && t.getSimpleName().equals("DealMethod")) {
                    DealMethod dm = "shipping".equalsIgnoreCase(method) ? DealMethod.SHIPPING : DealMethod.DIRECT;
                    bw.setPropertyValue("method", dm);
                } else {
                    bw.setPropertyValue("method", method.toUpperCase());
                }
            } catch (Exception ignored) {}
        }

        // price
        if (body.containsKey("price") && bw.isWritableProperty("price")) {
            Integer pv = asInt(body.get("price"));
            try { bw.setPropertyValue("price", pv); } catch (Exception ignored) {}
        }

        // direct { date, time, place }
        Map<String, Object> direct = asMap(body.get("direct"));
        LocalDate meetDate = parseLocalDate(asString(get(direct, "date")));
        LocalTime meetTime = parseLocalTime(asString(get(direct, "time")));
        String place = asString(get(direct, "place"));

        if (meetDate != null) { safeSet(bw, List.of("directDate", "meetDate", "directInfo.date"), meetDate); }
        else { safeSet(bw, List.of("directDate", "meetDate", "directInfo.date"), null); }

        if (meetTime != null && meetDate != null) {
            safeSet(bw, List.of("directTime"), LocalDateTime.of(meetDate, meetTime));
        } else {
            safeSet(bw, List.of("meetTime", "directInfo.time"), meetTime);
            if (meetTime == null || meetDate == null) {
                safeSet(bw, List.of("directTime"), null);
            }
        }

        if (place != null || existsAny(bw, List.of("place", "meetPlace", "directInfo.place"))) {
            safeSet(bw, List.of("place", "meetPlace", "directInfo.place"), place);
        }

        // shipping { wishDate, address, recipient }
        Map<String, Object> ship = asMap(body.get("shipping"));
        LocalDate wish = parseLocalDate(asString(get(ship, "wishDate")));
        String address = asString(get(ship, "address"));
        String recipient = asString(get(ship, "recipient"));

        if (wish != null) {
            if (!safeSet(bw, List.of("wishDate", "shippingWishDate", "recvStart", "shippingInfo.wishDate"), wish)) {
                if (bw.isWritableProperty("note")) {
                    String tag = "(희망수령일: " + wish + ")";
                    String note = asString(bw.getPropertyValue("note"));
                    if (note == null || !note.contains("(희망수령일:")) {
                        try { bw.setPropertyValue("note", (note == null ? tag : (note + " " + tag))); } catch (Exception ignored) {}
                    }
                }
            }
        } else {
            // 명시적으로 NULL 처리 요청 시
            safeSet(bw, List.of("wishDate", "shippingWishDate", "recvStart", "shippingInfo.wishDate"), null);
        }

        if (address != null || existsAny(bw, List.of("address", "shippingAddress", "shippingInfo.address"))) {
            safeSet(bw, List.of("address", "shippingAddress", "shippingInfo.address"), address);
        }

        if (recipient != null || existsAny(bw, List.of("recipient", "shippingRecipient", "shippingInfo.recipient"))) {
            safeSet(bw, List.of("recipient", "shippingRecipient", "shippingInfo.recipient"), recipient);
        }

        Agreement saved = repo.save(a);
        Map<String, Object> resp = Map.of(
                "ok", true,
                "id", saved.getId(),
                "method", String.valueOf(readFirst(bw, List.of("method"))),
                "price", readFirst(bw, List.of("price"))
        );
        return ResponseEntity.ok(resp);
    }

    /* ---------------------------------------------------------
     * 프런트에서 쓰기 좋은 JSON 항목 (id/title/offer 형태) - 동적 읽기
     * --------------------------------------------------------- */
    private Map<String, Object> toItemJson(Agreement a) {
        BeanWrapper bw = new BeanWrapperImpl(a);

        Map<String, Object> item = new LinkedHashMap<>();
        Object id = readFirst(bw, List.of("id"));
        item.put("id", id);

        String title = asString(readFirst(bw, List.of("title")));
        if (title == null) {
            String buyer = asString(readFirst(bw, List.of("buyer")));
            title = (buyer != null && !buyer.isBlank()) ? ("구매자 " + buyer + "의 합의") : null;
        }
        item.put("title", title);

        Map<String, Object> offer = new LinkedHashMap<>();
        offer.put("id", id);

        String methodStr = String.valueOf(readFirst(bw, List.of("method")));
        if (methodStr == null) methodStr = "direct";
        methodStr = methodStr.equalsIgnoreCase("SHIPPING") ? "shipping"
                : methodStr.equalsIgnoreCase("DIRECT") ? "direct"
                : methodStr.toLowerCase();
        offer.put("method", methodStr);

        Map<String, Object> payload = new LinkedHashMap<>();

        // direct
        Map<String, Object> direct = new LinkedHashMap<>();
        Object dDate = readFirst(bw, List.of("directDate", "meetDate", "directInfo.date"));
        Object dTime = readFirst(bw, List.of("directTime", "meetTime", "directInfo.time"));
        Object dPlace = readFirst(bw, List.of("place", "meetPlace", "directInfo.place"));
        if (dDate != null) direct.put("date", toDateString(dDate));
        if (dTime != null) direct.put("time", toTimeString(dTime));
        if (dPlace != null) direct.put("place", String.valueOf(dPlace));
        payload.put("direct", direct);

        // shipping
        Map<String, Object> shipping = new LinkedHashMap<>();
        Object addr = readFirst(bw, List.of("address", "shippingAddress", "shippingInfo.address"));
        if (addr != null) shipping.put("address", String.valueOf(addr));
        Object wish = readFirst(bw, List.of("wishDate", "shippingWishDate", "recvStart", "shippingInfo.wishDate"));
        if (wish != null) shipping.put("wishDate", toDateString(wish));
        Object rcpt = readFirst(bw, List.of("recipient", "shippingRecipient", "shippingInfo.recipient"));
        if (rcpt != null) shipping.put("recipient", String.valueOf(rcpt));
        payload.put("shipping", shipping);

        // price
        Object price = readFirst(bw, List.of("price"));
        if (price != null) {
            try { payload.put("price", Integer.valueOf(String.valueOf(price))); } catch (Exception ignored) {}
        }

        offer.put("payload", payload);
        offer.put("summary", null);

        item.put("offers", List.of(offer));
        return item;
    }

    /* ----------------- [NEW] ID 리넘버링 유틸 -----------------
     * H2 전용. agreements 테이블의 PK(id)를 1..N으로 재배열하고
     * 다음 insert가 N+1에서 시작하도록 IDENTITY 시퀀스를 리셋한다.
     * (FK 없다는 가정. 멀티스테이트먼트 수행을 위해 JdbcTemplate 사용)
     * ----------------------------------------------------------- */
    private void renumberAgreementIds() {
        // 테이블이 비면 시퀀스만 1로 리셋
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM agreements", Long.class);
        if (total == null || total == 0L) {
            jdbc.execute("ALTER TABLE agreements ALTER COLUMN id RESTART WITH 1");
            return;
        }

        // 1) 임시 테이블에 (old_id, new_id = row_number) 계산
        jdbc.execute("DROP TABLE IF EXISTS TMP_AGR");
        jdbc.execute("""
            CREATE TEMP TABLE TMP_AGR AS
            SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) AS new_id
            FROM agreements
        """);

        // 2) PK 업데이트 (주의: FK 없다는 가정)
        //   - H2에서는 서브쿼리로 매핑 가능
        jdbc.execute("""
            UPDATE agreements a
               SET id = (SELECT t.new_id FROM TMP_AGR t WHERE t.old_id = a.id)
        """);

        // 3) 임시 테이블 삭제
        jdbc.execute("DROP TABLE IF EXISTS TMP_AGR");

        // 4) 다음 삽입값을 N+1로 맞춤
        Integer maxId = jdbc.queryForObject("SELECT MAX(id) FROM agreements", Integer.class);
        int next = (maxId == null ? 1 : maxId + 1);
        jdbc.execute("ALTER TABLE agreements ALTER COLUMN id RESTART WITH " + next);
    }

    /* ----------------- 유틸 ----------------- */

    private static String asString(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? (Map<String, Object>) m : Collections.emptyMap();
    }

    private static Object get(Map<String, Object> m, String k) { return m == null ? null : m.get(k); }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static LocalDate parseLocalDate(String s) {
        try { return (s == null) ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private static LocalTime parseLocalTime(String s) {
        try { return (s == null) ? null : LocalTime.parse(s); } catch (Exception e) { return null; }
    }

    private static boolean existsAny(BeanWrapper bw, List<String> names) {
        for (String n : names) if (bw.isWritableProperty(n)) return true;
        return false;
    }

    /** names 중 첫 번째로 세팅 가능한 속성에 값을 넣는다. 성공 시 true */
    private static boolean safeSet(BeanWrapper bw, List<String> names, Object value) {
        for (String n : names) {
            if (bw.isWritableProperty(n)) {
                try { bw.setPropertyValue(n, value); return true; } catch (Exception ignored) {}
            }
        }
        return false;
    }

    /** names 중 첫 번째로 읽을 수 있는 속성 값을 반환 */
    private static Object readFirst(BeanWrapper bw, List<String> names) {
        for (String n : names) {
            if (bw.isReadableProperty(n)) {
                try { return bw.getPropertyValue(n); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static String toDateString(Object v) {
        if (v instanceof LocalDate ld) return ld.toString();
        return String.valueOf(v);
    }

    private static String toTimeString(Object v) {
        if (v instanceof LocalDateTime ldt) return ldt.toLocalTime().toString();
        if (v instanceof LocalTime lt) return lt.toString();
        return String.valueOf(v);
    }
}
