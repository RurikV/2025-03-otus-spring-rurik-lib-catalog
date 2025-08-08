package ru.otus.hw.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;
import ru.otus.hw.services.BookingService;
import ru.otus.hw.services.PaymentService;

@Configuration
public class BookingIntegrationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BookingIntegrationConfig.class);

    // Payment Processing Flow Channels
    @Bean
    public MessageChannel paymentInitiationChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel paymentConfirmationChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel payoutChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel bookingCreationChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel bookingUpdateChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel completeBookingChannel() {
        return new DirectChannel();
    }

    // Complete Booking Workflow - Single Unified Flow
    @Bean
    public IntegrationFlow completeBookingWorkflow(BookingService bookingService, PaymentService paymentService, ru.otus.hw.services.BookingPaymentOrchestrator orchestrator) {
        return IntegrationFlow.from("completeBookingChannel")
                .filter(Booking.class, this::isValidBooking, spec -> spec.discardChannel("discardedBookingChannel"))
                .handle(Booking.class, (booking, headers) -> logWorkflowStart(booking, bookingService))
                .handle(Booking.class, (booking, headers) -> validateAvailability(booking, bookingService))
                .transform(Booking.class, bookingService::createBooking)
                .filter(Booking.class, booking -> booking.getStatus() == Booking.BookingStatus.PENDING_PAYMENT)
                .transform(Booking.class, paymentService::initiatePayment)
                .transform(Booking.class, orchestrator::processPaymentAndConfirmBooking)
                .handle(Booking.class, (booking, headers) -> markScheduleBooked(booking, bookingService))
                .transform(Booking.class, paymentService::processPayout)
                .handle(Booking.class, this::logWorkflowCompletion)
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
                .transform(Booking.class, paymentService::initiatePayment)
                .get();
    }

    // Channel for discarded bookings
    @Bean
    public MessageChannel discardedBookingChannel() {
        return new DirectChannel();
    }

    // Flow to handle discarded bookings and return null
    @Bean
    public IntegrationFlow discardedBookingFlow() {
        return IntegrationFlow.from("discardedBookingChannel")
                .handle(Booking.class, (booking, headers) -> {
                    LOG.info("Booking discarded due to missing required fields");
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
                    LOG.info("Booking confirmed: {}, initiating payout", booking.getId());
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
                    LOG.info("Payout processed for booking: {} to tenant: {}", booking.getId(), booking.getTenantId());
                    return booking;
                })
                .get();
    }

    // Error handling channels
    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle(message -> {
                    LOG.error("Error processing message: {}", message.getPayload());
                    if (message.getPayload() instanceof Exception) {
                        LOG.error("Exception details:", (Exception) message.getPayload());
                    }
                })
                .get();
    }

    private boolean isValidBooking(Booking booking) {
        return booking.getClientId() != null && booking.getTenantId() != null 
                && booking.getScheduleId() != null && booking.getDeedId() != null;
    }

    private Booking logWorkflowStart(Booking booking, BookingService bookingService) {
        LOG.info("=== Starting Complete Booking Workflow ===");
        bookingService.logBookingProcessing(booking);
        return booking;
    }

    private Booking validateAvailability(Booking booking, BookingService bookingService) {
        bookingService.validateBookingAvailability(booking);
        return booking;
    }


    private Booking markScheduleBooked(Booking booking, BookingService bookingService) {
        bookingService.markScheduleAsBooked(booking.getScheduleId());
        return booking;
    }

    private Booking logWorkflowCompletion(Booking booking, Object headers) {
        LOG.info("=== Complete Booking Workflow Finished Successfully ===");
        LOG.info("Final booking status: {}", booking.getStatus());
        LOG.info("Schedule {} is now occupied by client {}", booking.getScheduleId(), booking.getClientId());
        return booking;
    }

}