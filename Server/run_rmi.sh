port="$(cat ../PORT)"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $port &
