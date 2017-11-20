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

package at.favre.tools.dconvert.converters.scaling;

import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.AbstractResizer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

/**
 * Algorithms from Thumbnailnator
 */
public class ThumbnailnatorProgressiveAlgorithm implements ScaleAlgorithm {

    private Object interpolationValue;

    public ThumbnailnatorProgressiveAlgorithm(Object interpolationValue) {
        this.interpolationValue = interpolationValue;
    }

    @Override
    public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        return new FixedSizeThumbnailMaker(dWidth, dHeight, false, true)
                .resizer(new ProgressiveResizer(interpolationValue)).make(imageToScale);
    }

    public static class ProgressiveResizer extends AbstractResizer {
        public ProgressiveResizer(Object interpolationValue) {
            this(interpolationValue, Collections.emptyMap());
        }

        public ProgressiveResizer(Object interpolationValue, Map<RenderingHints.Key, Object> hints) {
            super(interpolationValue, hints);
            checkArg(interpolationValue);
        }

        private void checkArg(Object interpolationValue) {
            if (interpolationValue != RenderingHints.VALUE_INTERPOLATION_BICUBIC &&
                    interpolationValue != RenderingHints.VALUE_INTERPOLATION_BILINEAR &&
                    interpolationValue != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
                throw new IllegalArgumentException("wrong argument passed muts be one of RenderingHints.VALUE_INTERPOLATION_BICUBIC, " +
                        "RenderingHints.VALUE_INTERPOLATION_BILINEAR or RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR");
        }

        @Override
        public void resize(BufferedImage srcImage, BufferedImage destImage)
                throws NullPointerException {
            super.performChecks(srcImage, destImage);

            int currentWidth = srcImage.getWidth();
            int currentHeight = srcImage.getHeight();

            final int targetWidth = destImage.getWidth();
            final int targetHeight = destImage.getHeight();

            // If multi-step downscaling is not required, perform one-step.
            if ((targetWidth * 2 >= currentWidth) && (targetHeight * 2 >= currentHeight)) {
                super.resize(srcImage, destImage);
                return;
            }

            // Temporary image used for in-place resizing of image.
            BufferedImage tempImage = new BufferedImage(
                    currentWidth,
                    currentHeight,
                    destImage.getType()
            );

            Graphics2D g = tempImage.createGraphics();
            g.setRenderingHints(RENDERING_HINTS);
            g.setComposite(AlphaComposite.Src);

            /*
             * Determine the size of the first resize step should be.
             * 1) Beginning from the target size
             * 2) Increase each dimension by 2
             * 3) Until reaching the original size
             */

            int startWidth = targetWidth;
            int startHeight = targetHeight;

            while (startWidth < currentWidth && startHeight < currentHeight) {
                startWidth *= 2;
                startHeight *= 2;
            }

            currentWidth = startWidth / 2;
            currentHeight = startHeight / 2;

            // Perform first resize step.
            g.drawImage(srcImage, 0, 0, currentWidth, currentHeight, null);

            // Perform an in-place progressive bilinear resize.
            while ((currentWidth >= targetWidth * 2) && (currentHeight >= targetHeight * 2)) {
                currentWidth /= 2;
                currentHeight /= 2;

                if (currentWidth < targetWidth) {
                    currentWidth = targetWidth;
                }
                if (currentHeight < targetHeight) {
                    currentHeight = targetHeight;
                }

                g.drawImage(
                        tempImage,
                        0, 0, currentWidth, currentHeight,
                        0, 0, currentWidth * 2, currentHeight * 2,
                        null
                );
            }

            g.dispose();

            // Draw the resized image onto the destination image.
            Graphics2D destg = destImage.createGraphics();
            destg.drawImage(tempImage, 0, 0, targetWidth, targetHeight, 0, 0, currentWidth, currentHeight, null);
            destg.dispose();
        }
    }

    @Override
    public String toString() {
        return "ThumbnailnatorProgressiveAlgorithm{" +
                "interpolationValue=" + interpolationValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThumbnailnatorProgressiveAlgorithm that = (ThumbnailnatorProgressiveAlgorithm) o;

        return interpolationValue != null ? interpolationValue.equals(that.interpolationValue) : that.interpolationValue == null;

    }

    @Override
    public int hashCode() {
        return interpolationValue != null ? interpolationValue.hashCode() : 0;
    }
}
