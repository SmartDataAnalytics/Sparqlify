jarVersion = $(shell xpath -q -e '/project/version/text()' pom.xml)
dpkgVersion = $(shell echo "$(jarVersion)" | sed -r 's|(.*)-SNAPSHOT|\1|')
dpkgDir = "debian/sparqlify-$(dpkgVersion)"

debFile = "debian/sparqlify_0.2_all.deb"

debuild:
#	@echo $(jarVersion)
#	@echo $(targetVersion)
	mvn clean assembly:assembly
	cp "target/sparqlify-$(jarVersion)-jar-with-dependencies.jar" "$(dpkgDir)/sparqlify.jar"
	cd $(dpkgDir); \
	debuild

# Build and install on system
debinst: debuild
#	The '-' in front of the command causes make to ignore any errors (i.e. its exit code)
	-sudo apt-get -y remove sparqlify
	sudo dpkg -i "$(debFile)"

