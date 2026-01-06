handler/EditProfileHandler.java


package com.quicksilver.lambda.handler;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicksilver.lambda.model.EditProfileRequest;
import com.quicksilver.lambda.utils.DynamoDBUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class EditProfileHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final AWSCognitoIdentityProvider cognito = AWSCognitoIdentityProviderClientBuilder.standard()
            .withRegion(System.getenv("REGION"))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        try {
            EditProfileRequest profile = mapper.readValue((String) input.get("body"), EditProfileRequest.class);

            // Update DynamoDB
            Table usersTable = DynamoDBUtils.getTable(System.getenv("USERS_TABLE"));
            UpdateItemSpec updateSpec = new UpdateItemSpec()
                    .withPrimaryKey("userId", profile.getEmail())
                    .withUpdateExpression("set #name = :name, #phone = :phone, #address = :address")
                    .withNameMap(new HashMap<String,String>() {{
                        put("#name", "name");
                        put("#phone", "phone");
                        put("#address", "address");
                    }})
                    .withValueMap(new HashMap<String,Object>() {{
                        put(":name", profile.getName());
                        put(":phone", profile.getPhone());
                        put(":address", profile.getAddress());
                    }});
            UpdateItemOutcome outcome = usersTable.updateItem(updateSpec);

            // Update Cognito attributes if needed
            List<AttributeType> attributes = new ArrayList<>();
            if(profile.getName()!=null) attributes.add(new AttributeType().withName("name").withValue(profile.getName()));
            if(profile.getPhone()!=null) attributes.add(new AttributeType().withName("phone_number").withValue(profile.getPhone()));
            if(!attributes.isEmpty()){
                AdminUpdateUserAttributesRequest updateRequest = new AdminUpdateUserAttributesRequest()
                        .withUserPoolId(System.getenv("COGNITO_USER_POOL_ID"))
                        .withUsername(profile.getEmail())
                        .withUserAttributes(attributes);
                cognito.adminUpdateUserAttributes(updateRequest);
            }

            response.put("statusCode", 200);
            response.put("body", "{\"message\":\"Profile updated successfully\"}");
        } catch (Exception e) {
            context.getLogger().log("Error in EditProfileHandler: " + e.getMessage());
            response.put("statusCode", 500);
            response.put("body", "{\"message\":\"Profile update failed\"}");
        }
        return response;
    }
}
