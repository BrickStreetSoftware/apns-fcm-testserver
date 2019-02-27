This is the root folder for the APNS simulator.

It is developed on top of netty (http://www.jboss.org/netty).

Steps to run:

1. build the simulator: set the correct JAVA_HOME folder in build.bat and run it (or build directly using the ant build file build.xml)

2. set the correct environment variables in run.bat, configure the host and port in apns.properties (very similar to the one used in the plugin):
apns.gateway.host=localhost
apns.gateway.port=2195
apns.feedback.host=localhost
apns.feedback.port=2196

3. feedback service will read the data from files with names starting with "feedback" from the folder specified in apns.properties and will collect all content before sending the data back to the caller:

apns.feedback.data.folder=C:\\svn\\trunk\\robotest\\apnssim\\feedbackData


4. launch the simulator by running runapnssim.bat (the simulator will display: "Server started..."

The simulator will display all kinds of traces in the console window.