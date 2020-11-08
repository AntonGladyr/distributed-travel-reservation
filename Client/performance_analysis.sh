#!/bin/bash

#example: ./performance_analysis.sh 1 100

ID=$1 # command prompt (client) id for running in parallel
NUM_OF_TRANSACTIONS=$2

HOST="lab2-41.cs.mcgill.ca"
SERVER="Server"
CLIENT_CMD="java -Djava.security.policy=java.policy -cp ../Server/RMIInterface.jar:. Client.RMIClient $HOST $SERVER"

# Set up a pipe named `/tmp/client_id`
rm -f /tmp/"client_$ID"
mkfifo /tmp/"client_$ID"

# start client interactive with pipe input
$CLIENT_CMD < /tmp/"client_$ID" > /dev/null 2>&1 &

# keep pipe open
sleep infinity > /tmp/"client_$ID" &
SLEEP_PID=`echo $!`

# interact with the java input
cat transaction.txt >> /tmp/"client_$ID"

#TODO For each iteration of the loop, execute a new transaction with different input parameters

#TODO At the client, measure the response time of the transaction


kill -9 $SLEEP_PID

# Successful execution
exit 0
