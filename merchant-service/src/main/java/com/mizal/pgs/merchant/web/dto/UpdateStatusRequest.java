package com.mizal.pgs.merchant.web.dto;

import com.mizal.pgs.merchant.domain.MerchantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull MerchantStatus status) {
}
