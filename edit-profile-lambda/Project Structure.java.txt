Project Structure


quicksilver-lambda-java/
├── pom.xml
└── src/main/java/com/quicksilver/lambda/
    ├── handler/
    │   ├── SignUpHandler.java
    │   ├── SignInHandler.java
    │   ├── EditProfileHandler.java
    │   ├── TollNotificationHandler.java
    │   └── PaymentProcessorHandler.java
    ├── model/
    │   ├── UserRequest.java
    │   ├── SignInRequest.java
    │   ├── EditProfileRequest.java
    │   └── PaymentRequest.java
    └── utils/
        └── DynamoDBUtils.java
