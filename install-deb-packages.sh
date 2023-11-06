#/bin/sh

p1=`find sparqlify-pkg-parent/sparqlify-pkg-deb-cli/target | grep deb$`
#p2=`find sparqlify-debian-tomcat-common/target | grep deb$`
#p3=`find sparqlify-debian-tomcat7/target | grep deb$`
#p4=`find sparqlify-debian-tomcat8/target | grep deb$`

#sudo dpkg -i "$p1" "$p2" "$p3" "$p4"
sudo dpkg -i "$p1"
