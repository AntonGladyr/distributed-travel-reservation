all: java.policy compile-server-rmi compile-server-tcp

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

RMIInterface.jar: Server/Interface/*.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/*.java
	jar cvf RMIInterface.jar Server/Interface/*.class

compile-server-rmi: Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java	
	javac Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java
	
compile-server-tcp: Server/TCP/*.java Server/Interface/*.java Server/Common/*.java
	javac Server/TCP/*.java Server/Interface/*.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class Server/TCP/*.class
	rm -f RMIInterface.jar
	rm -f java.policy 
