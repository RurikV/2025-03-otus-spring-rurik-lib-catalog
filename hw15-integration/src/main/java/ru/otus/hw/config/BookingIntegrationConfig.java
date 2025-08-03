package ru.otus.hw.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;
import ru.otus.hw.services.BookingService;
import ru.otus.hw.services.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class BookingIntegrationConfig {

    private static final Logger log = LoggerFactory.getLogger(BookingIntegrationConfig.class);

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

    @Bean
    public MessageChannel completeBookingChannel() {
        return MessageChannels.direct().getObject();
    }

    // Complete Booking Workflow - Single Unified Flow
    @Bean
    public IntegrationFlow completeBookingWorkflow(BookingService bookingService, PaymentService paymentService) {
        return IntegrationFlow.from("completeBookingChannel")
                .filter(Booking.class, booking -> booking.getClientId() != null && booking.getTenantId() != null 
                        && booking.getScheduleId() != null && booking.getDeedId() != null,
                        spec -> spec.discardChannel("discardedBookingChannel"))
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("=== Starting Complete Booking Workflow ===");
                    bookingService.logBookingProcessing(booking);
                    return booking;
                })
                // Step 1: Validate schedule availability
                .handle(Booking.class, (booking, headers) -> {
                    bookingService.validateBookingAvailability(booking);
                    return booking;
                })
                // Step 2: Create booking
                .transform(Booking.class, bookingService::createBooking)
                .filter(Booking.class, booking -> booking.getStatus() == Booking.BookingStatus.PENDING_PAYMENT)
                // Step 3: Initiate payment
                .transform(Booking.class, paymentService::initiatePayment)
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("Payment initiated for booking: " + booking.getId() + 
                                     ", payment ID: " + booking.getPaymentId());
                    return booking;
                })
                // Step 4: Simulate payment completion and confirm booking (preserving original data)
                .handle(Booking.class, (booking, headers) -> {
                    // Create payment confirmation
                    Payment payment = new Payment();
                    payment.setBookingId(booking.getId());
                    payment.setTransactionId(booking.getPaymentId());
                    payment.setAmount(new BigDecimal("100.00"));
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setId(System.currentTimeMillis());
                    
                    // Process payment confirmation
                    paymentService.processPaymentConfirmation(payment);
                    
                    // Confirm booking while preserving original data
                    return bookingService.confirmBookingWithData(payment, booking);
                })
                // Step 6: Mark schedule as booked
                .handle(Booking.class, (booking, headers) -> {
                    bookingService.markScheduleAsBooked(booking.getScheduleId());
                    return booking;
                })
                // Step 7: Process payout
                .transform(Booking.class, paymentService::processPayout)
                .handle(Booking.class, (booking, headers) -> {
                    System.out.println("=== Complete Booking Workflow Finished Successfully ===");
                    System.out.println("Final booking status: " + booking.getStatus());
                    System.out.println("Schedule " + booking.getScheduleId() + " is now occupied by client " + booking.getClientId());
                    return booking;
                })
                .get();
    }

    // Booking Creation & Payment Initiation Flow (kept for backward compatibility)
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
                    log.error("Error processing message: {}", message.getPayload());
                    if (message.getPayload() instanceof Exception) {
                        log.error("Exception details:", (Exception) message.getPayload());
                    }
                })
                .get();
    }
}