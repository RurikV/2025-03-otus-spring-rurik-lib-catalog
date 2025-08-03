# Unified Booking Workflow Implementation

## Overview
one comprehensive flow that processes: client + booking object → booking → payment → schedule management.

## Implementation Details

### 1. Unified Complete Booking Flow
- **Flow Name**: `completeBookingWorkflow`
- **Channel**: `completeBookingChannel`
- **Gateway Method**: `processCompleteBooking(Booking booking)`

### 2. Complete Workflow Steps
The unified flow handles the entire process in a single call:

1. **Input Validation**: Validates that all required fields (clientId, tenantId, scheduleId, deedId) are present
2. **Schedule Availability Check**: Validates that the requested schedule slot is available
3. **Booking Creation**: Creates the booking with PENDING_PAYMENT status
4. **Payment Initiation**: Initiates payment processing and generates payment ID
5. **Payment Confirmation**: Simulates payment completion (in real scenario, would come from webhook)
6. **Booking Confirmation**: Updates booking status to CONFIRMED while preserving all original data
7. **Schedule Booking**: Marks the schedule slot as occupied by the client
8. **Payout Processing**: Processes payout to the tenant

### 3. Key Features
- **Single Entry Point**: One method call `processCompleteBooking()` handles the entire workflow
- **Data Preservation**: All original booking data is maintained throughout the process
- **Error Handling**: Invalid requests are filtered out and return null
- **Comprehensive Logging**: Detailed logging at each step for monitoring and debugging
- **Backward Compatibility**: Existing separate flows are preserved for compatibility

### 4. Complete Booking Components

#### Configuration
- Added `completeBookingChannel()` message channel
- Added `completeBookingWorkflow()` integration flow
- Added required imports for BigDecimal and LocalDateTime

#### Gateway 
- Added `processCompleteBooking(Booking booking)` method with 10-second timeout

#### Service 
- Added `confirmBookingWithData(Payment payment, Booking originalBooking)` method to preserve booking data

#### Test 
- Added `testUnifiedCompleteBookingWorkflow()` to test the complete unified flow
- Added `testUnifiedWorkflowWithMissingFields()` to test error handling

### 5. Usage Example

```java
// Create booking request with all required fields
Booking booking = new Booking();
booking.setClientId(3L);
booking.setTenantId(3L);
booking.setScheduleId(3L);
booking.setDeedId(3L);

// Process complete booking workflow in one call
Booking result = bookingGateway.processCompleteBooking(booking);

// Result contains:
// - Generated booking ID
// - CONFIRMED status
// - Payment ID
// - All original booking data preserved
// - Schedule marked as occupied
```
## Available Shell Commands

### Complete Unified Booking Workflow
Test the new unified workflow that handles everything in one call:

```bash
shell:>complete-booking --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1
```
**Alias:** `pcb --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1`

**Expected Output:**
```
=== Starting Complete Booking Workflow ===
=== Booking Processing Log ===
Booking ID: null
Client ID: 1
Tenant ID: 1
Schedule ID: 1
Deed ID: 1
Status: null
Payment ID: null
Booking Time: null
==============================
Validating availability for schedule: 1
Creating booking for client: 1 with tenant: 1
Booking created with ID: 1704397200000, Status: PENDING_PAYMENT
Payment initiated for booking: 1704397200000, payment ID: PAY_ABC12345
Payment confirmation processed: PAY_ABC12345
Booking confirmed: 1704397200000
Schedule marked as booked: 1
Payout processed for booking: 1704397200000 to tenant: 1
=== Complete Booking Workflow Finished Successfully ===
Final booking status: CONFIRMED
Schedule 1 is now occupied by client 1

Complete booking workflow finished successfully: ID=1704397200000, Status=CONFIRMED, Payment ID=PAY_ABC12345, Schedule 1 is now occupied by client 1
```

### Individual Step Commands

#### 1. Create Booking Only
```bash
shell:>create-booking --client-id 2 --tenant-id 2 --schedule-id 2 --deed-id 2
```
**Alias:** `cb --client-id 2 --tenant-id 2 --schedule-id 2 --deed-id 2`

#### 2. Confirm Payment
```bash
shell:>confirm-payment --booking-id 1704397200000 --transaction-id PAY_ABC12345 --amount 100.00
```
**Alias:** `cp --booking-id 1704397200000 --transaction-id PAY_ABC12345`

#### 3. Process Payout
```bash
shell:>process-payout --booking-id 1704397200000 --tenant-id 2
```
**Alias:** `pp --booking-id 1704397200000 --tenant-id 2`

### Test Commands

#### Test Complete Flow 
```bash
shell:>test-booking-flow
```
**Alias:** `tbf`

#### Test Invalid Booking
```bash
shell:>test-invalid-booking
```
**Alias:** `tib`

#### Create Booking Asynchronously
```bash
shell:>create-booking-async --client-id 3 --tenant-id 3 --schedule-id 3 --deed-id 3
```
**Alias:** `cba --client-id 3 --tenant-id 3 --schedule-id 3 --deed-id 3`

## Testing Scenarios

### Successful Complete Workflow scenario
```bash
shell:>complete-booking --client-id 100 --tenant-id 200 --schedule-id 300 --deed-id 400
```

### Invalid Booking (Missing Fields) scenario
```bash
shell:>complete-booking --client-id 100 --tenant-id 200
# This will be rejected due to missing schedule-id and deed-id
```

### Multiple Bookings scenario
```bash
shell:>complete-booking --client-id 1 --tenant-id 1 --schedule-id 1 --deed-id 1
shell:>complete-booking --client-id 2 --tenant-id 2 --schedule-id 2 --deed-id 2
shell:>complete-booking --client-id 3 --tenant-id 3 --schedule-id 3 --deed-id 3
```

## View Help
To see help for the specific complete booking command:
```bash
shell:>help complete-booking
```