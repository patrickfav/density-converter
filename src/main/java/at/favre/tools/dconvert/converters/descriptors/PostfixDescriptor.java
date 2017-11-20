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

package at.favre.tools.dconvert.converters.descriptors;

/**
 * Created by PatrickF on 18.03.2016.
 */
public class PostfixDescriptor extends DensityDescriptor {
    public final String postFix;

    public PostfixDescriptor(float scale, String name, String postFix) {
        super(scale, name);
        this.postFix = postFix;
    }
}
