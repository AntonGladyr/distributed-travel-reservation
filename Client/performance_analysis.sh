#!/bin/bash

#example: ./performance_analysis.sh 1 10 false transaction_single_rm.txt

ID=$1 # command prompt (client) id for running in parallel
NUM_OF_EXPERIMENTS=$2 # max number of transactions per second
INCREMENT=$3 # the increment of transactions in each experiment
IS_MULTI_CLIENT=$4 # for the case when there is a single client it is not necessary to sleep
EXPERIMENT_DURATION=$5 # in seconds
TRANSACTION_FILE=$6 # file containing a transaction
TMP_TRANSACTION_FILE=`echo "$TRANSACTION_FILE" | cut -d"." -f1`
TMP_TRANSACTION_FILE+="_$ID.txt"


HOST="lab2-41.cs.mcgill.ca"
SERVER="Server"
CLIENT_CMD="java -Djava.security.policy=java.policy -cp ../Server/RMIInterface.jar:. Client.RMIClient $HOST $SERVER"
TOTAL_TIME=0
TR_ID=1

function send_workload() {
	# clean the output
	> /tmp/"client_$ID"_output
	# create new transaction
	echo "start" >> /tmp/"client_$ID"
	while true; do
		# read the transaction id
		TR_ID_LINE=`cat /tmp/"client_$ID"_output | grep -a 'Transaction ID (xid)' | wc -l`
		if [ "$TR_ID_LINE" -eq "1" ]
		then
			TR_ID=`cat /tmp/"client_$ID"_output | grep -a 'Transaction ID (xid)' | cut -d":" -f2 | tr -d ' '` 
			break
		fi
	done
	
	# make a copy of the transcation.txt file
	cp "$TRANSACTION_FILE" /tmp/"$TMP_TRANSACTION_FILE"
	# find and replace the input values	
	sed -i -e "s/<xid>/$TR_ID/g" /tmp/"$TMP_TRANSACTION_FILE"
	#sed -i -e "s/<FlightNumber>/$ID/g" /tmp/"$TMP_TRANSACTION_FILE"
	#sed -i -e 's/<customerID>//g' /tmp/"$TMP_TRANSACTION_FILE"
	# at the client, measure the response time of the transaction
	
	# interact with the java input
	cat /tmp/"$TMP_TRANSACTION_FILE" >> /tmp/"client_$ID"

	echo "commit,$TR_ID" >> /tmp/"client_$ID"
	
	while true; do
		# check for commit or abort
		NUM_COMMIT=`cat /tmp/"client_$ID"_output | grep -a 'committed' | wc -l`
		NUM_ABORT=`cat /tmp/"client_$ID"_output | grep -a 'aborted' | wc -l`
		if [ "$NUM_COMMIT" -eq "1" ] || [ "$NUM_ABORT" -eq "1" ]
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
FIRST=1
LAST=$NUM_OF_EXPERIMENTS

# warming up
END=$((SECONDS+3))
while [ $SECONDS -lt $END ]; do
	RESPONSE_TIME=`{ time send_workload >> /tmp/"client_$ID"; } 2>&1 | grep real | cut -f2 | sed -e 's/.m\(.*\)s/\1/'`
	sleep 1.s
done

for EXP_NUM in $(seq $FIRST $INCREMENT $LAST) # iterate through each experiment
do
	END=$((SECONDS+EXPERIMENT_DURATION))
	NUM_OF_TRANSACTIONS=$EXP_NUM
	rm -f /tmp/"response_time_samples_$ID".txt
	# send transactions in loop
	#while [ $SECONDS -lt $END ]; do
		for TR in $(seq 1 $NUM_OF_TRANSACTIONS) # send transactions
		do
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
			rm -f /tmp/"$TMP_TRANSACTION_FILE"
		
			
			if [ "$IS_MULTI_CLIENT" = true ]
			then
				# sleep for x seconds
				sleep $(( NUM_OF_EXPERIMENTS / NUM_OF_TRANSACTIONS ))s
			fi

			# increment transaction ID
			TR_ID=$((TR_ID + 1))
		done

		# save the samples
		#echo "$TOTAL_TIME" >> /tmp/"response_time_samples_$ID".txt

		#AVG_RESPONSE=$(( TOTAL_TIME / NUM_OF_TRANSACTIONS ))
		#echo "$AVG_RESPONSE" >> /tmp/"response_time_samples_$ID".txt
		# reset the total response time
		#TOTAL_TIME=0
	#done
	AVG_RESPONSE=$(( TOTAL_TIME / NUM_OF_TRANSACTIONS ))
	echo "$AVG_RESPONSE" >> /tmp/"response_time_$ID".txt
	
	# compute the average and save
	#NUM_OF_SAMPLES=`cat /tmp/"response_time_samples_$ID".txt | wc -l`
	#SKIP_SAMPLES=($EXPERIMENT_DURATION * 0.1)
	SKIP_SAMPLES=0
	FIRST=$((SKIP_SAMPLES + 1))
	LAST=$((NUM_OF_SAMPLES - SKIP_SAMPLES))
	RESPONSE_SUM=0
	#for LINE in $(seq $FIRST $LAST)
	#do
	#	SAMPLE=`sed -n "$LINE"p < /tmp/"response_time_samples_$ID.txt"`
	#	RESPONSE_SUM=$((RESPONSE_SUM + SAMPLE))
	#done
	
	#echo "$TOTAL_TIME" >> /tmp/"response_time_$ID".txt

	
	#NUM_OF_SAMPLES=$((NUM_OF_SAMPLES - (SKIP_SAMPLES * 2)))
	#AVG_RESPONSE=$((RESPONSE_SUM / NUM_OF_SAMPLES))
	#echo "$AVG_RESPONSE" >> /tmp/"response_time_$ID".txt
	echo "$NUM_OF_TRANSACTIONS" >> /tmp/"workload_$ID".txt
	sleep 1s
done

# close client prompt
echo "quit" >> /tmp/"client_$ID"

kill -9 $SLEEP_PID

# Successful execution
exit 0
