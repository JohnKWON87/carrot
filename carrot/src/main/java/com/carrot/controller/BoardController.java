// src/main/java/com/carrot/controller/BoardController.java
package com.carrot.controller;

import com.carrot.entity.Board;
import com.carrot.entity.User;
import com.carrot.entity.AdminMenu;
import com.carrot.service.BoardService;
import com.carrot.service.AdminMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final AdminMenuService adminMenuService;

    // 게시판 목록
    @GetMapping("/{boardType}")
    public String boardList(@PathVariable String boardType,
                            @RequestParam(required = false) String search,
                            HttpSession session,
                            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        // 게시판 메뉴 정보 가져오기
        AdminMenu boardMenu = adminMenuService.getAllMenus().stream()
                .filter(m -> boardType.equals(m.getBoardType()))
                .findFirst()
                .orElse(null);

        if (boardMenu == null) {
            return "redirect:/";
        }

        List<Board> boards;
        if (search != null && !search.isEmpty()) {
            boards = boardService.searchBoard(boardType, search);
        } else {
            boards = boardService.getBoardList(boardType);
        }

        model.addAttribute("boards", boards);
        model.addAttribute("boardType", boardType);
        model.addAttribute("boardName", boardMenu.getMenuName());
        model.addAttribute("user", user);

        return "board/board-list";
    }

    // 게시글 상세
    @GetMapping("/{boardType}/{id}")
    public String boardDetail(@PathVariable String boardType,
                              @PathVariable Long id,
                              HttpSession session,
                              Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        Board board = boardService.getBoard(id); // 이미 조회수 증가 포함

        AdminMenu boardMenu = adminMenuService.getAllMenus().stream()
                .filter(m -> boardType.equals(m.getBoardType()))
                .findFirst()
                .orElse(null);

        model.addAttribute("board", board);
        model.addAttribute("boardType", boardType);
        model.addAttribute("boardName", boardMenu != null ? boardMenu.getMenuName() : "게시판");
        model.addAttribute("user", user);

        return "board/board-detail";
    }

    // 글쓰기 폼
    @GetMapping("/{boardType}/write")
    public String boardWriteForm(@PathVariable String boardType,
                                 HttpSession session,
                                 Model model) {

        User user = (User) session.getAttribute("user");
        System.out.println("=== 글쓰기 폼 접근 ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        System.out.println("Is Admin: " + (user != null ? user.isAdmin() : "null"));
        System.out.println("Board Type: " + boardType);

        if (user == null) {
            return "redirect:/";
        }

        // 공지사항은 관리자만 작성 가능
        if ("notice".equals(boardType) && !user.isAdmin()) {
            System.out.println("=== 권한 없음 - 리다이렉트 ===");
            return "redirect:/board/" + boardType + "?error=no_permission";
        }

        AdminMenu boardMenu = adminMenuService.getAllMenus().stream()
                .filter(m -> boardType.equals(m.getBoardType()))
                .findFirst()
                .orElse(null);

        model.addAttribute("boardType", boardType);
        model.addAttribute("boardName", boardMenu != null ? boardMenu.getMenuName() : "게시판");
        model.addAttribute("user", user);

        return "board/board-write";
    }

    // 글쓰기 처리
    @PostMapping("/{boardType}/write")
    public String boardWrite(@PathVariable String boardType,
                             @RequestParam String title,
                             @RequestParam String content,
                             HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        // 공지사항은 관리자만 작성 가능
        if ("notice".equals(boardType) && !user.isAdmin()) {
            return "redirect:/board/" + boardType;
        }

        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setBoardType(boardType);
        board.setAuthor(user);

        boardService.saveBoard(board);

        return "redirect:/board/" + boardType;
    }

    // 게시글 삭제
    @PostMapping("/{boardType}/{id}/delete")
    public String boardDelete(@PathVariable String boardType,
                              @PathVariable Long id,
                              HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        Board board = boardService.getBoard(id);

        // 작성자 본인이거나 관리자만 삭제 가능
        if (board.getAuthor().getId().equals(user.getId()) || user.isAdmin()) {
            boardService.deleteBoard(id);
        }

        return "redirect:/board/" + boardType;
    }
}