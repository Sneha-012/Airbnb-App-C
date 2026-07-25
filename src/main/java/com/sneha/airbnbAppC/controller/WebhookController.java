package com.sneha.airbnbAppC.controller;


import com.sneha.airbnbAppC.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
// Stripe calls THIS endpoint automatically whenever something happens to a payment
// (success, failure, refund, etc.) — our app never calls this itself, only Stripe does.
public class WebhookController {

    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(
            @RequestBody String payload,                       // raw event data Stripe sends - must stay unparsed for signature check
            @RequestHeader("Stripe-Signature") String sigHeader // proves the payload wasn't tampered with in transit
    ) {
        try {
            // Verify this request genuinely came from Stripe (not forged) using our secret.
            // Throws if the signature doesn't match.
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            // Signature verified - safe to act on this event now.
            // e.g. mark the matching booking as CONFIRMED if this is a "checkout completed" event
            bookingService.capturePayment(event);

            // Tell Stripe "received successfully" - without a 2xx response,
            // Stripe will assume something failed and retry sending this event later
            return ResponseEntity.noContent().build();

        } catch (SignatureVerificationException e) {
            // Request couldn't be verified as genuinely from Stripe - reject it.
            // Using 400 (client error) instead of a generic 500, since retrying
            // won't help - the signature will never become valid.
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
