package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();
    }

    @Test
    void testCreateBookingSuccess() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When
        Booking result = bookingService.createBooking(booking);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(Booking.BookingStatus.PENDING_PAYMENT, result.getStatus());
        assertNotNull(result.getBookingTime());
        assertEquals(1L, result.getClientId());
        assertEquals(1L, result.getTenantId());
        assertEquals(1L, result.getScheduleId());
        assertEquals(1L, result.getDeedId());
    }

    @Test
    void testCreateBookingWithNullClientId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(null);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(booking);
        });
        assertEquals("Client ID cannot be null", exception.getMessage());
    }

    @Test
    void testCreateBookingWithNullTenantId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(null);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(booking);
        });
        assertEquals("Tenant ID cannot be null", exception.getMessage());
    }

    @Test
    void testCreateBookingWithNullScheduleId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(null);
        booking.setDeedId(1L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(booking);
        });
        assertEquals("Schedule ID cannot be null", exception.getMessage());
    }

    @Test
    void testCreateBookingWithNullDeedId() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(booking);
        });
        assertEquals("Deed ID cannot be null", exception.getMessage());
    }

    @Test
    void testConfirmBooking() {
        // Given
        Payment payment = new Payment();
        payment.setId(123L);
        payment.setBookingId(456L);
        payment.setTransactionId("TXN_TEST_789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);

        // When
        Booking result = bookingService.confirmBooking(payment);

        // Then
        assertNotNull(result);
        assertEquals(456L, result.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
        assertEquals("TXN_TEST_789", result.getPaymentId());
    }

    @Test
    void testValidateBookingAvailability() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setScheduleId(456L);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            bookingService.validateBookingAvailability(booking);
        });
    }

    @Test
    void testMarkScheduleAsBooked() {
        // Given
        Long scheduleId = 123L;

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            bookingService.markScheduleAsBooked(scheduleId);
        });
    }

    @Test
    void testLogBookingProcessing() {
        // Given
        Booking booking = new Booking();
        booking.setId(123L);
        booking.setClientId(1L);
        booking.setTenantId(2L);
        booking.setScheduleId(3L);
        booking.setDeedId(4L);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId("PAY_123");
        booking.setBookingTime(LocalDateTime.now());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            bookingService.logBookingProcessing(booking);
        });
    }

    @Test
    void testCreateBookingGeneratesUniqueIds() {
        // Given
        Booking booking1 = new Booking();
        booking1.setClientId(1L);
        booking1.setTenantId(1L);
        booking1.setScheduleId(1L);
        booking1.setDeedId(1L);

        Booking booking2 = new Booking();
        booking2.setClientId(2L);
        booking2.setTenantId(2L);
        booking2.setScheduleId(2L);
        booking2.setDeedId(2L);

        // When
        Booking result1 = bookingService.createBooking(booking1);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Booking result2 = bookingService.createBooking(booking2);

        // Then
        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void testCreateBookingSetsBookingTimeToNow() {
        // Given
        Booking booking = new Booking();
        booking.setClientId(1L);
        booking.setTenantId(1L);
        booking.setScheduleId(1L);
        booking.setDeedId(1L);
        
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        Booking result = bookingService.createBooking(booking);

        // Then
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
        assertNotNull(result.getBookingTime());
        assertTrue(result.getBookingTime().isAfter(beforeCreation));
        assertTrue(result.getBookingTime().isBefore(afterCreation));
    }
}