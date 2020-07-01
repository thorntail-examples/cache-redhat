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

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.logging.Logger;
import org.wildfly.swarm.config.jca.CachedConnectionManager;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class NameCache {
    private static final Logger log = Logger.getLogger(NameCache.class);

    private static final String KEY = "cute-name";

    @Inject
    @ConfigurationValue("infinispan.host")
    private String infinispanHost;

    @Inject
    @ConfigurationValue("infinispan.port")
    private int infinispanPort;

    private RemoteCacheManager cacheManager;

    @PostConstruct
    public void setUp() {
        Configuration configuration = new ConfigurationBuilder()
                .tcpNoDelay(true)
                .connectionPool()
                .addServer()
                .host(infinispanHost)
                .port(infinispanPort)
                .build();
        this.cacheManager = new RemoteCacheManager(configuration);
        log.infof("configured cache %s:%d", infinispanHost, infinispanPort);
    }

    private RemoteCache<String, String> getCache() {
        return cacheManager.getCache();
    }

    @Timeout(2000)
    @Fallback(fallbackMethod = "fallbackPut")
    public void put(String name) {
        getCache().put(KEY, name, 5, TimeUnit.SECONDS);
    }

    @Timeout(2000)
    @Fallback(fallbackMethod = "fallbackGet")
    public String get() {
        return getCache().get(KEY);
    }

    @Timeout(2000)
    @Fallback(fallbackMethod = "fallbackHasValue")
    public boolean hasValue() {
        return getCache().containsKey(KEY);
    }

    @Timeout(2000)
    @Fallback(fallbackMethod = "fallbackRemove")
    public void remove() {
        getCache().remove(KEY);
    }

    private void fallbackPut(String name) {
    }

    private String fallbackGet() {
        return null;
    }

    private boolean fallbackHasValue() {
        return false;
    }

    private void fallbackRemove() {
    }
}
