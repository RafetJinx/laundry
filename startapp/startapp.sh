#!/bin/bash

echo "Hello, welcome to the application!"
sleep 2

read -p "Do you have a database?
1. No, I will start it via Docker. I have set up the Compose YAML.
2. Yes, and I have configured it in application.properties.
Enter your choice (1 or 2): " answer

if [ "$answer" = "1" ]; then
  docker-compose up -d

  sh conf/build.sh

elif [ "$answer" = "2" ]; then
  sh conf/build.sh

else
  echo "Invalid choice. Please enter 1 or 2."
fi
