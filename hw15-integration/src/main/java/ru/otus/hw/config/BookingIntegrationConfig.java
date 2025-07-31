package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;
import ru.otus.hw.services.BookingService;
import ru.otus.hw.services.PaymentService;

@Configuration
public class BookingIntegrationConfig {

    // Payment Processing Flow Channels
    @Bean
    public MessageChannel paymentInitiationChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel paymentConfirmationChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel payoutChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel bookingCreationChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel bookingUpdateChannel() {
        return MessageChannels.direct().getObject();
    }

    // Booking Creation & Payment Initiation Flow
    @Bean
    public IntegrationFlow bookingCreationFlow(BookingService bookingService, PaymentService paymentService) {
        return IntegrationFlow.from("bookingCreationChannel")
                .filter(Booking.class, booking -> booking.getClientId() != null && booking.getTenantId() != null,
                        spec -> spec.discardChannel("discardedBookingChannel"))
                .transform(Booking.class, bookingService::createBooking)
                .filter(Booking.class, booking -> booking.getStatus() == Booking.BookingStatus.PENDING_PAYMENT)
                .channel("paymentInitiationChannel")
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Booking created, initiating payment for booking: " + booking.getId());
                    return booking;
                })
                .transform(Booking.class, paymentService::initiatePayment)
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Payment initiated for booking: " + booking.getId() + 
                                     ", payment ID: " + booking.getPaymentId());
                    return booking;
                })
                .get();
    }

    // Channel for discarded bookings
    @Bean
    public MessageChannel discardedBookingChannel() {
        return MessageChannels.direct().getObject();
    }

    // Flow to handle discarded bookings and return null
    @Bean
    public IntegrationFlow discardedBookingFlow() {
        return IntegrationFlow.from("discardedBookingChannel")
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Booking discarded due to missing required fields");
                    return null;
                })
                .get();
    }

    // Payment Confirmation Flow (from webhook)
    @Bean
    public IntegrationFlow paymentConfirmationFlow(BookingService bookingService, PaymentService paymentService) {
        return IntegrationFlow.from("paymentConfirmationChannel")
                .filter(Payment.class, payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED)
                .transform(Payment.class, paymentService::processPaymentConfirmation)
                .channel("bookingUpdateChannel")
                .transform(Payment.class, bookingService::confirmBooking)
                .channel("payoutChannel")
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Booking confirmed: " + booking.getId() + ", initiating payout");
                    return booking;
                })
                .get();
    }

    // Payout Processing Flow
    @Bean
    public IntegrationFlow payoutFlow(PaymentService paymentService) {
        return IntegrationFlow.from("payoutChannel")
                .filter(Booking.class, booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED)
                .transform(Booking.class, paymentService::processPayout)
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Payout processed for booking: " + booking.getId() + 
                                     " to tenant: " + booking.getTenantId());
                    return booking;
                })
                .get();
    }

    // Error handling channels
    @Bean
    public MessageChannel errorChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle(message -> {
                    System.err.println("Error processing message: " + message.getPayload());
                    if (message.getPayload() instanceof Exception) {
                        ((Exception) message.getPayload()).printStackTrace();
                    }
                })
                .get();
    }
}