PWD = $(shell pwd)
DIR_PATH = $(shell dirname $(PWD))

all: java.policy compile-server-rmi

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:$(DIR_PATH)/-\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

../Client/Client.jar: ../Client/Client/*.java
	make -C ../Client/ Client.jar

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class

compile-server-rmi: ../Client/Client.jar Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java	
	javac -cp ../Client/Client.jar Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class
	rm -f RMIInterface.jar
	rm -f java.policy 
