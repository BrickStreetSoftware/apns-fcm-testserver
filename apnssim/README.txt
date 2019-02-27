This is the root folder for the APNS simulator.

It is developed on top of netty (http://www.jboss.org/netty).

Steps to run:

1. build the simulator: ../gradlew installDist 
   or ../gradlew distZip


2. configure the host and port in apns.properties

apns.gateway.host=localhost
apns.gateway.port=2195
apns.feedback.host=localhost
apns.feedback.port=2196


3. feedback service will read the data from files with names starting with "feedback" from the folder specified in apns.properties and will collect all content before sending the data back to the caller:

apns.feedback.data.folder=feedbackData


4. launch the simulator by running ./bin/apnssim 

   the simulator will display: "Server started..."


When it runs, the simulator will display all kinds of traces in the console window.
