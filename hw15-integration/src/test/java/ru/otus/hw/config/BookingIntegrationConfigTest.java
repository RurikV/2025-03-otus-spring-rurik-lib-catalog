package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.shell.interactive.enabled=false",
    "logging.level.org.springframework.integration=INFO"
})
class BookingIntegrationConfigTest {

    @Autowired
    private BookingIntegrationConfig config;

    @Autowired
    private MessageChannel paymentInitiationChannel;

    @Autowired
    private MessageChannel paymentConfirmationChannel;

    @Autowired
    private MessageChannel payoutChannel;

    @Autowired
    private MessageChannel bookingCreationChannel;

    @Autowired
    private MessageChannel bookingUpdateChannel;

    @Autowired
    private MessageChannel errorChannel;

    @Autowired
    private IntegrationFlow bookingCreationFlow;

    @Autowired
    private IntegrationFlow paymentConfirmationFlow;

    @Autowired
    private IntegrationFlow payoutFlow;

    @Autowired
    private IntegrationFlow errorHandlingFlow;

    @Test
    void testConfigurationIsNotNull() {
        assertNotNull(config);
    }

    @Test
    void testPaymentInitiationChannelExists() {
        assertNotNull(paymentInitiationChannel);
    }

    @Test
    void testPaymentConfirmationChannelExists() {
        assertNotNull(paymentConfirmationChannel);
    }

    @Test
    void testPayoutChannelExists() {
        assertNotNull(payoutChannel);
    }

    @Test
    void testBookingCreationChannelExists() {
        assertNotNull(bookingCreationChannel);
    }

    @Test
    void testBookingUpdateChannelExists() {
        assertNotNull(bookingUpdateChannel);
    }

    @Test
    void testErrorChannelExists() {
        assertNotNull(errorChannel);
    }

    @Test
    void testBookingCreationFlowExists() {
        assertNotNull(bookingCreationFlow);
    }

    @Test
    void testPaymentConfirmationFlowExists() {
        assertNotNull(paymentConfirmationFlow);
    }

    @Test
    void testPayoutFlowExists() {
        assertNotNull(payoutFlow);
    }

    @Test
    void testErrorHandlingFlowExists() {
        assertNotNull(errorHandlingFlow);
    }

    @Test
    void testAllChannelsAreConfigured() {
        // Verify all required channels are properly configured
        assertNotNull(paymentInitiationChannel, "Payment initiation channel should be configured");
        assertNotNull(paymentConfirmationChannel, "Payment confirmation channel should be configured");
        assertNotNull(payoutChannel, "Payout channel should be configured");
        assertNotNull(bookingCreationChannel, "Booking creation channel should be configured");
        assertNotNull(bookingUpdateChannel, "Booking update channel should be configured");
        assertNotNull(errorChannel, "Error channel should be configured");
    }

    @Test
    void testAllFlowsAreConfigured() {
        // Verify all required integration flows are properly configured
        assertNotNull(bookingCreationFlow, "Booking creation flow should be configured");
        assertNotNull(paymentConfirmationFlow, "Payment confirmation flow should be configured");
        assertNotNull(payoutFlow, "Payout flow should be configured");
        assertNotNull(errorHandlingFlow, "Error handling flow should be configured");
    }
}