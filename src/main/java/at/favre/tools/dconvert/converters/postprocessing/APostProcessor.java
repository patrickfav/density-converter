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

package at.favre.tools.dconvert.converters.postprocessing;

import at.favre.tools.dconvert.converters.Result;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared code among {@link IPostProcessor}.
 * <p>
 * This helps to synchronize processors: will create a lock for each input file,
 * so that only 1 processor can process a file at a time
 */
public abstract class APostProcessor implements IPostProcessor {
    private static Map<File, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private static ReentrantLock administrationLock = new ReentrantLock(true);

    @Override
    public Result process(File rawFile, boolean keepOriginal) {
        try {

            administrationLock.lock();
            if (!lockMap.containsKey(rawFile)) {
                lockMap.put(rawFile, new ReentrantLock(true));
            }

            administrationLock.unlock();

            lockMap.get(rawFile).lock();

            return synchronizedProcess(rawFile, keepOriginal);
        } finally {
            lockMap.get(rawFile).unlock();
        }
    }

    /**
     * This is the thread safe version of {@link #process(File, boolean)}
     *
     * @param rawFile
     * @param keepOriginal
     * @return
     */
    protected abstract Result synchronizedProcess(File rawFile, boolean keepOriginal);
}
