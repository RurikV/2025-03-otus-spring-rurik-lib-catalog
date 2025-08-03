package ru.otus.hw.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.gateways.BookingGateway;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ShellComponent
public class BookingCommands {

    private static final Logger LOG = LoggerFactory.getLogger(BookingCommands.class);

    private final BookingGateway bookingGateway;

    @Autowired
    public BookingCommands(BookingGateway bookingGateway) {
        this.bookingGateway = bookingGateway;
    }

    @ShellMethod(value = "Create a booking through integration flow", key = {"create-booking", "cb"})
    public String createBooking(@ShellOption("--client-id") Long clientId, 
                               @ShellOption("--tenant-id") Long tenantId,
                               @ShellOption("--schedule-id") Long scheduleId,
                               @ShellOption("--deed-id") Long deedId) {
        try {
            // Create booking
            Booking booking = new Booking();
            booking.setClientId(clientId);
            booking.setTenantId(tenantId);
            booking.setScheduleId(scheduleId);
            booking.setDeedId(deedId);

            LOG.info("Starting booking creation through integration flow...");
            Booking createdBooking = bookingGateway.createBooking(booking);

            return "Booking created successfully: ID=" + createdBooking.getId() + 
                   ", Status=" + createdBooking.getStatus() + 
                   ", Payment ID=" + createdBooking.getPaymentId();
        } catch (Exception e) {
            return "Error creating booking: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Create a booking asynchronously", key = {"create-booking-async", "cba"})
    public String createBookingAsync(@ShellOption("--client-id") Long clientId, 
                                    @ShellOption("--tenant-id") Long tenantId,
                                    @ShellOption("--schedule-id") Long scheduleId,
                                    @ShellOption("--deed-id") Long deedId) {
        try {
            // Create booking
            Booking booking = new Booking();
            booking.setClientId(clientId);
            booking.setTenantId(tenantId);
            booking.setScheduleId(scheduleId);
            booking.setDeedId(deedId);

            LOG.info("Starting asynchronous booking creation...");
            bookingGateway.createBookingAsync(booking);

            return "Booking sent for asynchronous processing: Client=" + clientId + ", Tenant=" + tenantId;
        } catch (Exception e) {
            return "Error creating booking asynchronously: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Simulate payment confirmation webhook", key = {"confirm-payment", "cp"})
    public String confirmPayment(@ShellOption("--booking-id") Long bookingId, 
                                @ShellOption("--transaction-id") String transactionId,
                                @ShellOption(value = "--amount", defaultValue = "100.00") String amount) {
        try {
            // Create payment confirmation
            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setTransactionId(transactionId);
            payment.setAmount(new BigDecimal(amount));
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setCreatedAt(LocalDateTime.now());

            LOG.info("Simulating payment confirmation webhook...");
            bookingGateway.processPaymentConfirmation(payment);

            return "Payment confirmation processed: " + transactionId + " for booking " + bookingId;
        } catch (Exception e) {
            return "Error processing payment confirmation: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Process payout for booking", key = {"process-payout", "pp"})
    public String processPayout(@ShellOption("--booking-id") Long bookingId, 
                               @ShellOption("--tenant-id") Long tenantId) {
        try {
            // Create booking for payout
            Booking booking = new Booking();
            booking.setId(bookingId);
            booking.setTenantId(tenantId);
            booking.setStatus(Booking.BookingStatus.CONFIRMED);

            LOG.info("Processing payout through integration flow...");
            bookingGateway.processPayout(booking);

            return "Payout processed for booking: " + bookingId + " to tenant: " + tenantId;
        } catch (Exception e) {
            return "Error processing payout: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Test complete booking flow", key = {"test-booking-flow", "tbf"})
    public String testBookingFlow() {
        try {
            LOG.info("Testing complete booking flow...");
            
            // Step 1: Create booking
            String result1 = createBooking(1L, 1L, 1L, 1L);
            LOG.info("Step 1 - {}", result1);
            
            // Step 2: Simulate payment confirmation
            String result2 = confirmPayment(1L, "TXN_12345", "100.00");
            LOG.info("Step 2 - {}", result2);
            
            // Step 3: Process payout
            String result3 = processPayout(1L, 1L);
            LOG.info("Step 3 - {}", result3);

            return "Complete booking flow test completed successfully!";
        } catch (Exception e) {
            return "Error in booking flow test: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Process complete booking workflow in one call", key = {"complete-booking", "pcb"})
    public String processCompleteBooking(@ShellOption("--client-id") Long clientId, 
                                        @ShellOption("--tenant-id") Long tenantId,
                                        @ShellOption("--schedule-id") Long scheduleId,
                                        @ShellOption("--deed-id") Long deedId) {
        try {
            // Create booking
            Booking booking = new Booking();
            booking.setClientId(clientId);
            booking.setTenantId(tenantId);
            booking.setScheduleId(scheduleId);
            booking.setDeedId(deedId);

            LOG.info("Starting complete booking workflow through unified integration flow...");
            Booking result = bookingGateway.processCompleteBooking(booking);

            if (result == null) {
                return "Booking was rejected due to missing required fields";
            }

            return "Complete booking workflow finished successfully: ID=" + result.getId() + 
                   ", Status=" + result.getStatus() + 
                   ", Payment ID=" + result.getPaymentId() +
                   ", Schedule " + result.getScheduleId() + " is now occupied by client " + result.getClientId();
        } catch (Exception e) {
            return "Error in complete booking workflow: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Test invalid booking creation", key = {"test-invalid-booking", "tib"})
    public String testInvalidBooking() {
        try {
            // Create invalid booking (missing required fields)
            Booking invalidBooking = new Booking();
            invalidBooking.setClientId(null); // Missing client ID

            LOG.info("Testing invalid booking creation...");
            bookingGateway.createBookingAsync(invalidBooking);

            return "Invalid booking sent for processing (should be rejected)";
        } catch (Exception e) {
            return "Error with invalid booking test: " + e.getMessage();
        }
    }
}