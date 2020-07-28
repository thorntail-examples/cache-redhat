# Thorntail Cache Example

## Purpose

This example demonstrates how to cache data in a remote Infinispan cache.

## Prerequisites

* Log into an OpenShift cluster of your choice: `oc login ...`.
* Select a project in which the services will be deployed: `oc project ...`.

## Modules

The `greeting-service` module serves the web interface and communicates with the `cute-name-service` and the cache..

The `cute-name-service` module provides an endpoint that simulates a long-running action whose result is worth caching.

## Deployment

Run the following commands to configure and deploy the applications.

### Deployment using S2I

```bash
oc apply -f ./greeting-service/.openshiftio/service.cache.yml

oc apply -f ./greeting-service/.openshiftio/application.yaml
oc new-app --template=thorntail-cache-greeting

oc apply -f ./cute-name-service/.openshiftio/application.yaml
oc new-app --template=thorntail-cache-cute-name
```

### Deployment with the Fabric8 Maven Plugin

```bash
oc apply -f service.cache.yml

mvn clean fabric8:deploy -Popenshift
```

## Test everything

This is completely self-contained and doesn't require the application to be deployed in advance.
Note that this may delete anything and everything in the OpenShift project.

```bash
mvn clean verify -Popenshift,openshift-it
```

## Running locally

Run the generated jars with `-Dthorntail.project.stage=local`.
