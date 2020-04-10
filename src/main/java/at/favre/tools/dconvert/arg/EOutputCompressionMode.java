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

import java.util.ResourceBundle;

/**
 * Different output compression modes
 */
public enum EOutputCompressionMode {
    SAME_AS_INPUT_PREF_PNG("enum.outcomp.SAME_AS_INPUT_PREF_PNG"),
    SAME_AS_INPUT_STRICT("enum.outcomp.SAME_AS_INPUT_STRICT"),
    AS_JPG("enum.outcomp.AS_JPG"),
    AS_PNG("enum.outcomp.AS_PNG"),
    AS_GIF("enum.outcomp.AS_GIF"),
    AS_BMP("enum.outcomp.AS_BMP"),
    AS_JPG_AND_PNG("enum.outcomp.AS_JPG_AND_PNG");

    public final String rbKey;

    EOutputCompressionMode(String rbKey) {
        this.rbKey = rbKey;
    }

    public static EOutputCompressionMode getFromString(String i18nString, ResourceBundle bundle) {
        for (EOutputCompressionMode eOutputCompressionMode : EOutputCompressionMode.values()) {
            if (bundle.getString(eOutputCompressionMode.rbKey).equals(i18nString)) {
                return eOutputCompressionMode;
            }
        }
        return null;
    }
}
