package com.matsuzaka.foodtiger.dto;

import com.matsuzaka.foodtiger.dao.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "用戶名不能為空")
    @Size(min = 3, max = 50, message = "用戶名長度必須在 3 到 50 個字符之間")
    private String username;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼長度至少為 6 個字符")
    private String password;

    @Email(message = "電子郵件格式不正確")
    private String email;

    @Size(min = 10, max = 20, message = "電話號碼長度必須在 10 到 20 個字符之間")
    private String phone;

    @NotNull(message = "角色不能為空")
    private Role role;
}
