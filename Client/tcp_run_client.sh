# TODO remove reference to rmi
# Usage: ./tcp_run_client.sh [<server_hostname> [<server_rmiobject>]]

java -Djava.security.policy=java.policy -cp ../Server/TCPInterface.jar:. Client.TCPClient $1 $2
