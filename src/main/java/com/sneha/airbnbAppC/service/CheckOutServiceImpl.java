package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.entity.Booking;
import com.sneha.airbnbAppC.entity.User;
import com.sneha.airbnbAppC.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckOutServiceImpl implements CheckOutService{

    private final BookingRepository bookingRepository;


    @Override
    public String getCheckOutSession(Booking booking, String successUrl, String failureUrl) {

        log.info("Starting checkout session creation for booking id: {}", booking.getId());

        // Get the currently logged-in user
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {

            // Step 1: Register this user as a "Customer" inside Stripe's own system
            // (not our database) - so Stripe can attach receipts/refunds to a real name & email
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();

            Customer customer = Customer.create(customerCreateParams);
            log.info("Stripe customer created successfully with id: {}", customer.getId());

            // Price info for this booking: how much to charge, in what currency,
            // plus the product details (name/description) shown on Stripe's payment page.
            // Note: ProductData must be built INSIDE PriceData - Stripe doesn't allow it
            // as a separate field on LineItem directly.

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData
                    .builder()
                    .setCurrency("inr")
                    .setUnitAmount(booking.getAmount()
                            .multiply(BigDecimal.valueOf(100))// Stripe requires lowercase ISO currency codes
                            // Stripe expects the smallest currency unit (paise for INR),
                            // so ₹4500 must be sent as 450000
                            .longValue())
                    .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    // What the user actually sees on the payment page -
                                    // e.g. "Property Beachfront Villa : Room Deluxe"
                                    .setName("Property " + booking.getProperty().getName()
                                            + " : " + "Room " + booking.getRoom().getType())
                                    .setDescription("Booking Id: " + booking.getId())
                                    .build()
                    )
                    .build();


            // Build the full checkout session - this is the actual request sent to Stripe
            // describing the payment page it should generate.
            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)     // one-time payment, not a subscription
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())  // link this session to the customer created above
                    .setSuccessUrl(successUrl)     // where to send user if payment succeeds
                    .setCancelUrl(failureUrl)      // where to send user if they cancel
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)      // one unit of this booking (not multiple)
                            .setPriceData(priceData)    // attach the price + product info built above
                            .build())
                    .build();



            Session session =Session.create(sessionParams);
            log.info("Stripe session created successfully with id: {}", session.getId());

            // Save Stripe's session id on our own booking, so we can match it up
            // later when Stripe notifies us (via webhook) that payment succeeded
            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);


            return session.getUrl();

        }catch (StripeException e){
            log.error("Stripe checkout session creation failed for booking id: {}. Reason: {}",
                    booking.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
