package ru.otus.hw.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BookingPaymentOrchestrator {

    private static final Logger LOG = LoggerFactory.getLogger(BookingPaymentOrchestrator.class);

    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingPaymentOrchestrator(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    public Booking processPaymentAndConfirmBooking(Booking booking) {
        // Create payment confirmation
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setTransactionId(booking.getPaymentId());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setId(System.currentTimeMillis());

        LOG.info("Simulating payment completion for booking: {} with transaction: {}", booking.getId(), payment.getTransactionId());

        // Process payment confirmation
        paymentService.processPaymentConfirmation(payment);

        // Confirm booking while preserving original data
        return bookingService.confirmBookingWithData(payment, booking);
    }
}
