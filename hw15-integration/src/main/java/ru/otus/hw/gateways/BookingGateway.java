package ru.otus.hw.gateways;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

@MessagingGateway
public interface BookingGateway {

    @Gateway(requestChannel = "completeBookingChannel", replyTimeout = 10000)
    Booking processCompleteBooking(Booking booking);

    @Gateway(requestChannel = "bookingCreationChannel", replyTimeout = 5000)
    Booking createBooking(Booking booking);

    @Gateway(requestChannel = "paymentConfirmationChannel")
    void processPaymentConfirmation(Payment payment);

    @Gateway(requestChannel = "bookingCreationChannel")
    void createBookingAsync(Booking booking);

    @Gateway(requestChannel = "payoutChannel")
    void processPayout(Booking booking);
}