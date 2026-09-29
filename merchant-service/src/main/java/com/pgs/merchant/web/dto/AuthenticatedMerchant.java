package com.pgs.merchant.web.dto;

import java.util.UUID;

/** Internal response returned to payment-service when it validates an API key. */
public record AuthenticatedMerchant(UUID merchantId, String name, int feeBps) {
}
