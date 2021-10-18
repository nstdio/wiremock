/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.extension;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.stream.Collectors.toList;

public class ExtensionLoader {

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> loadExtension(String... classNames) {
        return (Map<String, T>) asMap(
                Stream.of(classNames)
                        .map(toClasses())
                        .map(toExtensions())
                        .collect(toList())
        );
    }

    public static Map<String, Extension> load(String... classNames) {
        return loadExtension(classNames);
    }

    public static Map<String, Extension> asMap(Iterable<Extension> extensions) {
        return Maps.uniqueIndex(
                extensions,
                Extension::getName);
    }

    public static Map<String, Extension> load(Class<? extends Extension>... classes) {
        return asMap(Stream.of(classes).map(toExtensions()).collect(toList()));
    }

    private static Function<Class<? extends Extension>, Extension> toExtensions() {
        return extensionClass -> {
            try {
                return extensionClass.newInstance();
            } catch (Exception e) {
                return throwUnchecked(e, Extension.class);
            }

        };
    }

    private static Function<String, Class<? extends Extension>> toClasses() {
        return className -> {
            try {
                return (Class<? extends Extension>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                return throwUnchecked(e, Class.class);
            }
        };
    }

    public static <T extends Extension> Predicate<Map.Entry<String, Extension>> valueAssignableFrom(final Class<T> extensionType) {
        return input -> extensionType.isAssignableFrom(input.getValue().getClass());
    }
}
