# TODO remove reference to rmi
#Usage: ./tcp_run_server.sh [<rmi_name>]

java -Djava.security.policy=java.policy Server.TCP.TCPResourceManager $1
