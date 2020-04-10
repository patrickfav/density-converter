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
 * Supported image types
 */
public enum ImageType {

    JPG(ECompression.JPG, ECompression.JPG, new String[]{"jpeg", "jpg"}, true),
    PNG(ECompression.PNG, ECompression.PNG, new String[]{"png"}, true),
    GIF(ECompression.GIF, ECompression.PNG, new String[]{"gif"}, true),
    SVG(ECompression.PNG, ECompression.PNG, new String[]{"svg"}, true),
    PSD(ECompression.PNG, ECompression.PNG, new String[]{"psd"}, true), //adobe photoshop
    TIFF(ECompression.TIFF, ECompression.PNG, new String[]{"tif", "tiff"}, true), //Tagged Image File Format
    BMP(ECompression.BMP, ECompression.PNG, new String[]{"bmp"}, true); // bitmap image file or device independent bitmap (DIB)

    public final ECompression outCompressionStrict;
    public final ECompression outCompressionCompat;
    public final String[] extensions;
    public final boolean supportRead;

    ImageType(ECompression outCompressionStrict, ECompression outCompressionCompat, String[] extensions, boolean supportRead) {
        this.outCompressionStrict = outCompressionStrict;
        this.outCompressionCompat = outCompressionCompat;
        this.extensions = extensions;
        this.supportRead = supportRead;
    }

    /**
     * Supported image compression types
     */
    public enum ECompression {
        JPG(false, "jpg"), PNG(true, "png"), GIF(true, "gif"), TIFF(false, "tif"), BMP(false, "bmp");

        public final boolean hasTransparency;
        public final String extension;

        ECompression(boolean hasTransparency, String extension) {
            this.hasTransparency = hasTransparency;
            this.extension = extension;
        }
    }
}
