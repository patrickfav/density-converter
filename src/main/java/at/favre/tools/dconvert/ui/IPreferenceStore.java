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

package at.favre.tools.dconvert.ui;

import at.favre.tools.dconvert.arg.Arguments;

/**
 * Interface for preference store in the ui
 */
public interface IPreferenceStore {
    /**
     * Persistently saves the given argument
     *
     * @param arg
     */
    void save(Arguments arg);

    /**
     * Gets the arguemnt object from the persistence store
     *
     * @return the arg or null if not set or could not be read
     */
    Arguments get();
}
