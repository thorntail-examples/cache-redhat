/*
 *
 *  Copyright 2016-2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wildfly.swarm.cachebooster;

import org.jboss.logging.Logger;
import org.wildfly.swarm.cachebooster.dto.CacheCheck;
import org.wildfly.swarm.cachebooster.dto.Message;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/31/18
 */
@Path("/")
@Produces("application/json")
public class GreetingResource {

    private static final Logger log = Logger.getLogger(GreetingResource.class);

    @Inject
    private NameCache cache;

    @GET
    @Path("greeting")
    public Message greeting() {
        log.info("getting the greeting");
        String value = cache.get();
        if (value == null) {
            value = getNameFromNameService();
            cache.put(value);
        }
        return new Message(value);
    }

    @GET
    @Path("cached")
    public CacheCheck isCached() {
        log.debug("checking if the value is cached");
        return new CacheCheck(cache.hasValue());
    }

    @DELETE
    @Path("cached")
    public Response deleteFromCache() {
        log.info("removing cached value");
        cache.remove();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private String getNameFromNameService() {
        String url = "http://wfswarm-cache-name:8080/"; // TODO: make it configurable
        WebTarget target = ClientBuilder.newClient().target(url);
        NameDto dto = target.path("name").request().get().readEntity(NameDto.class);
        return dto.getName();
    }

    public static class NameDto {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
