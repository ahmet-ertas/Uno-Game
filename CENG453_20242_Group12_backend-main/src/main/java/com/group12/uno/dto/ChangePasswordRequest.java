package com.group12.uno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for changing password")
public class ChangePasswordRequest {
    @Schema(description = "Current password", required = true)
    private String currentPassword;

    @Schema(description = "New password", required = true)
    private String newPassword;
} 