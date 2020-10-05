#echo "Edit file tcp_run_middleware.sh to include instructions for launching the middleware"
#echo '  $1 - hostname of Flights'
#echo '  $2 - hostname of Cars'
#echo '  $3 - hostname of Rooms'

java -Djava.security.policy=java.policy -cp ../Client/Client.jar:. -Djava.rmi.server.codebase=file:$(pwd)/ Server.TCP.TCPMiddleware $1 $2 $3
