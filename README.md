# thorntail-cache-mission

To run locally, run the generated jars with `-Dswarm.project.stage=local`

To deploy to OpenShift, use `mvn fabric8:deploy -Popenshift` or run 
`oc create -f ...` on the YAMLs in the .openshift directory. 
 
 
The greeting-service requires a running JDG server. In OpenShift, you 
can create one with `oc apply -f service.cache.yml`.
