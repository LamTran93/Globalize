# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:22-jre

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . /app

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/booking_platform-0.0.1-SNAPSHOT.jar"]