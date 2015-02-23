dbUser="postgres"
dbPass="postgres"
dbJdbcUrl="jdbc:postgresql://localhost:5432/lgd"

java -cp target/sparqlify-cli-0.6.16-SNAPSHOT-jar-with-dependencies.jar org.aksw.sparqlify.web.Main -j "$dbJdbcUrl" -U "$dbUser" -W "$dbPass" -m /usr/share/linkedgeodata/sml/LinkedGeoData-Triplify-IndividualViews.sml -n 1000 -t 180

