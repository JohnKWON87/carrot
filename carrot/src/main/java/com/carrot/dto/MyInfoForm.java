package com.carrot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MyInfoForm {
    private String username; // read-only 표시용

    @NotBlank @Email
    private String email;

    private String phone;
    private String address;

    // 비밀번호 변경(선택)
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;

    private String returnTo;
}
