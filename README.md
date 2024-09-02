To configure environment variables for your Spring Boot application using IntelliJ IDEA, follow these steps:

Open Run/Debug Configurations:

In the top menu of IntelliJ IDEA, click on the dropdown list of Run/Debug configurations.
Select "Edit Configurations...".
Create or Edit a Configuration:

Choose the configuration you want to modify, or create a new one by clicking on the "+" button and selecting the type of configuration (e.g., "Application" for Java applications).
Add Environment Variables:

In the "Configuration" tab of your selected or newly created configuration, find the "Environment variables" field.
Click on the "..." button next to this field to open the dialog window for adding environment variables.
Add your environment variables by filling in the keys and values. For example:

DB_USERNAME=mydatabaseuser
DB_PASSWORD=mypassword
DB_URL=jdbc:mysql://localhost:3306/mydatabase
Click "OK" or "Apply" to save the changes.
Use Environment Variables in application.yml:

In your application.yml file, you can reference these environment variables as follows:
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
server:
  port: ${SERVER_PORT:8080}
  
Run Your Application:
Ensure you run your application using the configuration you modified. The application will use the values provided in the environment variables.
