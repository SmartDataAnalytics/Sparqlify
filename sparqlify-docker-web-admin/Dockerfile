FROM ubuntu:14.04

MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

RUN export DEBIAN_FRONTEND=noninteractive

# OP#3182 : set the locale
# http://jaredmarkell.com/docker-and-locales/
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN echo "deb http://archive.ubuntu.com/ubuntu/ trusty main restricted universe multiverse" > /etc/apt/sources.list && \
    echo "deb http://archive.ubuntu.com/ubuntu/ trusty-updates main restricted universe multiverse" >> /etc/apt/sources.list && \
    echo "deb http://archive.ubuntu.com/ubuntu/ trusty-backports main restricted universe multiverse" >> /etc/apt/sources.list && \
    echo "deb http://archive.ubuntu.com/ubuntu/ trusty-security main restricted universe multiverse" >> /etc/apt/sources.list

RUN apt-get update
RUN apt-get -y install postgresql supervisor wget openjdk-7-jdk dialog libterm-readline-gnu-perl

RUN apt-get install -qy apache2 libapache2-mod-proxy-html && \
	apt-get install -qy apache2-mpm-prefork apache2-utils  && \
	apt-get install -qy libapache2-mod-authnz-external git

RUN a2enmod proxy proxy_ajp 

#ADD 000-default.conf /etc/apache2/sites-available/000-default.conf

#dialog libterm-readline-gnu-perl
RUN echo "deb http://cstadler.aksw.org/repos/apt precise main contrib non-free" > /etc/apt/sources.list.d/cstadler.aksw.org.list
RUN wget -O - http://cstadler.aksw.org/repos/apt/conf/packages.precise.gpg.key | apt-key add -

# ADD answers/answers
RUN echo "force-confold" >> /etc/dpkg/dpkg.cfg
RUN echo "force-confdef" >> /etc/dpkg/dpkg.cfg
RUN apt-get update

# certificate workaround
#RUN \
#	mkdir /etc/ssl/private-copy; \ 
#	mv /etc/ssl/private/* /etc/ssl/private-copy/; \
#	rm -r /etc/ssl/private; \
#	mv /etc/ssl/private-copy /etc/ssl/private; \
#	chmod -R 0700 /etc/ssl/private; \
#	chown -R postgres /etc/ssl/private

#RUN apt-get -fy install sparqlify-tomcat7

ADD install-app.sh /install-app.sh
RUN chmod +x /install-app.sh
RUN /install-app.sh

#ADD install_facete2.sh /install_facete2.sh
#RUN chmod +x /install_facete2.sh
#RUN /install_facete2.sh
#RUN wget -P /usr/share/tomcat7/lib/ http://repo1.maven.org/maven2/postgresql/postgresql/8.4-701.jdbc4/postgresql-8.4-701.jdbc4.jar
#RUN wget -O /usr/share/java/tomcat-dbcp-7.0.30.jar http://search.maven.org/remotecontent?filepath=org/apache/tomcat/tomcat-dbcp/7.0.30/tomcat-dbcp-7.0.30.jar

#RUN service postgresql start
#RUN apt-get -y install facete2-tomcat7
#RUN service postgresql stop

# workaround SHMMAX problem of postgresql in some envs
RUN sed -ie "s&^shared_buffers =.*&shared_buffers = 16MB&" "/etc/postgresql/9.3/main/postgresql.conf"

# configure supervisord
#RUN mkdir -p /etc/supervisor/conf.d

ADD etc /etc

#ADD etc /etc/supervisor/conf.d/supervisord.conf
#ADD etc /etc/default/tomcat7
#ADD etc /etc/tomcat7/server.xml

EXPOSE 8080
EXPOSE 80

CMD ["/usr/bin/supervisord"]

