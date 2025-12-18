```markdown
## SNS Integration Overview

This document outlines the integration of AWS SNS for sending notifications related to user payments in the toll system.

### Key Features:
- Sends notifications for different payment statuses (successful, pending).
- Utilizing Amazon SNS for real-time updates to users.
- Configurable subject lines for enhanced user communication.

### Environment Setup:
- Set the `SNS_TOPIC_ARN` environment variable in your AWS Lambda to ensure the script can publish notifications correctly.
