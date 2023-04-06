/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kyligence.zenml.toolkit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class EnvironmentPropertyProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SPRING_ORIGIN_VALUE_CLASS = "org.springframework.boot.origin.OriginTrackedValue$OriginTrackedCharSequence";

    private static final AtomicBoolean preInitializationStarted = new AtomicBoolean(false);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!preInitializationStarted.compareAndSet(false, true)) {
            return;
        }

        for (PropertySource propertySource : environment.getPropertySources()) {
            if (propertySource instanceof OriginTrackedMapPropertySource) {
                OriginTrackedMapPropertySource originPropertySource = (OriginTrackedMapPropertySource) propertySource;
                Map<String, Object> source = originPropertySource.getSource();

                source.forEach((k, v) -> {
                    if (v instanceof OriginTrackedValue) {
                        Object value = ((OriginTrackedValue) v).getValue();
                        if (value instanceof String) {
                            Origin origin = ((OriginTrackedValue) v).getOrigin();
                            String trimVal = ((String) value).trim();

                            try {
                                Class<?> aClass = Class.forName(SPRING_ORIGIN_VALUE_CLASS);

                                Constructor<?> constructor = aClass.getDeclaredConstructor(CharSequence.class, Origin.class);
                                constructor.setAccessible(true);

                                // TODO decrypt password

                                Object o = constructor.newInstance(trimVal, origin);

                                source.put(k, o);
                            } catch (Exception e) {
                                log.error("Java reflection error", e);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}

