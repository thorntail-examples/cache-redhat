# Thorntail Cache Example

To run locally, run the generated jars with `-Dthorntail.project.stage=local`.

To deploy to OpenShift, use `mvn fabric8:deploy -Popenshift` or run `oc create -f ...` on the YAMLs in the `.openshiftio` directory. 
 
The `greeting-service` requires a running JDG server. In OpenShift, you can create one with `oc apply -f service.cache.yml`.
