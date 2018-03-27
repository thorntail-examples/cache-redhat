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

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.logging.Logger;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/31/18
 */
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

    private RemoteCache<String, String> cache;

    @PostConstruct
    public void setUp() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.tcpNoDelay(true)
                .connectionPool()
                .addServer()
                .host(infinispanHost)
                .port(infinispanPort);
        RemoteCacheManager cacheManager = new RemoteCacheManager(configurationBuilder.build());
        cache = cacheManager.getCache();
        log.debugv("configured cache %s:%d", infinispanHost, infinispanPort);
    }

    public void put(String name) {
        cache.put(KEY, name, 5, TimeUnit.SECONDS);
    }

    public String get() {
        return cache.get(KEY);
    }

    public boolean hasValue() {
        return cache.containsKey(KEY);
    }

    public void remove() {
        cache.remove(KEY);
    }
}
