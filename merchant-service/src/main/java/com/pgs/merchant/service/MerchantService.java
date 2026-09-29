package com.pgs.merchant.service;

import com.pgs.merchant.domain.Merchant;
import com.pgs.merchant.domain.MerchantRepository;
import com.pgs.merchant.domain.MerchantStatus;
import com.pgs.merchant.web.dto.CreateMerchantRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchants;

    public MerchantService(MerchantRepository merchants) {
        this.merchants = merchants;
    }

    /** Result of onboarding: the plaintext API key is returned exactly once and never stored. */
    public record Onboarded(Merchant merchant, String apiKey) {
    }

    @Transactional
    public Onboarded onboard(CreateMerchantRequest request) {
        if (merchants.existsByEmailIgnoreCase(request.email())) {
            throw new MerchantAlreadyExistsException(request.email());
        }
        String apiKey = ApiKeys.generate();
        Merchant merchant = new Merchant(
                request.name(),
                request.email().toLowerCase(),
                request.feeBps(),
                ApiKeys.displayPrefix(apiKey),
                ApiKeys.hash(apiKey));
        return new Onboarded(merchants.save(merchant), apiKey);
    }

    @Transactional(readOnly = true)
    public Merchant get(UUID id) {
        return merchants.findById(id).orElseThrow(() -> new MerchantNotFoundException(id));
    }

    @Transactional
    public Merchant changeStatus(UUID id, MerchantStatus status) {
        Merchant merchant = get(id);
        merchant.changeStatus(status);
        return merchant;
    }

    @Transactional
    public Onboarded rotateApiKey(UUID id) {
        Merchant merchant = get(id);
        String apiKey = ApiKeys.generate();
        merchant.rotateApiKey(ApiKeys.displayPrefix(apiKey), ApiKeys.hash(apiKey));
        return new Onboarded(merchant, apiKey);
    }

    /** Used by other services to resolve the merchant behind an API key. */
    @Transactional(readOnly = true)
    public Merchant authenticate(String apiKey) {
        return merchants.findByApiKeyHash(ApiKeys.hash(apiKey))
                .filter(Merchant::isActive)
                .orElseThrow(InvalidApiKeyException::new);
    }
}
