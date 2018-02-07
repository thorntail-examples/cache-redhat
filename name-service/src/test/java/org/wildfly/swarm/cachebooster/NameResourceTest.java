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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 2/9/18
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class NameResourceTest {

    private static final String EXPECTED_RESULT = "\\{\"name\":\".*\"}";
    private static final String BASE_URI = "http://localhost:8080";

    @Test
    @RunAsClient
    public void testGetName() {
        Response response = RestAssured.when().get(BASE_URI + "/name");
        response.then().assertThat().statusCode(200);
        String result = responseAsString(response);
        assertTrue("Actual response '" + result + "' didn't match the expected expression: '" + EXPECTED_RESULT,
                result.matches(EXPECTED_RESULT));
    }

    private String responseAsString(Response response) {
        String result = response.getBody().print();
        result = result.replaceAll("\\s", "");
        return result;
    }
}
