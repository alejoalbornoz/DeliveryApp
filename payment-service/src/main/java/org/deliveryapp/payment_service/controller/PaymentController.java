package org.deliveryapp.payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.payment_service.dto.request.PaymentRequestDTO;
import org.deliveryapp.payment_service.dto.response.PaymentResponseDTO;
import org.deliveryapp.payment_service.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "MercadoPago payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "Create a MercadoPago preference and get the payment URL")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook called by MercadoPago with payment result")
    public ResponseEntity<Void> webhook(
            @RequestParam(value = "id") String paymentId,
            @RequestParam(value = "topic", defaultValue = "payment") String topic) {
        log.info("Webhook received: paymentId={}, topic={}", paymentId, topic);
        paymentService.processWebhook(paymentId, topic);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment status by order ID")
    public ResponseEntity<PaymentResponseDTO> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

    // Back URL endpoints — MercadoPago redirects here after payment
    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("Payment approved successfully");
    }

    @GetMapping("/failure")
    public ResponseEntity<String> failure() {
        return ResponseEntity.ok("Payment was rejected");
    }

    @GetMapping("/pending")
    public ResponseEntity<String> pending() {
        return ResponseEntity.ok("Payment is pending");
    }
}