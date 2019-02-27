This is the root folder for Android/GCM simulator

It is developed on top of netty (http://www.jboss.org/netty).

Steps to run:

1. Build the simulator using gradle

   sample targets:
   ../gradlew distZip
   ../gradlew installDist
   
   See the docs for the gradle applicaton plugin for more information.
   

2. Install the distribution.

3. Edit Config file: android.properties

	# Sample android.properties config info:

	# this is also required in the android sender plugin when using the simulator
	android.use.dummy.trust.manager=true                 

	# URL on which the simulator listens for notifications
	android.sim.url=https://localhost:10999/gcm

	# flag for verbose tracing
	android.sim.verbose=true

	# don't change unless required
	android.sim.ssl.KeyManagerFactory.algorithm=SunX509

	# following properties are used to create a dummy SSL context
	android.sim.keystore.type=PKCS12
	android.sim.keystore.file.path=C:\\svn\\trunk\\robotest\\apnssim\\res\\apns-dev-cert-key.p12
	android.sim.keystore.file.password=hsbc1234


4. In order to simulate response errors the simulator checks the content of the notification message and generate the error requested, or a plain response if the error object is missing from the notification.
The simulator reads the values of the error object and echoes them in the response.

Here is a sample of the request for an "InternalServerError" in the response:


{ "collapse_key": "score_update",
  "time_to_live": 108,
  "delay_while_idle": true,
  "data": {
    "error": {
       "httpStatusCode" : "200",
       "success" : "0",
       "failure" : "1",
       "errorCode" : "InternalServerError"
       }
  },
  "registration_ids":["4", "8", "15", "16", "23", "42"]
}


And here is the response:

{ "multicast_id": 108,"success": 0,"failure": 1,"canonical_ids": 0,"results": [   { "error": "InternalServerError" }  ]}



4.1 To test registration id change use the following content:

{"registration_ids": ["1APA91bFnMHbucNU2qq8h4fqh7x-ETFrk-42fzkYbsAuK"],"data": {"score": "value1","time": "value2","registration_id": "SY-yBbj95Itl1h09wR830nY2pBnLU63PJh4eV2uo3Q" }}

The response:
{ "multicast_id": 108,"success": 1,"failure": 0,"canonical_ids": 1,"results": [   { "message_id": "1:08" , "registration_id" : "SY-yBbj95Itl1h09wR830nY2pBnLU63PJh4eV2uo3Q" }  ]}



5. launch the simulator by ./bin/fcmsim android.properties

The simulator will display: "Server started..."

If verbose is enabled the simulator will display incoming notification and other traces in the console window.
