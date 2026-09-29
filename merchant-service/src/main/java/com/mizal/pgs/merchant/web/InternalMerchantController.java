package com.mizal.pgs.merchant.web;

import com.mizal.pgs.merchant.domain.Merchant;
import com.mizal.pgs.merchant.service.MerchantService;
import com.mizal.pgs.merchant.web.dto.AuthenticatedMerchant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints. In a real deployment these would only be reachable
 * inside the cluster network (or protected with mTLS).
 */
@RestController
@RequestMapping("/internal/merchants")
public class InternalMerchantController {

    private final MerchantService merchantService;

    public InternalMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/authenticate")
    public AuthenticatedMerchant authenticate(@RequestHeader("X-Api-Key") String apiKey) {
        Merchant merchant = merchantService.authenticate(apiKey);
        return new AuthenticatedMerchant(merchant.getId(), merchant.getName(), merchant.getFeeBps());
    }
}
