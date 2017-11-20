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

import java.io.*;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * Simple persistence store for UI
 */
public class SerializePreferenceStore implements IPreferenceStore {
    public static final String ARGS_KEY = "args";
    private Preferences prefs;

    public SerializePreferenceStore() {
        this.prefs = Preferences.userNodeForPackage(GUI.class);
    }

    @Override
    public void save(Arguments arg) {
        try {
            prefs.put(ARGS_KEY, serialize(arg));
        } catch (Exception e) {
            prefs.remove(ARGS_KEY);
            e.printStackTrace();
        }
    }

    @Override
    public Arguments get() {
        try {
            String saved = prefs.get(ARGS_KEY, null);
            if (saved == null) {
                return null;
            }

            Object out = unserialize(saved);
            return (Arguments) out;
        } catch (Exception e) {
            prefs.remove(ARGS_KEY);
            e.printStackTrace();
            return null;
        }
    }

    private static String serialize(Serializable obj) throws IOException {
        ObjectOutput out = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    private static Object unserialize(String base64Obj) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64Obj)))) {
            return in.readObject();
        }
    }
}
