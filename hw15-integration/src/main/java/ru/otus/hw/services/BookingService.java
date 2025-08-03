package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.time.LocalDateTime;

@Service
public class BookingService {

    public Booking createBooking(Booking booking) {
        System.out.println("Creating booking for client: " + booking.getClientId() + 
                          " with tenant: " + booking.getTenantId());
        
        validateBookingData(booking);
        
        // Set booking time and initial status
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PENDING_PAYMENT);
        
        // Simulate saving to database (would use repository in real implementation)
        booking.setId(System.currentTimeMillis()); // Mock ID generation
        
        System.out.println("Booking created with ID: " + booking.getId() + 
                          ", Status: " + booking.getStatus());
        
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
        System.out.println("Confirming booking for payment: " + payment.getId());
        
        // In real implementation, would fetch booking from DB by payment.getBookingId()
        // For now, we need to preserve the booking data from the flow context
        // This method will be called in the integration flow where the booking data is available
        
        // Create a booking object from payment (in real implementation, would fetch from DB)
        Booking booking = new Booking();
        booking.setId(payment.getBookingId());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(payment.getTransactionId());
        
        System.out.println("Booking confirmed: " + booking.getId());
        
        return booking;
    }

    public Booking confirmBookingWithData(Payment payment, Booking originalBooking) {
        System.out.println("Confirming booking for payment: " + payment.getId());
        
        // Preserve all original booking data and update status
        originalBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        originalBooking.setPaymentId(payment.getTransactionId());
        
        System.out.println("Booking confirmed: " + originalBooking.getId());
        
        return originalBooking;
    }

    public void validateBookingAvailability(Booking booking) {
        System.out.println("Validating availability for schedule: " + booking.getScheduleId());
        
        // In real implementation, would check if schedule slot is still available
        // For now, just simulate validation
        
        System.out.println("Schedule availability validated for booking: " + booking.getId());
    }

    public void markScheduleAsBooked(Long scheduleId) {
        System.out.println("Marking schedule as booked: " + scheduleId);
        
        // In real implementation, would update schedule.isBooked = true
        
        System.out.println("Schedule marked as booked: " + scheduleId);
    }

    public void logBookingProcessing(Booking booking) {
        System.out.println("=== Booking Processing Log ===");
        System.out.println("Booking ID: " + booking.getId());
        System.out.println("Client ID: " + booking.getClientId());
        System.out.println("Tenant ID: " + booking.getTenantId());
        System.out.println("Schedule ID: " + booking.getScheduleId());
        System.out.println("Deed ID: " + booking.getDeedId());
        System.out.println("Status: " + booking.getStatus());
        System.out.println("Payment ID: " + booking.getPaymentId());
        System.out.println("Booking Time: " + booking.getBookingTime());
        System.out.println("==============================");
    }
}