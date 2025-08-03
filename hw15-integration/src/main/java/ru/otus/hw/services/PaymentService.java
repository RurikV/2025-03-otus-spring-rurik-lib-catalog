package ru.otus.hw.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentService.class);

    public Booking initiatePayment(Booking booking) {
        LOG.info("Initiating payment for booking: {}", booking.getId());
        
        String paymentId = generatePaymentId();
        booking.setPaymentId(paymentId);
        
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(calculatePaymentAmount(booking));
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setTransactionId(paymentId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setId(System.currentTimeMillis());
        
        LOG.info("Payment initiated with ID: {}, Amount: {}", paymentId, payment.getAmount());
        
        simulateExternalPaymentServiceCall(payment);
        
        return booking;
    }

    public Payment processPaymentConfirmation(Payment payment) {
        LOG.info("Processing payment confirmation for payment: {}", payment.getId());
        
        // Validate payment confirmation
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment is not completed");
        }
        
        if (payment.getTransactionId() == null || payment.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        
        LOG.info("Payment confirmation processed: {}", payment.getTransactionId());
        
        return payment;
    }

    public Booking processPayout(Booking booking) {
        LOG.info("Processing payout for booking: {}", booking.getId());
        
        // Calculate payout amount (could be different from original payment due to fees)
        BigDecimal payoutAmount = calculatePayoutAmount(booking);
        
        // Get tenant payment account (in real implementation, would fetch from database)
        String tenantPaymentAccount = getTenantPaymentAccount(booking.getTenantId());
        
        // Simulate external payout service call
        String payoutTransactionId = generatePayoutTransactionId();
        
        LOG.info("Payout processed: {}, Amount: {}, To Account: {}", 
                payoutTransactionId, payoutAmount, tenantPaymentAccount);
        
        // In real implementation, would call external payment service for payout
        simulateExternalPayoutServiceCall(payoutTransactionId, payoutAmount, tenantPaymentAccount);
        
        return booking;
    }

    public void handlePaymentFailure(Payment payment) {
        LOG.info("Handling payment failure for payment: {}", payment.getId());
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        
        // In real implementation, would:
        // 1. Update payment status in database
        // 2. Release the booked schedule slot
        // 3. Notify client about payment failure
        // 4. Send message to error handling channel
        
        LOG.info("Payment failure handled for: {}", payment.getTransactionId());
    }

    private String generatePaymentId() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePayoutTransactionId() {
        return "PAYOUT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculatePaymentAmount(Booking booking) {
        // In real implementation, would fetch deed price from database
        // For now, simulate with a fixed amount
        return new BigDecimal("100.00");
    }

    private BigDecimal calculatePayoutAmount(Booking booking) {
        // Calculate payout amount (original amount minus service fee)
        BigDecimal originalAmount = calculatePaymentAmount(booking);
        BigDecimal serviceFee = originalAmount.multiply(new BigDecimal("0.05")); // 5% fee
        return originalAmount.subtract(serviceFee);
    }

    private String getTenantPaymentAccount(Long tenantId) {
        // In real implementation, would fetch from database
        return "TENANT_ACCOUNT_" + tenantId;
    }

    private void simulateExternalPaymentServiceCall(Payment payment) {
        LOG.info("Calling external payment service...");
        LOG.info("Payment gateway response: Payment link generated for {}", payment.getTransactionId());
        
        // In real implementation, would make HTTP call to payment gateway
        // and return payment link to client
    }

    private void simulateExternalPayoutServiceCall(String transactionId, BigDecimal amount, String account) {
        LOG.info("Calling external payout service...");
        LOG.info("Payout service response: Transfer initiated - {} for {} to {}", transactionId, amount, account);
        
        // In real implementation, would make HTTP call to payment service
        // to transfer funds to tenant account
    }

    public void logPaymentProcessing(Payment payment) {
        LOG.info("=== Payment Processing Log ===");
        LOG.info("Payment ID: {}", payment.getId());
        LOG.info("Booking ID: {}", payment.getBookingId());
        LOG.info("Amount: {}", payment.getAmount());
        LOG.info("Status: {}", payment.getStatus());
        LOG.info("Transaction ID: {}", payment.getTransactionId());
        LOG.info("Created At: {}", payment.getCreatedAt());
        LOG.info("==============================");
    }
}