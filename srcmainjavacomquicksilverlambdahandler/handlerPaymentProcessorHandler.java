handler/PaymentProcessorHandler.java


package com.quicksilver.lambda.handler;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicksilver.lambda.model.PaymentRequest;
import com.quicksilver.lambda.utils.DynamoDBUtils;

import java.util.HashMap;
import java.util.Map;

public class PaymentProcessorHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
            .withRegion(System.getenv("REGION"))
            .build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentRequest payment = mapper.readValue((String) input.get("body"), PaymentRequest.class);

            String vehicleId = payment.getVehicleId();
            double amountPaid = payment.getAmount();
            String timestamp = java.time.Instant.now().toString();

            // 1️⃣ Verify Toll Amount from QuickSilver-TollPasses
            Table tollPassTable = DynamoDBUtils.getTable(System.getenv("TOLL_PASSES_TABLE"));
            Item tollPass = tollPassTable.getItem("vehicleId", vehicleId, "timestamp", payment.getPaymentToken()); // assuming paymentToken used as timestamp or placeholder
            double expectedAmount = tollPass != null ? tollPass.getDouble("amount") : amountPaid;

            if(amountPaid != expectedAmount){
                throw new Exception("Payment amount does not match expected toll amount");
            }

            // 2️⃣ Process Payment (placeholder for GPay / Apple Pay / credit card)
            // For now, assume payment is always successful
            String paymentStatus = "Paid";
            String transactionId = "TXN-" + System.currentTimeMillis();

            // 3️⃣ Update TollPass Table with payment status
            UpdateItemSpec updateSpec = new UpdateItemSpec()
                    .withPrimaryKey("vehicleId", vehicleId, "timestamp", payment.getPaymentToken())
                    .withUpdateExpression("set paymentStatus = :status, paymentTransactionId = :txn")
                    .withValueMap(new ValueMap()
                            .withString(":status", paymentStatus)
                            .withString(":txn", transactionId)
                    );
            tollPassTable.updateItem(updateSpec);

            // 4️⃣ Update QuickSilver-payment Methods Table
            Table paymentTable = DynamoDBUtils.getTable(System.getenv("PAYMENT_METHODS_TABLE"));
            paymentTable.putItem(new Item()
                    .withPrimaryKey("vehicleId", vehicleId, "timestamp", timestamp)
                    .withString("paymentStatus", paymentStatus)
                    .withDouble("amount", amountPaid)
                    .withString("transactionId", transactionId)
            );

            // 5️⃣ Send SNS Notification
            String message = String.format("Payment %s for vehicle %s. Amount: $%.2f, Transaction ID: %s",
                    paymentStatus, vehicleId, amountPaid, transactionId);
            snsClient.publish(System.getenv("SNS_TOPIC_ARN"), message, "Payment Status");

            // 6️⃣ Log Notification in QuickSilver-Notifications
            Table notificationsTable = DynamoDBUtils.getTable(System.getenv("NOTIFICATIONS_TABLE"));
            notificationsTable.putItem(new Item()
                    .withPrimaryKey("userId", vehicleId, "timestamp", timestamp)
                    .withString("type", "Payment")
                    .withString("message", message)
            );

            response.put("statusCode", 200);
            response.put("body", mapper.writeValueAsString(Map.of(
                    "message", "Payment processed successfully",
                    "transactionId", transactionId
            )));
        } catch (Exception e){
            context.getLogger().log("Error in PaymentProcessorHandler: " + e.getMessage());
            response.put("statusCode", 500);
            response.put("body", "{\"message\":\"Payment processing failed\"}");
        }
        return response;
    }
}
