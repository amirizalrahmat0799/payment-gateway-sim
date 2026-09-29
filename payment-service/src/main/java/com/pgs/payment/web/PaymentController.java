package com.pgs.payment.web;

import com.pgs.payment.client.MerchantClient;
import com.pgs.payment.client.MerchantClient.MerchantContext;
import com.pgs.payment.service.PaymentService;
import com.pgs.payment.web.dto.CaptureRequest;
import com.pgs.payment.web.dto.CreatePaymentRequest;
import com.pgs.payment.web.dto.PageResponse;
import com.pgs.payment.web.dto.PaymentResponse;
import com.pgs.payment.web.dto.RefundRequest;
import com.pgs.payment.web.dto.RefundResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/payments")
public class PaymentController {

    static final String API_KEY = "X-Api-Key";
    static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final PaymentService paymentService;
    private final MerchantClient merchantClient;

    public PaymentController(PaymentService paymentService, MerchantClient merchantClient) {
        this.paymentService = paymentService;
        this.merchantClient = merchantClient;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader(API_KEY) String apiKey,
            @RequestHeader(IDEMPOTENCY_KEY) @Size(min = 8, max = 100) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        MerchantContext merchant = merchantClient.authenticate(apiKey);
        PaymentService.CreateResult result = paymentService.create(merchant, idempotencyKey, request);
        PaymentResponse body = PaymentResponse.from(result.payment());

        if (result.replayed()) {
            return ResponseEntity.ok().header("Idempotent-Replayed", "true").body(body);
        }
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@RequestHeader(API_KEY) String apiKey, @PathVariable UUID id) {
        return PaymentResponse.from(paymentService.get(merchantClient.authenticate(apiKey), id));
    }

    @GetMapping
    public PageResponse<PaymentResponse> list(@RequestHeader(API_KEY) String apiKey,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(paymentService.list(merchantClient.authenticate(apiKey), page, size),
                PaymentResponse::from);
    }

    @PostMapping("/{id}/capture")
    public PaymentResponse capture(@RequestHeader(API_KEY) String apiKey, @PathVariable UUID id,
                                   @Valid @RequestBody(required = false) CaptureRequest request) {
        Long amount = request == null ? null : request.amount();
        return PaymentResponse.from(paymentService.capture(merchantClient.authenticate(apiKey), id, amount));
    }

    @PostMapping("/{id}/void")
    public PaymentResponse voidPayment(@RequestHeader(API_KEY) String apiKey, @PathVariable UUID id) {
        return PaymentResponse.from(paymentService.voidPayment(merchantClient.authenticate(apiKey), id));
    }

    @PostMapping("/{id}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse refund(@RequestHeader(API_KEY) String apiKey, @PathVariable UUID id,
                                 @Valid @RequestBody RefundRequest request) {
        return RefundResponse.from(paymentService.refund(merchantClient.authenticate(apiKey), id, request.amount()).refund());
    }

    @GetMapping("/{id}/refunds")
    public List<RefundResponse> refunds(@RequestHeader(API_KEY) String apiKey, @PathVariable UUID id) {
        return paymentService.refundsOf(merchantClient.authenticate(apiKey), id).stream()
                .map(RefundResponse::from).toList();
    }
}
