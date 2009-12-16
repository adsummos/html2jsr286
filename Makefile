# I don't know how to write ant tasks. This should be one. - lamikae

jarlib=WEB-INF/lib
classes=WEB-INF/classes
liferay_v=`grep 'LIFERAY_VERSION' WEB-INF/src/PortletVersion.java | grep -o .,. | tr ',' '.'`
hotdeploydir='/usr/local/liferay/tomcat/webapps/ROOT/WEB-INF/classes'

# version of Liferay's portal-service jar
#liferay=5.1.1
liferay=5.2.3

all: compile list

clean:
	find $(classes) -name *.class -exec rm -f {} \;
	if [ -e test/classes ]; then rm -rf test/classes/* ; fi

compile: clean
	#
	###################### compiling all Java classes
	#
# careful here, as the CLASSPATH easily falls out of the javac namespace,
# see the ';\' between the export and the for loop, it is required.
# portal-service and servlet-api are included to satisfy Liferay classes.
	
# how to build packages that are compatible with older versions?
#$(jarlib)/portal-service-5.1.1.jar:\

	export CLASSPATH="\
	$(jarlib)/portlet-2.0.jar:\
	$(jarlib)/commons-logging.jar:\
	$(jarlib)/commons-httpclient-3.1.jar:\
	$(jarlib)/portal-service-$(liferay).jar:\
	$(jarlib)/servlet-api-2.4.jar:\
	$(jarlib)/htmlparser-1.6.jar" ;\
	javac WEB-INF/src/*.java -target jsr14 -Xlint:unchecked -Xlint:deprecation -d $(classes)

#xargs javac -target jsr14 -d classes/ <<< `find src/ -name *.java`

list:
	#
	###################### bytecode classes
	#
	tree $(classes)

test: compile
	#
	###################### running tests
	#
	if [ -e test/classes ]; then rm -rf test/classes/* ; fi
	mkdir -p test/classes
	export CLASSPATH="\
	$(classes):\
	test/classes:\
	$(jarlib)/portlet-2.0.jar:\
	$(jarlib)/commons-logging.jar:\
	$(jarlib)/commons-httpclient-3.1.jar:\
	$(jarlib)/commons-codec-1.3.jar:\
	$(jarlib)/portal-service-$(liferay).jar:\
	$(jarlib)/servlet-api-2.4.jar:\
	$(jarlib)/htmlparser-1.6.jar:\
	$(jarlib)/log4j-1.2.15.jar:\
	$(jarlib)/junit-4.6.jar:\
	$(jarlib)/spring-test-2.5.6.jar:\
	$(jarlib)/spring-core-2.5.6.jar:\
	test" ;\
	javac test/*.java -Xlint:unchecked -Xlint:deprecation -d test/classes && \
	time java -ea  org.junit.runner.JUnitCore com.celamanzi.liferay.portlets.rails286.TestLoader

hotdeploy: compile
	rm -rf $(hotdeploydir)/com/celamanzi/liferay/portlets/rails286/
	cp -r WEB-INF/classes/com $(hotdeploydir)

help:
	@echo "To compile classes:"
	@echo " 	make all"
	@echo
	@echo "To run the (very sparse) test suite:"
	@echo " 	make test"


.PHONY: list deploy test help
