package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.models.Booking;
import ru.otus.hw.models.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    public Booking initiatePayment(Booking booking) {
        System.out.println("Initiating payment for booking: " + booking.getId());
        
        String paymentId = generatePaymentId();
        booking.setPaymentId(paymentId);
        
        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(calculatePaymentAmount(booking));
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setTransactionId(paymentId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setId(System.currentTimeMillis());
        
        System.out.println("Payment initiated with ID: " + paymentId + 
                          ", Amount: " + payment.getAmount());
        
        simulateExternalPaymentServiceCall(payment);
        
        return booking;
    }

    public Payment processPaymentConfirmation(Payment payment) {
        System.out.println("Processing payment confirmation for payment: " + payment.getId());
        
        // Validate payment confirmation
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment is not completed");
        }
        
        if (payment.getTransactionId() == null || payment.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        
        System.out.println("Payment confirmation processed: " + payment.getTransactionId());
        
        return payment;
    }

    public Booking processPayout(Booking booking) {
        System.out.println("Processing payout for booking: " + booking.getId());
        
        // Calculate payout amount (could be different from original payment due to fees)
        BigDecimal payoutAmount = calculatePayoutAmount(booking);
        
        // Get tenant payment account (in real implementation, would fetch from database)
        String tenantPaymentAccount = getTenantPaymentAccount(booking.getTenantId());
        
        // Simulate external payout service call
        String payoutTransactionId = generatePayoutTransactionId();
        
        System.out.println("Payout processed: " + payoutTransactionId + 
                          ", Amount: " + payoutAmount + 
                          ", To Account: " + tenantPaymentAccount);
        
        // In real implementation, would call external payment service for payout
        simulateExternalPayoutServiceCall(payoutTransactionId, payoutAmount, tenantPaymentAccount);
        
        return booking;
    }

    public void handlePaymentFailure(Payment payment) {
        System.out.println("Handling payment failure for payment: " + payment.getId());
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        
        // In real implementation, would:
        // 1. Update payment status in database
        // 2. Release the booked schedule slot
        // 3. Notify client about payment failure
        // 4. Send message to error handling channel
        
        System.out.println("Payment failure handled for: " + payment.getTransactionId());
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
        System.out.println("Calling external payment service...");
        System.out.println("Payment gateway response: Payment link generated for " + payment.getTransactionId());
        
        // In real implementation, would make HTTP call to payment gateway
        // and return payment link to client
    }

    private void simulateExternalPayoutServiceCall(String transactionId, BigDecimal amount, String account) {
        System.out.println("Calling external payout service...");
        System.out.println("Payout service response: Transfer initiated - " + 
                          transactionId + " for " + amount + " to " + account);
        
        // In real implementation, would make HTTP call to payment service
        // to transfer funds to tenant account
    }

    public void logPaymentProcessing(Payment payment) {
        System.out.println("=== Payment Processing Log ===");
        System.out.println("Payment ID: " + payment.getId());
        System.out.println("Booking ID: " + payment.getBookingId());
        System.out.println("Amount: " + payment.getAmount());
        System.out.println("Status: " + payment.getStatus());
        System.out.println("Transaction ID: " + payment.getTransactionId());
        System.out.println("Created At: " + payment.getCreatedAt());
        System.out.println("==============================");
    }
}