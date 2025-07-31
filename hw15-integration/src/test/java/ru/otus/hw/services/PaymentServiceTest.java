package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void testInitiatePayment() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When
        Booking result = paymentService.initiatePayment(booking);

        // Then
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertNotNull(result.getPaymentId());
        assertTrue(result.getPaymentId().startsWith("PAY_"));
    }

    @Test
    void testProcessPaymentConfirmationSuccess() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_TEST_789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When
        Payment result = paymentService.processPaymentConfirmation(payment);

        // Then
        assertNotNull(result);
        assertEquals(payment.getId(), result.getId());
        assertEquals(payment.getTransactionId(), result.getTransactionId());
        assertEquals(Payment.PaymentStatus.COMPLETED, result.getStatus());
    }

    @Test
    void testProcessPaymentConfirmationWithIncompletePayment() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_TEST_789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.PROCESSING); // Not completed
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentConfirmation(payment);
        });
        assertEquals("Payment is not completed", exception.getMessage());
    }

    @Test
    void testProcessPaymentConfirmationWithNullTransactionId() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId(null);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentConfirmation(payment);
        });
        assertEquals("Transaction ID is required", exception.getMessage());
    }

    @Test
    void testProcessPaymentConfirmationWithEmptyTransactionId() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("   "); // Empty/whitespace transaction ID
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentConfirmation(payment);
        });
        assertEquals("Transaction ID is required", exception.getMessage());
    }

    @Test
    void testProcessPayout() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setTenantId(1L);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        // When
        Booking result = paymentService.processPayout(booking);

        // Then
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals(1L, result.getTenantId());
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void testHandlePaymentFailure() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_FAILED_789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setCreatedAt(LocalDateTime.now());

        // When
        paymentService.handlePaymentFailure(payment);

        // Then
        assertEquals(Payment.PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void testLogPaymentProcessing() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_LOG_TEST");
        payment.setAmount(new BigDecimal("150.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            paymentService.logPaymentProcessing(payment);
        });
    }

    @Test
    void testInitiatePaymentGeneratesUniquePaymentIds() {
        // Given
        Booking booking1 = new Booking();
        booking1.setId(123L);
        booking1.setClientId(1L);
        booking1.setTenantId(1L);

        Booking booking2 = new Booking();
        booking2.setId(456L);
        booking2.setClientId(2L);
        booking2.setTenantId(2L);

        // When
        Booking result1 = paymentService.initiatePayment(booking1);
        Booking result2 = paymentService.initiatePayment(booking2);

        // Then
        assertNotNull(result1.getPaymentId());
        assertNotNull(result2.getPaymentId());
        assertNotEquals(result1.getPaymentId(), result2.getPaymentId());
        assertTrue(result1.getPaymentId().startsWith("PAY_"));
        assertTrue(result2.getPaymentId().startsWith("PAY_"));
    }

    @Test
    void testProcessPaymentConfirmationWithFailedStatus() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_FAILED");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentConfirmation(payment);
        });
        assertEquals("Payment is not completed", exception.getMessage());
    }

    @Test
    void testProcessPaymentConfirmationWithRefundedStatus() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_REFUNDED");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setCreatedAt(LocalDateTime.now());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPaymentConfirmation(payment);
        });
        assertEquals("Payment is not completed", exception.getMessage());
    }

    @Test
    void testProcessPayoutWithDifferentTenantIds() {
        // Given
        Booking booking1 = new Booking();
        booking1.setId(123L);
        booking1.setTenantId(1L);
        booking1.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking booking2 = new Booking();
        booking2.setId(456L);
        booking2.setTenantId(2L);
        booking2.setStatus(Booking.BookingStatus.CONFIRMED);

        // When
        Booking result1 = paymentService.processPayout(booking1);
        Booking result2 = paymentService.processPayout(booking2);

        // Then
        assertEquals(1L, result1.getTenantId());
        assertEquals(2L, result2.getTenantId());
    }

    @Test
    void testInitiatePaymentPreservesBookingData() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setClientId(1L);
        booking.setTenantId(2L);
        booking.setScheduleId(3L);
        booking.setDeedId(4L);
        booking.setStatus(Booking.BookingStatus.PENDING_PAYMENT);

        // When
        Booking result = paymentService.initiatePayment(booking);

        // Then
        assertEquals(123L, result.getId());
        assertEquals(1L, result.getClientId());
        assertEquals(2L, result.getTenantId());
        assertEquals(3L, result.getScheduleId());
        assertEquals(4L, result.getDeedId());
        assertEquals(Booking.BookingStatus.PENDING_PAYMENT, result.getStatus());
        assertNotNull(result.getPaymentId());
    }
}