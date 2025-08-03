package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.gateways.BookingGateway;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.shell.interactive.enabled=false",
    "logging.level.org.springframework.integration=INFO"
})
class BookingIntegrationTest {

    @Autowired
    private BookingGateway bookingGateway;

    @Test
    void testBookingCreationFlow() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When
        Booking createdBooking = bookingGateway.createBooking(booking);

        // Then
        assertNotNull(createdBooking);
        assertNotNull(createdBooking.getId());
        assertEquals(Booking.BookingStatus.PENDING_PAYMENT, createdBooking.getStatus());
        assertNotNull(createdBooking.getPaymentId());
        assertEquals(1L, createdBooking.getClientId());
        assertEquals(1L, createdBooking.getTenantId());
        assertNotNull(createdBooking.getBookingTime());
    }

    @Test
    void testPaymentConfirmationFlow() {
        // Given
        Payment payment = new Payment();
        payment.setBookingId(123L);
        payment.setTransactionId("TXN_TEST_123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            bookingGateway.processPaymentConfirmation(payment);
        });
    }

    @Test
    void testPayoutFlow() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setTenantId(1L);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            bookingGateway.processPayout(booking);
        });
    }

    @Test
    void testBookingCreationWithMissingClientId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(null); // Missing client ID
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When & Then - Should be filtered out by integration flow, not throw exception
        assertDoesNotThrow(() -> {
            Booking result = bookingGateway.createBooking(booking);
            // Result should be null as message is filtered out
            assertNull(result);
        });
    }

    @Test
    void testBookingCreationWithMissingTenantId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(null); // Missing tenant ID
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When & Then - Should be filtered out by integration flow, not throw exception
        assertDoesNotThrow(() -> {
            Booking result = bookingGateway.createBooking(booking);
            // Result should be null as message is filtered out
            assertNull(result);
        });
    }

    @Test
    void testAsyncBookingCreation() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When & Then - Should not throw exception for async processing
        assertDoesNotThrow(() -> {
            bookingGateway.createBookingAsync(booking);
        });
    }

    @Test
    void testPaymentConfirmationWithIncompletePayment() {
        // Given
        Payment payment = new Payment();
        payment.setBookingId(123L);
        payment.setTransactionId("TXN_TEST_123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.PROCESSING); // Not completed
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then - Should be filtered out by the flow
        assertDoesNotThrow(() -> {
            bookingGateway.processPaymentConfirmation(payment);
        });
    }

    @Test
    void testUnifiedCompleteBookingWorkflow() {
        // Given - Create a booking request with all required fields
        Booking booking = new Booking();
        booking.setClientId(3L);
        booking.setTenantId(3L);
        booking.setScheduleId(3L);
        booking.setDeedId(3L);

        // When - Process complete booking workflow in one call
        Booking result = bookingGateway.processCompleteBooking(booking);

        // Then - Verify complete workflow was successful
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getPaymentId());
        assertEquals(3L, result.getClientId());
        assertEquals(3L, result.getTenantId());
        assertEquals(3L, result.getScheduleId());
        assertEquals(3L, result.getDeedId());
        assertNotNull(result.getBookingTime());
    }

    @Test
    void testUnifiedWorkflowWithMissingFields() {
        // Given - Create a booking request with missing required fields
        Booking booking = new Booking();
        booking.setClientId(4L);
        booking.setTenantId(4L);
        // Missing scheduleId and deedId

        // When & Then - Should be filtered out and return null
        assertDoesNotThrow(() -> {
            Booking result = bookingGateway.processCompleteBooking(booking);
            assertNull(result);
        });
    }

    @Test
    void testCompleteBookingWorkflow() {
        // Given - Create a booking
        Booking booking = new Booking();
        booking.setClientId(2L);
        booking.setTenantId(2L);
        booking.setScheduleId(2L);
        booking.setDeedId(2L);

        // When - Create booking
        Booking createdBooking = bookingGateway.createBooking(booking);

        // Then - Verify booking creation
        assertNotNull(createdBooking);
        assertEquals(Booking.BookingStatus.PENDING_PAYMENT, createdBooking.getStatus());

        // Given - Payment confirmation
        Payment payment = new Payment();
        payment.setBookingId(createdBooking.getId());
        payment.setTransactionId("TXN_WORKFLOW_TEST");
        payment.setAmount(new BigDecimal("150.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then - Process payment confirmation
        assertDoesNotThrow(() -> {
            bookingGateway.processPaymentConfirmation(payment);
        });

        // Given - Payout processing
        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(createdBooking.getId());
        confirmedBooking.setTenantId(2L);
        confirmedBooking.setStatus(Booking.BookingStatus.CONFIRMED);

        // When & Then - Process payout
        assertDoesNotThrow(() -> {
            bookingGateway.processPayout(confirmedBooking);
        });
    }

    @Test
    void testBookingCreationWithAllMissingFields() {
        // Given
        Booking booking = new Booking();
        // All fields are null

        // When & Then - Should be filtered out by integration flow, not throw exception
        assertDoesNotThrow(() -> {
            Booking result = bookingGateway.createBooking(booking);
            // Result should be null as message is filtered out
            assertNull(result);
        });
    }

    @Test
    void testPaymentConfirmationWithNullTransactionId() {
        // Given
        Payment payment = new Payment();
        payment.setBookingId(123L);
        payment.setTransactionId(null); // Null transaction ID
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then - Should throw exception due to service validation
        assertThrows(Exception.class, () -> {
            bookingGateway.processPaymentConfirmation(payment);
        });
    }
}