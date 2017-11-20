/*
 *  Copyright 2016 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.favre.tools.dconvert.arg;

import at.favre.tools.dconvert.converters.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines platforms to convert to
 */
public enum EPlatform {
    ANDROID(new AndroidConverter()),
    IOS(new IOSConverter()),
    WINDOWS(new WindowsConverter()),
    WEB(new WebConverter());

    private final IPlatformConverter converter;

    EPlatform(IPlatformConverter converter) {
        this.converter = converter;
    }

    public IPlatformConverter getConverter() {
        return converter;
    }

    private static Set<EPlatform> ALL;

    public static Set<EPlatform> getAll() {
        if (ALL == null) {
            Set<EPlatform> temp = new HashSet<>(EPlatform.values().length);
            for (EPlatform ePlatform : EPlatform.values()) {
                temp.add(ePlatform);
            }
            ALL = Collections.unmodifiableSet(temp);
        }
        return ALL;
    }

    public static List<IPlatformConverter> getAllConverters() {
        return getAll().stream().map(EPlatform::getConverter).collect(Collectors.toList());
    }
}
