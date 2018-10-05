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

import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 3/9/18
 */
@RunWith(Arquillian.class)
public class OpenshiftIT {
    private static final String NAME_SERVICE_APP = "thorntail-cache-cute-name";
    private static final String GREETING_SERVICE_APP = "thorntail-cache-greeting";

    @RouteURL(NAME_SERVICE_APP)
    @AwaitRoute(statusCode = {200, 204}, path = "/health")
    private URL nameServiceUrl;

    @RouteURL(GREETING_SERVICE_APP)
    @AwaitRoute(statusCode = {200, 204}, path = "/health")
    private URL greetingServiceUrl;

    @Before
    public void setup() {
        clearCache();
    }

    @Test
    public void greetingShouldBeCached() {
        String first = getGreeting();
        String second = getGreeting();

        assertThat(first, is(second));
    }

    @Test
    public void greetingShouldChangeAfterCacheClear() {
        String first = getGreeting();
        clearCache();
        String second = getGreeting();

        assertThat(first, is(not(second)));
    }

    @Test
    public void cacheShouldExpire() throws InterruptedException {
        String first = getGreeting();
        TimeUnit.SECONDS.sleep(6); // wait for expiration, TTL of the cute name cache entry is 5 seconds
        String second = getGreeting();

        assertThat(first, is(not(second)));
    }

    @Test
    public void firstRequestShouldBeSlow() {
        long time = measureTime(this::getGreeting);

        assertThat("Server responded too fast, expected at least 2000 ms, got response in " + time,
                time, greaterThanOrEqualTo(2000L));
    }

    @Test
    public void secondRequestShouldBeFast() {
        getGreeting();

        long time = measureTime(this::getGreeting);

        assertThat("Server didn't respond fast enough. Expecting response in 1000 ms, got in " + time,
                time, lessThanOrEqualTo(1000L));
    }

    @Test
    public void shouldClearCache() {
        getGreeting();
        assertCached(true);

        clearCache();
        assertCached(false);
    }

    // @formatter:off
    private void assertCached(boolean cached) {
        when()
                .get(greetingServiceUrl + "api/cached")
        .then()
                .body("cached", is(cached));
    }

    private String getGreeting() {
        String greeting =
                when()
                        .get(greetingServiceUrl + "api/greeting")
                .then()
                        .statusCode(200)
                        .body("message", startsWith("Hello"))
                .extract()
                        .jsonPath()
                        .get("message");
        return greeting;
    }

    private void clearCache() {
        when()
                .delete(greetingServiceUrl + "api/cached")
        .then()
                .statusCode(204);
    }
    // @formatter:on

    private long measureTime(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }
}
