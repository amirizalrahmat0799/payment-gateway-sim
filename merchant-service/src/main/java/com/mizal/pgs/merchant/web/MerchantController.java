package com.mizal.pgs.merchant.web;

import com.mizal.pgs.merchant.service.MerchantService;
import com.mizal.pgs.merchant.web.dto.CreateMerchantRequest;
import com.mizal.pgs.merchant.web.dto.MerchantResponse;
import com.mizal.pgs.merchant.web.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<MerchantResponse> onboard(@Valid @RequestBody CreateMerchantRequest request) {
        MerchantService.Onboarded result = merchantService.onboard(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(result.merchant().getId()).toUri();
        return ResponseEntity.created(location)
                .body(MerchantResponse.from(result.merchant(), result.apiKey()));
    }

    /** The merchant that owns the given API key. Used by the merchant dashboard after login. */
    @GetMapping("/me")
    public MerchantResponse me(@RequestHeader("X-Api-Key") String apiKey) {
        return MerchantResponse.from(merchantService.authenticate(apiKey));
    }

    @GetMapping("/{id}")
    public MerchantResponse get(@PathVariable UUID id) {
        return MerchantResponse.from(merchantService.get(id));
    }

    @PatchMapping("/{id}/status")
    public MerchantResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return MerchantResponse.from(merchantService.changeStatus(id, request.status()));
    }

    @PostMapping("/{id}/api-key/rotate")
    public MerchantResponse rotateApiKey(@PathVariable UUID id) {
        MerchantService.Onboarded result = merchantService.rotateApiKey(id);
        return MerchantResponse.from(result.merchant(), result.apiKey());
    }
}
