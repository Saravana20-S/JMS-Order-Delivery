# JMS Order Processing System

A practical asynchronous Order Processing System built using **Spring
Boot 4.1.1**, **Spring JMS**, **Apache ActiveMQ Artemis**,
**PostgreSQL**, **Spring Data JPA**, **Hibernate**, **Lombok**,
**Jakarta Validation**, and **Postman**.

The project demonstrates how a REST API can create and process orders
asynchronously using JMS queues, consumers, producers, event tracking,
cancellation, retry handling, notifications, and Dead Letter Queue (DLQ)
concepts.

------------------------------------------------------------------------

## 1. Project Objective

The objective of this project is to build a backend order-processing
system where a customer creates an order through a REST API and the
actual processing is performed asynchronously through JMS.

The main architecture is:

``` text
REST API
   |
   v
Order Controller
   |
   v
Order Service
   |
   v
Order Producer
   |
   v
ActiveMQ Artemis
   |
   v
order.queue
   |
   v
Order Consumer
   |
   +----> Inventory Processing
   |
   +----> Payment Processing
   |
   v
Order Status / Order Events
   |
   v
Notification Producer
   |
   v
notification.queue
   |
   v
Notification Consumer
```

------------------------------------------------------------------------

## 2. Features

The system provides six REST endpoints:

  -----------------------------------------------------------------------------
\#                Method            Endpoint                Purpose
  ----------------- ----------------- ----------------------- -----------------
1                 POST              `/orders`               Create an order
and send a JMS
message

2                 GET               `/orders/{id}`          Get the current
order status

3                 POST              `/orders/{id}/cancel`   Request
asynchronous
order
cancellation

4                 POST              `/orders/{id}/retry`    Retry a failed
order

5                 GET               `/orders/{id}/events`   View order
processing events

6                 GET               `/orders/failed`        View failed
orders
  -----------------------------------------------------------------------------

------------------------------------------------------------------------

## 3. Technologies Used

-   Java 17+
-   Spring Boot 4.1.1
-   Spring Web
-   Spring JMS
-   Jakarta JMS
-   Apache ActiveMQ Artemis
-   Spring Data JPA
-   Hibernate
-   PostgreSQL
-   Lombok
-   Jakarta Validation
-   Jackson ObjectMapper
-   Maven
-   Postman
-   Docker Desktop

------------------------------------------------------------------------

## 4. Project Package Structure

The application uses the base package:

``` text
com.bridgelabz.jms
```

Recommended structure:

``` text
src/main/java
└── com/bridgelabz/jms
    │
    ├── JmsApplication.java
    │
    ├── config
    │   └── JmsConfig.java
    │
    ├── controller
    │   └── OrderController.java
    │
    ├── service
    │   ├── OrderService.java
    │   └── OrderServiceImpl.java
    │
    ├── producer
    │   ├── OrderProducer.java
    │   ├── CancelProducer.java
    │   └── NotificationProducer.java
    │
    ├── consumer
    │   ├── OrderConsumer.java
    │   ├── CancelConsumer.java
    │   └── NotificationConsumer.java
    │
    ├── dto
    │   ├── CreateOrderRequest.java
    │   ├── OrderMessage.java
    │   └── NotificationMessage.java
    │
    ├── entity
    │   ├── Order.java
    │   └── OrderEvent.java
    │
    ├── repository
    │   ├── OrderRepository.java
    │   └── OrderEventRepository.java
    │
    ├── enums
    │   ├── OrderStatus.java
    │   └── EventType.java
    │
    └── exception
        ├── OrderNotFoundException.java
        └── GlobalExceptionHandler.java
```

------------------------------------------------------------------------

## 5. Database

PostgreSQL is used as the persistent database.

Example database:

``` text
jms_order_db
```

Configure the connection in:

``` text
src/main/resources/application.properties
```

Example:

``` properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jms_order_db
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8081
```

> Replace `your_password` with your PostgreSQL password.

------------------------------------------------------------------------

## 6. Database Tables

### Orders

The `orders` table stores the current state of every order.

Important fields:

``` text
id
customer_id
product_id
quantity
status
retry_count
correlation_id
created_at
updated_at
```

Example status lifecycle:

``` text
CREATED
   |
   v
PROCESSING
   |
   +------> FAILED
   |
   v
COMPLETED
```

Cancellation uses:

``` text
CANCEL_REQUESTED
       |
       v
CANCELLED
```

------------------------------------------------------------------------

### Order Events

The `order_events` table stores the processing history of an order.

Example events:

``` text
ORDER_CREATED
ORDER_PROCESSING
INVENTORY_CHECKED
PAYMENT_COMPLETED
ORDER_COMPLETED
ORDER_CANCEL_REQUESTED
ORDER_CANCELLED
ORDER_RETRIED
```

This provides an audit trail for the order.

------------------------------------------------------------------------

# 7. ActiveMQ Artemis

Apache ActiveMQ Artemis is used as the JMS broker.

Docker Desktop can be used to run Artemis.

Example Docker command:

``` powershell
docker run -d `
  --name artemis `
  -p 61616:61616 `
  -p 8161:8161 `
  -e ARTEMIS_USER=admin `
  -e ARTEMIS_PASSWORD=admin `
  apache/activemq-artemis:latest
```

Check whether the container is running:

``` powershell
docker ps
```

You should see ports similar to:

``` text
61616 -> JMS connection
8161  -> Artemis Web Console
```

------------------------------------------------------------------------

## 8. Artemis Web Console

Open:

``` text
http://localhost:8161
```

Login:

``` text
Username: admin
Password: admin
```

The Artemis console can be used to inspect:

-   Queues
-   Addresses
-   Consumers
-   Producers
-   Connections
-   Sessions
-   Messages
-   Redelivery
-   Dead Letter Queue messages

------------------------------------------------------------------------

## 9. JMS Queues

The project uses multiple queues.

``` text
order.queue
cancel.queue
notification.queue
order.dlq
```

### order.queue

Used for asynchronous order processing.

``` text
OrderProducer
      |
      v
order.queue
      |
      v
OrderConsumer
```

### cancel.queue

Used for asynchronous cancellation commands.

``` text
CancelProducer
      |
      v
cancel.queue
      |
      v
CancelConsumer
```

### notification.queue

Used for customer notifications.

``` text
NotificationProducer
      |
      v
notification.queue
      |
      v
NotificationConsumer
```

### order.dlq

Used for messages that repeatedly fail processing after the configured
retry/redelivery policy.

``` text
order.queue
     |
     v
Processing Failure
     |
     v
Redelivery
     |
     v
Maximum Attempts
     |
     v
order.dlq
```

------------------------------------------------------------------------

# 10. JMS Configuration

The `JmsConfig` class contains the queue names and JMS configuration.

Important concepts used:

``` text
JmsTemplate
@JmsListener
ConnectionFactory
Session Transaction
Message Converter
Correlation ID
```

The application sends JSON strings through JMS rather than directly
sending Java DTO objects.

This avoids the message-conversion problem where
`SimpleMessageConverter` cannot directly convert a normal DTO into a JMS
message.

------------------------------------------------------------------------

# 11. JSON JMS Messaging

The producer converts the DTO into JSON using Jackson:

``` text
OrderMessage
     |
     v
ObjectMapper
     |
     v
JSON String
     |
     v
JmsTemplate
```

Example:

``` json
{
  "orderId": 11,
  "customerId": 105,
  "productId": 501,
  "quantity": 2,
  "correlationId": "45689d8a-766c-4860-952c-01c05c534b10"
}
```

The consumer receives the JSON string and converts it back into
`OrderMessage`:

``` text
JMS Message
     |
     v
JSON String
     |
     v
ObjectMapper
     |
     v
OrderMessage
```

------------------------------------------------------------------------

# 12. Correlation ID

Every order receives a unique correlation ID.

Example:

``` text
45689d8a-766c-4860-952c-01c05c534b10
```

The correlation ID is used to connect:

``` text
REST Request
     |
     v
Order
     |
     v
JMS Message
     |
     v
Order Event
     |
     v
Notification
```

The producer also places the correlation ID into:

``` text
JMSCorrelationID
```

This makes it easier to trace a message through the asynchronous system.

------------------------------------------------------------------------

# 13. Endpoint 1 --- Create Order

### Request

``` http
POST http://localhost:8081/orders
```

Body:

``` json
{
  "customerId": 101,
  "productId": 501,
  "quantity": 2
}
```

### Flow

``` text
POST /orders
     |
     v
OrderController
     |
     v
OrderService
     |
     v
Save Order
     |
     v
ORDER_CREATED event
     |
     v
OrderProducer
     |
     v
order.queue
     |
     v
OrderConsumer
```

The initial status is:

``` text
CREATED
```

The JMS consumer then changes it to:

``` text
PROCESSING
```

and finally:

``` text
COMPLETED
```

------------------------------------------------------------------------

# 14. Endpoint 2 --- Get Order

### Request

``` http
GET http://localhost:8081/orders/11
```

This endpoint reads the current order state from PostgreSQL.

Possible states include:

``` text
CREATED
PROCESSING
COMPLETED
FAILED
CANCEL_REQUESTED
CANCELLED
```

Because processing is asynchronous, the response may show `CREATED`
immediately after creation and later show `PROCESSING` or `COMPLETED`.

------------------------------------------------------------------------

# 15. Endpoint 3 --- Cancel Order

### Request

``` http
POST http://localhost:8081/orders/11/cancel
```

### Flow

``` text
Cancel API
     |
     v
OrderService
     |
     v
CANCEL_REQUESTED
     |
     v
CancelProducer
     |
     v
cancel.queue
     |
     v
CancelConsumer
     |
     v
CANCELLED
```

Events can include:

``` text
ORDER_CANCEL_REQUESTED
ORDER_CANCELLED
```

------------------------------------------------------------------------

# 16. Endpoint 4 --- Retry Failed Order

### Request

``` http
POST http://localhost:8081/orders/11/retry
```

The order must be in:

``` text
FAILED
```

before retrying.

Example flow:

``` text
FAILED
   |
   v
Retry API
   |
   v
retryCount + 1
   |
   v
CREATED
   |
   v
order.queue
   |
   v
OrderConsumer
```

The retry count is stored in the database.

Example:

``` text
Attempt 1
Attempt 2
Attempt 3
```

------------------------------------------------------------------------

# 17. Endpoint 5 --- Order Events

### Request

``` http
GET http://localhost:8081/orders/11/events
```

Example response:

``` json
[
  {
    "eventType": "ORDER_CREATED",
    "message": "Order created successfully"
  },
  {
    "eventType": "ORDER_PROCESSING",
    "message": "Order processing started"
  },
  {
    "eventType": "INVENTORY_CHECKED",
    "message": "Inventory checked successfully"
  },
  {
    "eventType": "PAYMENT_COMPLETED",
    "message": "Payment completed successfully"
  },
  {
    "eventType": "ORDER_COMPLETED",
    "message": "Order completed successfully"
  }
]
```

This endpoint demonstrates event tracking and auditing.

------------------------------------------------------------------------

# 18. Endpoint 6 --- Failed Orders

### Request

``` http
GET http://localhost:8081/orders/failed
```

This endpoint returns orders whose status is:

``` text
FAILED
```

Example:

``` json
[
  {
    "id": 15,
    "customerId": 105,
    "productId": 501,
    "quantity": 2,
    "status": "FAILED",
    "retryCount": 3
  }
]
```

If there are no failed orders:

``` json
[]
```

------------------------------------------------------------------------

# 19. Successful Processing Flow

A successful order follows:

``` text
Client
  |
  | POST /orders
  v
OrderController
  |
  v
OrderService
  |
  +---- Save Order ----------------> PostgreSQL
  |
  +---- Save ORDER_CREATED --------> PostgreSQL
  |
  v
OrderProducer
  |
  v
JmsTemplate
  |
  v
ActiveMQ Artemis
  |
  v
order.queue
  |
  v
OrderConsumer
  |
  +---- PROCESSING
  |
  +---- Inventory
  |
  +---- PAYMENT
  |
  +---- COMPLETED
  |
  +---- Save Events
  |
  v
NotificationProducer
  |
  v
notification.queue
  |
  v
NotificationConsumer
```

------------------------------------------------------------------------

# 20. Notification Flow

After successful order processing:

``` text
OrderConsumer
     |
     v
NotificationMessage
     |
     v
NotificationProducer
     |
     v
notification.queue
     |
     v
NotificationConsumer
```

Example console output:

``` text
====================================
Notification Message Received
Order ID: 11
Customer ID: 105
Message: Your order 11 has been completed successfully.
Correlation ID: 45689d8a-766c-4860-952c-01c05c534b10
====================================

Sending notification to customer 105
Notification: Your order 11 has been completed successfully.
```

------------------------------------------------------------------------

# 21. Failure, Retry and DLQ

The JMS transaction configuration allows failed message processing to be
rolled back.

Conceptual flow:

``` text
order.queue
     |
     v
OrderConsumer
     |
     v
Business Processing
     |
     +------ SUCCESS ------> ACK
     |
     +------ FAILURE
              |
              v
           Rollback
              |
              v
          Redelivery
              |
              v
        Retry / Attempts
              |
              v
       Maximum Attempts
              |
              v
          order.dlq
```

The exact redelivery/DLQ behavior is controlled by the Artemis broker
configuration and the Spring JMS listener transaction configuration.

------------------------------------------------------------------------

# 22. JMS Acknowledgement

With successful transactional processing:

``` text
Consumer
   |
   v
Process Message
   |
   v
Database Update
   |
   v
Transaction Commit
   |
   v
Message Acknowledged
```

If processing fails:

``` text
Consumer
   |
   v
Process Message
   |
   v
Exception
   |
   v
Transaction Rollback
   |
   v
Message Eligible for Redelivery
```

This demonstrates the relationship between JMS transactions and message
processing.

------------------------------------------------------------------------

# 23. TTL

TTL means:

``` text
Time To Live
```

It controls how long a JMS message remains valid.

Conceptually:

``` text
Producer
   |
   v
Message
   |
   +---- TTL = configured duration
   |
   v
Queue
   |
   +---- Consumed before expiry
   |
   +---- Expired
```

Expired messages can be handled by broker configuration.

------------------------------------------------------------------------

# 24. Testing With Postman

## Create Order

``` http
POST http://localhost:8081/orders
Content-Type: application/json
```

``` json
{
  "customerId": 101,
  "productId": 501,
  "quantity": 2
}
```

------------------------------------------------------------------------

## Get Order

``` http
GET http://localhost:8081/orders/1
```

------------------------------------------------------------------------

## Cancel Order

``` http
POST http://localhost:8081/orders/1/cancel
```

------------------------------------------------------------------------

## Retry Order

``` http
POST http://localhost:8081/orders/1/retry
```

The order must be `FAILED`.

------------------------------------------------------------------------

## Get Events

``` http
GET http://localhost:8081/orders/1/events
```

------------------------------------------------------------------------

## Get Failed Orders

``` http
GET http://localhost:8081/orders/failed
```

------------------------------------------------------------------------

# 25. How to Verify JMS Processing

When creating an order, check the Spring Boot console.

Expected producer output:

``` text
Sending Order Message: {...}
```

Expected consumer output:

``` text
Received Order Message
Order ID: 11
Correlation ID: ...
```

Then:

``` text
Checking inventory...
Product ID: 501
Quantity: 2

Processing payment...
Customer ID: 105
```

Finally:

``` text
Order 11 COMPLETED
```

Then notification:

``` text
Notification Message Received
Order ID: 11
Customer ID: 105
```

------------------------------------------------------------------------

# 26. How to Check Artemis

Open:

``` text
http://localhost:8161
```

Go to:

``` text
Artemis
  |
  +---- Queues
          |
          +---- order.queue
          +---- cancel.queue
          +---- notification.queue
          +---- order.dlq
```

You can inspect queue statistics and, when messages are pending, browse
their contents.

A successful message may not remain visible for long because the JMS
consumer immediately receives and acknowledges it.

Therefore:

``` text
Message enters queue
       ↓
Consumer receives it
       ↓
Message disappears from pending queue
```

An empty `order.queue` does not mean JMS is broken.

------------------------------------------------------------------------

# 27. Important JMS Concepts Demonstrated

Concept           Usage
  ----------------- ------------------------------------
JMS               Messaging API
Producer          Sends messages
Consumer          Receives messages
Queue             Stores messages
JmsTemplate       Sends JMS messages
`@JmsListener`    Receives JMS messages
JSON              Message payload format
ObjectMapper      JSON serialization/deserialization
Correlation ID    Message tracing
Transaction       Groups processing operations
ACK               Successful message processing
Rollback          Failed transactional processing
Redelivery        Broker retries failed messages
Retry             Application-level retry
DLQ               Stores repeatedly failed messages
TTL               Message expiration
Event Tracking    Stores processing history
Multiple Queues   Separates business operations

------------------------------------------------------------------------

# 28. Error Handling

The project should handle common errors such as:

-   Order not found
-   Invalid order data
-   Invalid cancellation
-   Retry of non-failed order
-   JMS conversion errors
-   Database errors
-   Business processing failures

A global exception handler can return consistent REST error responses.

Example:

``` json
{
  "status": 404,
  "message": "Order not found with id: 11",
  "timestamp": "2026-08-29T10:30:00"
}
```

------------------------------------------------------------------------

# 29. Validation

The create-order request should validate:

``` text
customerId
productId
quantity
```

For example:

``` text
customerId > 0
productId > 0
quantity > 0
```

Invalid requests should be rejected before the order is created.

------------------------------------------------------------------------

# 30. Recommended Development Order

The project can be developed in the following sequence:

``` text
1. Spring Boot Project Setup
        ↓
2. PostgreSQL Configuration
        ↓
3. ActiveMQ Artemis Setup
        ↓
4. JMS Configuration
        ↓
5. Entity Classes
        ↓
6. Repository Layer
        ↓
7. DTOs
        ↓
8. Enums
        ↓
9. Order Producer
        ↓
10. Order Consumer
        ↓
11. Create Order API
        ↓
12. Order Status API
        ↓
13. Notification Producer/Consumer
        ↓
14. Cancel Producer/Consumer
        ↓
15. Cancel API
        ↓
16. Order Events API
        ↓
17. Failure Handling
        ↓
18. Retry
        ↓
19. Redelivery
        ↓
20. DLQ
        ↓
21. TTL
        ↓
22. Postman Testing
```

------------------------------------------------------------------------

# 31. Example End-to-End Scenario

### Step 1 --- Create order

``` http
POST /orders
```

``` json
{
  "customerId": 105,
  "productId": 501,
  "quantity": 2
}
```

### Step 2 --- Database

``` text
Order saved
Status = CREATED
```

### Step 3 --- JMS

``` text
OrderProducer
      ↓
order.queue
```

### Step 4 --- Consumer

``` text
OrderConsumer
      ↓
PROCESSING
      ↓
Inventory
      ↓
Payment
      ↓
COMPLETED
```

### Step 5 --- Events

``` text
ORDER_CREATED
ORDER_PROCESSING
INVENTORY_CHECKED
PAYMENT_COMPLETED
ORDER_COMPLETED
```

### Step 6 --- Notification

``` text
NotificationProducer
      ↓
notification.queue
      ↓
NotificationConsumer
```

### Final result

``` text
Order Status = COMPLETED
```

------------------------------------------------------------------------

# 32. Project Learning Outcomes

After completing this project, the following concepts are covered:

-   Spring Boot REST APIs
-   Layered architecture
-   DTO pattern
-   Service layer
-   Repository layer
-   PostgreSQL persistence
-   Spring Data JPA
-   Hibernate
-   JMS
-   ActiveMQ Artemis
-   JMS Producer
-   JMS Consumer
-   `JmsTemplate`
-   `@JmsListener`
-   JSON messaging
-   ObjectMapper
-   Correlation IDs
-   Multiple queues
-   Asynchronous processing
-   Transaction management
-   Acknowledgement
-   Rollback
-   Redelivery
-   Retry
-   Dead Letter Queue
-   TTL
-   Event tracking
-   Notification processing
-   Error handling
-   Postman API testing
-   Docker-based message broker

------------------------------------------------------------------------

# 33. Troubleshooting

## JMS DTO Conversion Error

If you see:

``` text
Cannot convert object of type OrderMessage to JMS message
```

send a JSON string instead of directly sending the DTO:

``` text
OrderMessage
    ↓
ObjectMapper.writeValueAsString()
    ↓
String
    ↓
JmsTemplate
```

------------------------------------------------------------------------

## ObjectMapper Bean Not Found

If Spring reports:

``` text
required a bean of type 'ObjectMapper' that could not be found
```

make sure Jackson is available through the Spring Boot web stack or
explicitly configure an `ObjectMapper` bean.

------------------------------------------------------------------------

## Artemis Connection Problem

Check:

``` powershell
docker ps
```

Then verify:

``` text
localhost:61616
```

is accessible for JMS and:

``` text
localhost:8161
```

is accessible for the Artemis console.

------------------------------------------------------------------------

## Queue Shows 0 Messages

This is often normal.

If the consumer is running:

``` text
Producer
   ↓
Queue
   ↓
Consumer
   ↓
ACK
```

the message can be consumed before you open the queue browser.

------------------------------------------------------------------------

# 34. Future Improvements

Possible improvements for a production-style version include:

-   Real inventory service
-   Real payment service
-   Email/SMS notification service
-   Idempotent message processing
-   Structured logging
-   Distributed tracing
-   Authentication and authorization
-   API documentation with OpenAPI/Swagger
-   Automated tests
-   Integration tests with Artemis
-   Docker Compose for PostgreSQL + Artemis + application
-   Configurable retry policies
-   Broker-level DLQ configuration
-   Monitoring and metrics
-   Health checks
-   Externalized configuration

------------------------------------------------------------------------

## Author

**BridgeLabz JMS Practical Project**

Package:

``` text
com.bridgelabz.jms
```

Technology:

``` text
Spring Boot 4.1.1 + JMS + ActiveMQ Artemis + PostgreSQL
```
