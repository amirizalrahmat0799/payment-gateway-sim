package com.pgs.merchant.web.dto;

import com.pgs.merchant.domain.MerchantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull MerchantStatus status) {
}
