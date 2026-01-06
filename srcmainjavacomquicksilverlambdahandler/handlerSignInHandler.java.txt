handler/SignInHandler.java


package com.quicksilver.lambda.handler;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicksilver.lambda.model.SignInRequest;

import java.util.HashMap;
import java.util.Map;

public class SignInHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final AWSCognitoIdentityProvider cognito = AWSCognitoIdentityProviderClientBuilder.standard()
            .withRegion(System.getenv("REGION"))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        try {
            SignInRequest request = mapper.readValue((String) input.get("body"), SignInRequest.class);

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withUserPoolId(System.getenv("COGNITO_USER_POOL_ID"))
                    .withClientId(System.getenv("COGNITO_CLIENT_ID"))
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .addAuthParametersEntry("USERNAME", request.getEmail())
                    .addAuthParametersEntry("PASSWORD", request.getPassword());

            AdminInitiateAuthResult authResult = cognito.adminInitiateAuth(authRequest);

            Map<String, String> body = new HashMap<>();
            body.put("message", "Sign-in successful");
            body.put("idToken", authResult.getAuthenticationResult().getIdToken());
            body.put("accessToken", authResult.getAuthenticationResult().getAccessToken());
            body.put("refreshToken", authResult.getAuthenticationResult().getRefreshToken());

            response.put("statusCode", 200);
            response.put("body", mapper.writeValueAsString(body));
        } catch (Exception e) {
            context.getLogger().log("Error in SignInHandler: " + e.getMessage());
            response.put("statusCode", 401);
            response.put("body", "{\"message\":\"Invalid credentials\"}");
        }
        return response;
    }
}
