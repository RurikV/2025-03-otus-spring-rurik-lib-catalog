package ru.otus.hw.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingService.class);

    public Booking createBooking(Booking booking) {
        LOG.info("Creating booking for client: {} with tenant: {}", booking.getClientId(), booking.getTenantId());
        
        validateBookingData(booking);
        
        // Set booking time and initial status
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PENDING_PAYMENT);
        
        // Simulate saving to database (would use repository in real implementation)
        booking.setId(System.currentTimeMillis()); // Mock ID generation
        
        LOG.info("Booking created with ID: {}, Status: {}", booking.getId(), booking.getStatus());
        
        return booking;
    }

    private void validateBookingData(Booking booking) {
        if (booking.getClientId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        if (booking.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (booking.getScheduleId() == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (booking.getDeedId() == null) {
            throw new IllegalArgumentException("Deed ID cannot be null");
        }
    }

    public Booking confirmBooking(Payment payment) {
        LOG.info("Confirming booking for payment: {}", payment.getId());
        
        // In real implementation, would fetch booking from DB by payment.getBookingId()
        // For now, we need to preserve the booking data from the flow context
        // This method will be called in the integration flow where the booking data is available
        
        // Create a booking object from payment (in real implementation, would fetch from DB)
        Booking booking = new Booking();
        booking.setId(payment.getBookingId());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(payment.getTransactionId());
        
        LOG.info("Booking confirmed: {}", booking.getId());
        
        return booking;
    }

    public Booking confirmBookingWithData(Payment payment, Booking originalBooking) {
        LOG.info("Confirming booking for payment: {}", payment.getId());
        
        // Preserve all original booking data and update status
        originalBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        originalBooking.setPaymentId(payment.getTransactionId());
        
        LOG.info("Booking confirmed: {}", originalBooking.getId());
        
        return originalBooking;
    }

    public void validateBookingAvailability(Booking booking) {
        LOG.info("Validating availability for schedule: {}", booking.getScheduleId());
        
        // In real implementation, would check if schedule slot is still available
        // For now, just simulate validation
        
        LOG.info("Schedule availability validated for booking: {}", booking.getId());
    }

    public void markScheduleAsBooked(Long scheduleId) {
        LOG.info("Marking schedule as booked: {}", scheduleId);
        
        // In real implementation, would update schedule.isBooked = true
        
        LOG.info("Schedule marked as booked: {}", scheduleId);
    }

    public void logBookingProcessing(Booking booking) {
        LOG.info("=== Booking Processing Log ===");
        LOG.info("Booking ID: {}", booking.getId());
        LOG.info("Client ID: {}", booking.getClientId());
        LOG.info("Tenant ID: {}", booking.getTenantId());
        LOG.info("Schedule ID: {}", booking.getScheduleId());
        LOG.info("Deed ID: {}", booking.getDeedId());
        LOG.info("Status: {}", booking.getStatus());
        LOG.info("Payment ID: {}", booking.getPaymentId());
        LOG.info("Booking Time: {}", booking.getBookingTime());
        LOG.info("==============================");
    }
}