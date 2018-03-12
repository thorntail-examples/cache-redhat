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

import com.jayway.restassured.response.Response;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.hamcrest.Matchers;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 3/9/18
 */
@RunWith(Arquillian.class)
public class OpenshiftIT {
    private static final String NAME_SERVICE_APP = "wfswarm-cache-name";
    private static final String GREETING_SERVICE_APP = "wfswarm-cache-greeting";

    @RouteURL(NAME_SERVICE_APP)
    private URL nameServiceUrl;

    @RouteURL(GREETING_SERVICE_APP)
    private URL greetingServiceUrl;

    @Before
    public void setup() {
        await().pollInterval(1, TimeUnit.SECONDS).atMost(5, TimeUnit.MINUTES).until(() -> {
            try {
                return isOk(greetingServiceUrl + "api/alive")
                        && isOk(nameServiceUrl + "api/alive");
            } catch (Exception ignored) {
                return false;
            }
        });
        clearCache();
    }

    @Test
    public void shouldWorkSlowOnFirstQuery() {
        long start = System.currentTimeMillis();
        getGreeting();
        long time = System.currentTimeMillis() - start;
        assertThat("Server responded too fast, expected at least 2000 ms, got response in " + time,
                time,
                greaterThanOrEqualTo(2000L));
    }

    @Test
    public void shouldWorkFastOnConsecutiveQuery() {
        getGreeting();

        long start = System.currentTimeMillis();
        getGreeting();
        long time = System.currentTimeMillis() - start;
        assertThat("Server didn't respond fast enough. Expecting response in 1000 ms, got in " + time,
                time,
                lessThanOrEqualTo(1000L));
    }

    @Test
    public void shouldClearCache() {
        when().get(greetingServiceUrl + "api/cached")
                .then().body("cached", Matchers.is(false));
        getGreeting();

        when().get(greetingServiceUrl + "api/cached")
                .then().body("cached", Matchers.is(true));

        clearCache();
        when().get(greetingServiceUrl + "api/cached")
                .then().body("cached", Matchers.is(false));
    }

    private void getGreeting() {
        when().get(greetingServiceUrl + "api/greeting")
                .then().assertThat().statusCode(200)
                .body("message", startsWith("hello"));
    }

    private void clearCache() {
        Response response = delete(greetingServiceUrl + "api/cached");
        assertEquals(204, response.getStatusCode());
    }

    private boolean isOk(String url) {
        return get(url).getStatusCode() == 200;
    }
}
