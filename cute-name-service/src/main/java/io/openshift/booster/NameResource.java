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
package io.openshift.booster;

import io.openshift.booster.dto.NameDto;
import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/31/18
 */
@Path("/name")
@Produces("application/json")
public class NameResource {

    private static final Logger log = Logger.getLogger(NameResource.class);

    @GET
    public NameDto getName() {
        log.info("generating the name");
        performSlowOperation();
        return new NameDto(UserNameGenerator.generate());
    }

    private void performSlowOperation() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException ignored) {
        }
    }

}
