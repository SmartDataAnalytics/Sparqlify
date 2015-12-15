# Sparqlify Docker for Web Admin

 * [facete2](https://github.com/AKSW/Sparqlify)


## Build

    docker build -t sparqlify .


## Run

 * run one instance, open http://<docker ip>:8080/sparqlify in your browser:


    docker run -d -p 8080:8060 -p 80:8061 --name sparqlify sparqlify

 * run many times, open http://<docker ip>:<container port>/sparqlify in your browser:


    docker run --rm -P sparqlify
    docker ps


## ToDos

 * current config is a bad hack, even it works though
 * fix facete2-tomcat7 package as it starts tomcat with error, thus causes regular build to fail

