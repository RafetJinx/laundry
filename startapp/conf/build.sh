#!/bin/bash

# Move two directories up
cd .. || exit 1

# Ask the user whether to build the app
read -p "Do you build this app?
1. Yes
2. No
Enter your choice (1 or 2): " answer

if [ "$answer" = "1" ]; then
    echo "Building the app..."

    # Run Maven commands
    mvn clean
    sleep 2

    mvn install
    mvn package

    echo "Running the application..."
    # Make sure the correct path to the JAR file is specified
    java -jar target/demo-1.0.0.jar  # Correct the path to the JAR file

elif [ "$answer" = "2" ]; then
    echo "Skipping the build."

    echo "Running the application..."
    # Run the JAR file directly if skipping build
    java -jar target/demo-1.0.0.jar  # Correct the path to the JAR file

else
    echo "Invalid choice. Please enter 1 or 2."
fi
