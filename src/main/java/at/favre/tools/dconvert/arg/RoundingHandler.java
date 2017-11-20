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

/**
 * Defines how float numbers will be rounded
 */
public class RoundingHandler {
    public enum Strategy {
        ROUND_HALF_UP,
        CEIL,
        FLOOR
    }

    private final Strategy strategy;

    public RoundingHandler(Strategy strategy) {
        this.strategy = strategy;
    }

    public long round(double value) {
        switch (strategy) {
            case CEIL:
                return Math.max(1, (long) Math.ceil(value));
            case FLOOR:
                return Math.max(1, (long) Math.floor(value));
            default:
            case ROUND_HALF_UP:
                return Math.max(1, Math.round(value));
        }
    }
}
