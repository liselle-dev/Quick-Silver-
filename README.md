# Quick-Silver-
QuickSilver is a mobile application designed to allow drivers to make immediate, fee-free toll payments when they forget their toll pass, use the wrong transponder, or forget to reload their transponder balance. The app detects toll usage in real time and allows drivers to pay instantly or shortly after passing through a toll, avoiding penalty fees
QuickSilver Backend Integration
Project Overview

QuickSilver is a serverless toll payment system that handles user registration, authentication, toll payments, and notifications. This repository contains the backend integration code using AWS services, ready to be connected with a React Native front-end.

Tech Stack
Layer	Technology
Front-End	React Native, AWS Amplify
Back-End	AWS Lambda, API Gateway, DynamoDB, EventBridge
Authentication	Amazon Cognito
Notifications	Amazon SNS, Amazon SES
Location / Maps	AWS Location Services
Monitoring & Logging	Amazon CloudWatch
Version Control	GitHub
Repository Structure
quicksilver-tech-integration/
├── aws-setup.js           # AWS SDK configuration
├── createUser.js          # User registration
├── signIn.js              # User login
├── editProfile.js         # Edit user profile
├── paymentProcessor.js    # Process toll payments
├── mainMenu.js            # Main menu options
├── lambdaHandler.js       # API Gateway Lambda entry point
├── package.json           # Node.js dependencies
└── README.md              # Project documentation

Environment Variables

Set the following environment variables in your Lambda function or local .env file:

Key	Example Value
AWS_REGION	us-east-2
COGNITO_USER_POOL_ID	us-east-2_jnPSlO1tw
COGNITO_APP_CLIENT_ID	5rpp2p7omas5e4voi261jlmbi8
DYNAMODB_TABLE_NAME	QuickSilverTable
SNS_TOPIC_ARN	arn:aws:sns:us-east-2:142754574142:QuickSilverAlerts

API Endpoints

The Lambda handler exposes the following endpoints via API Gateway:

Endpoint	Method	Description
/createUser	POST	Registers a new user (email, password, name)
/signIn	POST	Signs in a user and returns JWT tokens
/editProfile	PUT	Updates user profile information
/processPayment	POST	Processes toll payments for a vehicle
Request & Response Examples
1. Create User

POST /createUser
Request Body:

{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "John Doe"
}


Response:

{
  "success": true,
  "message": "User registration successful"
}

2. Sign In

POST /signIn
Request Body:

{
  "email": "user@example.com",
  "password": "Password123!"
}


Response:

{
  "success": true,
  "idToken": "<JWT ID Token>",
  "accessToken": "<JWT Access Token>",
  "refreshToken": "<JWT Refresh Token>"
}

3. Edit Profile

PUT /editProfile
Request Body:

{
  "email": "user@example.com",
  "updatedData": {
    "name": "Jane Doe",
    "phone": "+1234567890",
    "address": "123 Main St"
  }
}


Response:

{
  "success": true,
  "message": "Profile updated successfully"
}

4. Process Payment

POST /processPayment
Request Body:

{
  "vehicleId": "ABC123",
  "amount": 5.00,
  "paymentToken": "tok_visa_123"
}


Response:

{
  "success": true,
  "message": "Payment successful",
  "transactionId": "ch_1ABCDEF..."
}

Setup & Deployment

Clone the repository:

git clone https://github.com/yourusername/quicksilver-tech-integration.git


Install Node.js dependencies:

cd quicksilver-tech-integration
npm install
