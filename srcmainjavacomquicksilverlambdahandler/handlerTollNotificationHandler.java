handler/TollNotificationHandler.java


package com.quicksilver.lambda.handler;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class TollNotificationHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
            .withRegion(System.getenv("REGION"))
            .build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> bodyMap = mapper.readValue((String) input.get("body"), Map.class);

            String userId = (String) bodyMap.get("userId");
            String vehicleId = (String) bodyMap.get("vehicleId");
            String tollId = (String) bodyMap.get("tollId");
            String timestamp = java.time.Instant.now().toString();

            // 1️⃣ Save Toll Pass in DynamoDB
            Table tollPassTable = com.quicksilver.lambda.utils.DynamoDBUtils.getTable(System.getenv("TOLL_PASSES_TABLE"));
            tollPassTable.putItem(new Item()
                    .withPrimaryKey("vehicleId", vehicleId, "timestamp", timestamp)
                    .withString("userId", userId)
                    .withString("tollId", tollId)
                    .withString("paymentStatus", "Pending")
            );

            // 2️⃣ Send SNS Notification
            String message = String.format("You have passed toll %s at %s", tollId, timestamp);
            snsClient.publish(System.getenv("SNS_TOPIC_ARN"), message, "Toll Pass Alert");

            // 3️⃣ Log Notification in DynamoDB
            Table notificationsTable = com.quicksilver.lambda.utils.DynamoDBUtils.getTable(System.getenv("NOTIFICATIONS_TABLE"));
            notificationsTable.putItem(new Item()
                    .withPrimaryKey("userId", userId, "timestamp", timestamp)
                    .withString("type", "TollPass")
                    .withString("message", message)
            );

            response.put("statusCode", 200);
            response.put("body", "{\"message\":\"Toll pass recorded and notification sent\"}");
        } catch (Exception e) {
            context.getLogger().log("Error in TollNotificationHandler: " + e.getMessage());
            response.put("statusCode", 500);
            response.put("body", "{\"message\":\"Failed to record toll pass\"}");
        }
        return response;
    }
}
