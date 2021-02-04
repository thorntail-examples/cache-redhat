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
package io.thorntail.example;

import io.thorntail.openshift.test.AdditionalResources;
import io.thorntail.openshift.test.OpenShiftTest;
import io.thorntail.openshift.test.injection.TestResource;
import io.thorntail.openshift.test.injection.WithName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@OpenShiftTest
@AdditionalResources("classpath:test-cache.yml")
public class OpenshiftIT {
    private static final String NAME_SERVICE_APP = "thorntail-cache-cute-name";
    private static final String GREETING_SERVICE_APP = "thorntail-cache-greeting";

    @TestResource
    @WithName(NAME_SERVICE_APP)
    private URL nameServiceUrl;

    @TestResource
    @WithName(GREETING_SERVICE_APP)
    private URL greetingServiceUrl;

    @BeforeEach
    public void setup() {
        clearCache();
    }

    @Test
    public void greetingShouldBeCached() {
        String first = getGreeting();
        String second = getGreeting();

        assertThat(first).isEqualTo(second);
    }

    @Test
    public void greetingShouldChangeAfterCacheClear() {
        String first = getGreeting();
        clearCache();
        String second = getGreeting();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    public void cacheShouldExpire() throws InterruptedException {
        String first = getGreeting();
        TimeUnit.SECONDS.sleep(6); // wait for expiration, TTL of the cute name cache entry is 5 seconds
        String second = getGreeting();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    public void firstRequestShouldBeSlow() {
        long time = measureTime(this::getGreeting);

        assertThat(time)
                .as("Server responded too fast, expected at least 2000 ms, got response in " + time)
                .isGreaterThanOrEqualTo(2000L);
    }

    @Test
    public void secondRequestShouldBeFast() {
        getGreeting();

        long time = measureTime(this::getGreeting);

        assertThat(time)
                .as("Server didn't respond fast enough. Expecting response in at most 1000 ms, got in " + time)
                .isLessThanOrEqualTo(1000L);
    }

    @Test
    public void shouldClearCache() {
        getGreeting();
        assertCached(true);

        clearCache();
        assertCached(false);
    }

    private void assertCached(boolean cached) {
        given()
                .baseUri(greetingServiceUrl.toString())
        .when()
                .get("/api/cached")
        .then()
                .body("cached", is(cached));
    }

    private String getGreeting() {
        String greeting =
                given()
                        .baseUri(greetingServiceUrl.toString())
                .when()
                        .get("/api/greeting")
                .then()
                        .statusCode(200)
                        .body("message", startsWith("Hello"))
                        .extract().jsonPath().get("message");
        return greeting;
    }

    private void clearCache() {
        given()
                .baseUri(greetingServiceUrl.toString())
        .when()
                .delete("/api/cached")
        .then()
                .statusCode(204);
    }

    private long measureTime(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }
}
