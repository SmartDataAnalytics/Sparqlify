#jarVersion = $(shell xpath -q -e '/project/version/text()' pom.xml)
#dpkgVersion = $(shell echo "$(jarVersion)" | sed -r 's|(.*)-SNAPSHOT|\1|')
#dpkgDir = "debian/sparqlify-$(dpkgVersion)"

#debFile = "debian/sparqlify_0.2_all.deb"

deb:
	mvn -U clean install -Dmaven.test.skip=true
	cd sparqlify-debian && mvn clean install deb:package

