```python
import json
import boto3
import os

# Initialize the SNS client
sns_client = boto3.client('sns')

def send_notification(message, subject):
"""
Sends a notification message via AWS SNS.

:param message: The message to be sent
:param subject: The subject of the notification
"""

topic_arn = os.environ.get('SNS_TOPIC_ARN') # Set this environment variable in your AWS Lambda function

try:
response = sns_client.publish(
TopicArn=topic_arn,
Message=json.dumps({'default': message}),
Subject=subject,
MessageStructure='json'
)
print(f"Notification sent: {response['MessageId']}")
except Exception as e:
print(f"Error sending notification: {e}")

def lambda_handler(event, context):
"""
AWS Lambda function to handle incoming events.

:param event: AWS Lambda uses this argument to pass in event data to the handler
:param context: Provides runtime information to your handler
"""

# Example notification for payment confirmation
if 'paymentStatus' in event:
status = event['paymentStatus'] # Received from the event
user_contact = event.get('userContact', 'user@example.com') # Fallback contact

if status == 'successful':
message = f"Dear User, your payment was successful."
subject = "Payment Confirmation"
elif status == 'pending':
message = f"Dear User, your payment is pending."
subject = "Payment Pending Notification"
else:
message = "Invalid payment status."
subject = "Notification Error"

send_notification(message, subject)
else:
print("No payment status found in the event.")
```
