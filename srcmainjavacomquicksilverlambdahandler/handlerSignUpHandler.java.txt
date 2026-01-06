handler/SignUpHandler.java


package com.quicksilver.lambda.handler;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicksilver.lambda.model.UserRequest;
import com.quicksilver.lambda.utils.DynamoDBUtils;

import java.util.HashMap;
import java.util.Map;

public class SignUpHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final AWSCognitoIdentityProvider cognito = AWSCognitoIdentityProviderClientBuilder.standard()
            .withRegion(System.getenv("REGION"))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserRequest user = mapper.readValue((String) input.get("body"), UserRequest.class);

            // Cognito Sign Up
            SignUpRequest signUpRequest = new SignUpRequest()
                    .withClientId(System.getenv("COGNITO_CLIENT_ID"))
                    .withUsername(user.getEmail())
                    .withPassword(user.getPassword())
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(user.getEmail())
                    );
            cognito.signUp(signUpRequest);

            // Save to DynamoDB
            Table usersTable = DynamoDBUtils.getTable(System.getenv("USERS_TABLE"));
            usersTable.putItem(new Item()
                    .withPrimaryKey("userId", user.getEmail())
                    .withString("name", user.getName())
                    .withString("createdAt", java.time.Instant.now().toString())
            );

            response.put("statusCode", 201);
            response.put("body", "{\"message\":\"User registration successful\"}");
        } catch (Exception e) {
            context.getLogger().log("Error in SignUpHandler: " + e.getMessage());
            response.put("statusCode", 500);
            response.put("body", "{\"message\":\"User registration failed\"}");
        }
        return response;
    }
}
