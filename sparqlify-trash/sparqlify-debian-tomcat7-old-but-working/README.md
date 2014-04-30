# Sparqlify Web Admin Deployment Guide for Tomcat 7 on Ubuntu

## Prerequisites

Although Sparqlify Web Admin comes packaged as a Debian package, you need to adjust a few system settings before it will work.

Important: Before installing the Sparqlify Web Admin Debian package, please make sure you have a Java JDK installed and that your Tomcat configuration is adjusted according to the following instructions.
Otherwise, you may be left with a non-functional deployed war file.

* If you have not done already, install a JDK using one of the commands below. Be aware that if you have `openjdk-6-jre` (just the runtime!) installed and you additionally install `openjdk-7-jdk`, you have 2 versions of Java on your system. If `openjdk-6-jre` is then still set as the default Java, upon launching the Sparqlify Web Admin in your browser you will see an exception that a JDK is required to run this web application.

        # Pick a jdk if you need one.
        # sudo apt-get install openjdk-6-jdk
        # sudo apt-get install openjdk-7-jdk

        # Fixing your Java configuration is out of scope for this guide,
        # but this command lists the installed Java versions and may help you
        # on your decision on which jdk to install:
        
        dpkg --get-selections | grep openjdk-

* Make sure that there is an approriate JDBC driver in tomcat's lib folder. If not, you can install one using the command below. Note that we sucessfully tested version 8.4-701 against Postgresql 9.* databases, however we had issues related to incompatible datatype mappings with the 9.* JDBC drivers.

        sudo wget -P /usr/share/tomcat7/lib/ http://repo1.maven.org/maven2/postgresql/postgresql/8.4-701.jdbc4/postgresql-8.4-701.jdbc4.jar

* For running the Sparqlify Web Admin, Tomcat needs to be configured to at least 512MB or RAM.
A common recommendation to achieve this is to set `JAVA_HOME` or `CATALINA_HOME`, however this may have no effect - at least on some Ubuntu distributions, such as 12.04 - as their values may be overwriten, such as seen in the snippet below.
Edit the file `/etc/default/tomcat7` and perform the following change. 

        # The original options look something like this:
        # JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
        
        JAVA_OPTS="-Djava.awt.headless=true -Xmx512m -XX:+UseConcMarkSweepGC"

If you followed about recommendations, your chances of getting a working deployment have increased drastically.


* If you install the Sparqlify Web Admin from file - rather than a repository - you first need to install the following dependencies manually:

        sudo apt-get install tomcat7 dbconfig-common xsltproc postgresql

* The Sparqlify Web Admin debian package is available at:

[http://cstadler.aksw.org/repos/apt/pool/main/s/sparqlify-tomcat7/](http://cstadler.aksw.org/repos/apt/pool/main/s/sparqlify-tomcat7/)

* Download the sparqlify-tomcat7 package and install it with (fill out `${version}` appropriately):

        sudo dpkg -i sparqlify-tomcat7_${version}.deb

* When prompted for the database password, enter one. Important note: Currently there is a character escaping issue - do not use characters that have special meaning in XML in the password, otherwise you need to manually fix the file `/etc/tomcat7/Catalina/localhost/sparqlify.xml`.

* Visiting the following URL with your browser should show the Sparqlify Web Admin dashboard

[http://localhost:8080/sparqlify](http://localhost:8080/sparqlify)

* If this is the case: Congratulations! Otherwise, check out the trouble shooting guide.

## A Quick Mapping Example

The Sparqlify Web Admin uses a relational database for data storage.
So you can test your setup by mapping this database.

* Click on "Add New Service" and use the following settings:

        path: test
        hostname: localhost
        database: sparqlifytomcat7
        username: sparqlifytomcat7
        password: (whatever you entered when you were prompted for a password during the install of the debian package)

* Paste the following statement into the text area for the mapping:

        Prefix o: <http://example.org/ontology/>
        Prefix r: <http://example.org/resource/>

        Create View rdb2rdfExecution As
          Construct {
            ?s
              a o:Rdb2RdfExecution ;
              a o:ServiceExecution ;
              o:id ?i ;
              o:status ?st ;      .
          }
          With
            ?s = uri(r:, 'serviceExecution', ?id)
            ?i = typedLiteral(?id, xsd:int)
            ?st = plainLiteral(?status)
          From
            [[SELECT id, status FROM configtoexecution WHERE configclassname='org.aksw.sparqlify.admin.model.Rdb2RdfConfig']]

* The table in the dashboard should now show an entry. In the column "Path" click on the link "test". A new tab with the HTML view of the SPARQL endpoint should open. Runnig the default query should show some RDF data.

## Trouble Shooting
* In the browser I see an exception that I need a full JDK installed.
 * Check the prerequisites about the notes about the JDK.
* In the browser I see a blank page.
 * Check `/var/log/tomcat7/catalina.out` for exceptions.
* In `catalina.out`, I see an exception mentioning `Heap space`
 * Probably Tomcat does not have enough memory. Check the prerequisites section.
* In `catalina.out`, I see `java.lang.NoSuchMethodError: org.postgresql.core.BaseConnection.getLargeObjectAPI()Lorg/postgresql/largeobject/LargeObjectManager;`
 * Place the PostgreSQL JDBC driver into `/usr/share/tomcat7/lib`. Check the prerequisites section.


