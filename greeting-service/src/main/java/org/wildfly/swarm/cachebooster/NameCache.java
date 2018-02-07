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

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/31/18
 */
@ApplicationScoped
public class NameCache {
    public static final String CACHE_NAME = "nameCache";

    private static final String KEY = "name";

    private Integer timeToLive = 5;

    @Resource(lookup = "java:jboss/infinispan/container/server")
    private EmbeddedCacheManager cacheManager;

    private Cache<String, String> cache;

    @PostConstruct
    public void setUp() {
        cache = cacheManager.getCache(CACHE_NAME);
    }

    public void put(String name) {
        cache.put(KEY, name, timeToLive, TimeUnit.SECONDS);
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
