#!/bin/bash

#example: ./performance_analysis.sh 1 10 false transaction_single_rm.txt

ID=$1 # command prompt (client) id for running in parallel
NUM_OF_EXPERIMENTS=$2 # max number of transactions per second
IS_MULTI_CLIENT=$3 # for the case when there is a single client it is not necessary to sleep
TRANSACTION_FILE=$4 # file containing a transaction


HOST="lab2-41.cs.mcgill.ca"
SERVER="Server"
CLIENT_CMD="java -Djava.security.policy=java.policy -cp ../Server/RMIInterface.jar:. Client.RMIClient $HOST $SERVER"
TOTAL_TIME=0
TR_ID=1

function send_workload() {
	# interact with the java input
	cat /tmp/"$TRANSACTION_FILE" >> /tmp/"client_$ID"
	
	while true; do
		#TODO check for commit or abort
		#abort=`cat /tmp/client_1_output | grep "Abort" | wc -l`
		#commit=`cat /tmp/client_1_output | grep "Commit" | wc -l`	
		NUM=`cat /tmp/"client_$ID"_output | grep "Customer Deleted" | wc -l`
		if [ "$NUM" -eq "1" ]
		then
			break
		fi
	done
}

# Set up a pipe named `/tmp/client_id`
rm -f /tmp/"client_$ID"
rm -f /tmp/"client_$ID"_output
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt
mkfifo /tmp/"client_$ID"

# start client interactive with pipe input
# redirect input and output
$CLIENT_CMD < /tmp/"client_$ID" > /tmp/"client_$ID"_output 2>&1 &

# keep pipe open
sleep infinity > /tmp/"client_$ID" &
SLEEP_PID=`echo $!`

# wait for the client-prompt to load
sleep 1.75s

# create tmp files for response times and workloads
touch /tmp/"response_time_$ID".txt
touch /tmp/"workload_$ID".txt

# for each iteration of the loop, execute a new transaction with different input parameters
for EXP_NUM in $(seq 1 $NUM_OF_EXPERIMENTS) # iterate through each experiment
do
	NUM_OF_TRANSACTIONS=$EXP_NUM
	for TR in $(seq 1 $NUM_OF_TRANSACTIONS) # send transactions
	do
		# make a copy of the transcation.txt file
		cp "$TRANSACTION_FILE" /tmp/"$TRANSACTION_FILE"
		# find and replace the input values
		sed -i -e "s/<xid>/$TR_ID/g" /tmp/"$TRANSACTION_FILE"
		#sed -i -e 's/<customerID>//g' /tmp/"$TRANSACTION_FILE"
		# at the client, measure the response time of the transaction		
		RESPONSE_TIME=`{ time send_workload >> /tmp/"client_$ID"; } 2>&1 | grep real | cut -f2 | sed -e 's/.m\(.*\)s/\1/'`
		SEC=`echo $RESPONSE_TIME | cut -d"." -f1`
		MILLISECONDS=`echo $RESPONSE_TIME | cut -d"." -f2`
		# remove the leading zero
		SEC=${SEC#0}
		MILLISECONDS=${MILLISECONDS#0}
		MILLISECONDS=$(((SEC * 1000) + MILLISECONDS))
		TOTAL_TIME=$((TOTAL_TIME + MILLISECONDS))	
		#cat /tmp/"client_$ID"_output
		> /tmp/"client_$ID"_output	
		#echo "===================================="
		rm -f /tmp/"$TRANSACTION_FILE"
		
		if [ "$IS_MULTI_CLIENT" = true ]
		then
			:
			#TODO sleep
		fi	

		# increment transaction ID
		TR_ID=$((TR_ID + 1))
	done
	
	# reset the total response time
	# not sure if we need to reset the transaction id
	echo "$TOTAL_TIME" >> /tmp/"response_time_$ID".txt
	echo "$NUM_OF_TRANSACTIONS" >> /tmp/"workload_$ID".txt
	TOTAL_TIME=0
done

# close client prompt
echo "quit" >> /tmp/"client_$ID"

kill -9 $SLEEP_PID

# Successful execution
exit 0
