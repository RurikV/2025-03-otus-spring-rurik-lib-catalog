# Spring Integration - Booking Service Payment Processing

## Overview

This project implements a booking service with payment processing through Spring Integration channels using Enterprise Integration Patterns (EIP). The application processes booking entities through integration flows with payment initiation, confirmation, and payout capabilities, simulating real-world booking service operations with external payment gateway integration.

## Domain Model

The application uses a complex booking service domain with the following entities:

- **Tenant**: Business or individual providing services with payment account information
- **Client**: Customer who books appointments with contact details
- **Deed**: Specific service offered by a Tenant with pricing and duration
- **Schedule**: Available time slots for bookings with availability status
- **Booking**: Central entity connecting Client, Tenant, Deed, and Schedule with payment tracking
- **Payment**: Financial transaction record with external payment gateway integration

## Spring Integration Implementation

### Integration Flows

1. **Booking Creation & Payment Initiation Flow** (`bookingCreationFlow`):
   - Input: `bookingCreationChannel`
   - Filter: Validates booking has required client and tenant IDs
   - Transform: Creates booking with PENDING_PAYMENT status
   - Filter: Ensures booking is in PENDING_PAYMENT status
   - Channel: Routes to `paymentInitiationChannel`
   - Transform: Initiates payment through external service simulation
   - Output: Booking with payment ID assigned

2. **Payment Confirmation Flow** (`paymentConfirmationFlow`):
   - Input: `paymentConfirmationChannel` (from webhook simulation)
   - Filter: Processes only COMPLETED payments
   - Transform: Validates payment confirmation data
   - Channel: Routes to `bookingUpdateChannel`
   - Transform: Confirms booking and updates status
   - Channel: Routes to `payoutChannel` for tenant payout

3. **Payout Processing Flow** (`payoutFlow`):
   - Input: `payoutChannel`
   - Filter: Processes only CONFIRMED bookings
   - Transform: Calculates payout amount and initiates transfer to tenant
   - Output: Payout completion confirmation

4. **Error Handling Flow** (`errorHandlingFlow`):
   - Handles processing errors and exceptions
   - Logs error details for debugging

### MessagingGateway

The `BookingGateway` interface provides entry points to the integration flows:

- `createBooking(Booking booking)`: Synchronous booking creation with payment initiation
- `createBookingAsync(Booking booking)`: Asynchronous booking processing
- `processPaymentConfirmation(Payment payment)`: Webhook-style payment confirmation
- `processPayout(Booking booking)`: Manual payout processing

### Services

- **BookingService**: Contains business logic for:
  - Booking creation and validation
  - Booking confirmation after payment
  - Schedule availability management
  - Booking status tracking

- **PaymentService**: Contains payment processing logic for:
  - Payment initiation with external service simulation
  - Payment confirmation processing
  - Payout calculation and processing
  - External payment gateway integration simulation

## Usage

### Shell Commands

The application provides Spring Shell commands for testing booking scenarios:

1. **Create Booking**: `create-booking --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1`
2. **Create Booking Async**: `create-booking-async --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1`
3. **Confirm Payment**: `confirm-payment --booking-id 1 --transaction-id TXN_123 --amount 100.00`
4. **Process Payout**: `process-payout --booking-id 1 --tenant-id 1`
5. **Test Complete Flow**: `test-booking-flow`
6. **Test Invalid Booking**: `test-invalid-booking`

### Example Usage

```bash
# Start the application
java -jar hw15-integration-0.0.1-SNAPSHOT.jar

# Create a booking
shell:>create-booking --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1

# Simulate payment confirmation (webhook)
shell:>confirm-payment --booking-id 1 --transaction-id TXN_12345 --amount 100.00

# Process payout to tenant
shell:>process-payout --booking-id 1 --tenant-id 1

# Test complete booking flow
shell:>test-booking-flow

# Test error scenarios
shell:>test-invalid-booking
```

## Configuration

### Application Properties

- H2 in-memory database for testing
- Spring Integration debugging enabled
- JPA with Hibernate DDL auto-creation
- Spring Shell interactive mode

### Integration Configuration

- Direct message channels for synchronous processing
- Automatic channel creation enabled
- Error handling with dedicated error channel
- Payment processing flow with external service simulation

## Testing

The project includes comprehensive integration tests:

- `BookingIntegrationTest`: Tests all booking integration flows
- Validates booking creation scenarios
- Tests payment confirmation processing
- Verifies payout flow execution
- Tests error handling and validation
- Verifies asynchronous processing capabilities

## Key Features

1. **Enterprise Integration Patterns**:
   - Message Channels for flow routing
   - Message Filters for conditional processing
   - Message Transformers for data processing
   - Message Gateways for external interfaces
   - Error handling with dedicated channels

2. **Complex Payment Processing**:
   - Multi-step payment flow (initiation → confirmation → payout)
   - External payment service integration simulation
   - Webhook-style payment confirmation handling
   - Tenant payout processing with fee calculation

3. **Asynchronous Processing**:
   - Non-blocking booking creation
   - Event-driven payment confirmation
   - Separate payout processing flow

4. **MessagingGateway Usage**:
   - Synchronous and asynchronous booking operations
   - Type-safe integration interfaces
   - Webhook simulation for payment confirmations

## Architecture

```
BookingCommands (Shell) 
    ↓
BookingGateway (MessagingGateway)
    ↓
Integration Flows (Booking → Payment → Payout)
    ↓
BookingService & PaymentService (Business Logic)
    ↓
External Payment Service Simulation
    ↓
Domain Entities (Tenant, Client, Deed, Schedule, Booking, Payment)
```

## Payment Processing Flow

```
1. Booking Creation
   ↓
2. Payment Initiation (External Service)
   ↓
3. Payment Confirmation (Webhook)
   ↓
4. Booking Confirmation
   ↓
5. Payout Processing (Tenant Transfer)
```
