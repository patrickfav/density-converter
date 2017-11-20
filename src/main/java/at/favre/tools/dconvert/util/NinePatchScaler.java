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

package at.favre.tools.dconvert.util;

import at.favre.tools.dconvert.converters.scaling.ScaleAlgorithm;
import at.favre.tools.dconvert.converters.scaling.ThumbnailnatorProgressiveAlgorithm;
import at.favre.tools.dconvert.exceptions.NinePatchException;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Scales 9-patches correctly, keeping the 1px border intact.
 * <p>
 * Adapted from <a href="https://github.com/redwarp/9-Patch-Resizer/blob/develop/src/net/redwarp/tool/resizer/worker/ImageScaler.java">Github</a>
 *
 * @author Redwarp, pfavre
 */
public class NinePatchScaler {

    private ScaleAlgorithm algorithm;
    private ScaleAlgorithm borderScalerAlgorithm = new ThumbnailnatorProgressiveAlgorithm(RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    public BufferedImage scale(BufferedImage inputImage, Dimension dimensions, ScaleAlgorithm algorithm) throws NinePatchException {
        this.algorithm = algorithm;
        BufferedImage trimmedImage = this.trim9PBorder(inputImage);

        trimmedImage = algorithm.scale(trimmedImage, dimensions.width, dimensions.height);

        BufferedImage borderImage;

        int w = trimmedImage.getWidth();
        int h = trimmedImage.getHeight();

        borderImage = this.generateBordersImage(inputImage, w, h);

        int[] rgbArray = new int[w * h];
        trimmedImage.getRGB(0, 0, w, h, rgbArray, 0, w);
        borderImage.setRGB(1, 1, w, h, rgbArray, 0, w);
        rgbArray = null;

        return borderImage;
    }

    private BufferedImage trim9PBorder(BufferedImage inputImage) {
        BufferedImage trimedImage = new BufferedImage(inputImage.getWidth() - 2, inputImage.getHeight() - 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = trimedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, trimedImage.getWidth(), trimedImage.getHeight(), 1, 1, inputImage.getWidth() - 1, inputImage.getHeight() - 1, null);
        g.dispose();
        return trimedImage;
    }

    private void enforceBorderColors(BufferedImage inputImage) {
        Graphics2D g = inputImage.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(1, 1, inputImage.getWidth() - 2, inputImage.getHeight() - 2);
        g.dispose();
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();
        int[] rgb = new int[w * h];

        inputImage.getRGB(0, 0, w, h, rgb, 0, w);

        for (int i = 0; i < rgb.length; i++) {
            if ((0xff000000 & rgb[i]) != 0) {
                rgb[i] = 0xff000000;
            }
        }
        inputImage.setRGB(0, 0, w, h, rgb, 0, w);
        inputImage.setRGB(0, 0, 0x0);
        inputImage.setRGB(0, h - 1, 0x0);
        inputImage.setRGB(w - 1, h - 1, 0x0);
        inputImage.setRGB(w - 1, 0, 0x0);
    }

    private BufferedImage generateBordersImage(BufferedImage source, int trimedWidth, int trimedHeight) throws NinePatchException {
        BufferedImage finalBorder = new BufferedImage(trimedWidth + 2, trimedHeight + 2, BufferedImage.TYPE_INT_ARGB);
        int cutW = source.getWidth() - 2;
        int cutH = source.getHeight() - 2;
        // left border
        BufferedImage leftBorder = new BufferedImage(1, cutH, BufferedImage.TYPE_INT_ARGB);
        leftBorder.setRGB(0, 0, 1, cutH, source.getRGB(0, 1, 1, cutH, null, 0, 1), 0, 1);
        this.verifyBorderImage(leftBorder);
        leftBorder = this.resizeBorder(leftBorder, 1, trimedHeight);
        finalBorder.setRGB(0, 1, 1, trimedHeight, leftBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1), 0, 1);
        // right border
        BufferedImage rightBorder = new BufferedImage(1, cutH, BufferedImage.TYPE_INT_ARGB);
        rightBorder.setRGB(0, 0, 1, cutH, source.getRGB(cutW + 1, 1, 1, cutH, null, 0, 1), 0, 1);
        this.verifyBorderImage(rightBorder);
        rightBorder = this.resizeBorder(rightBorder, 1, trimedHeight);
        finalBorder.setRGB(trimedWidth + 1, 1, 1, trimedHeight, rightBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1), 0, 1);
        // top border
        BufferedImage topBorder = new BufferedImage(cutW, 1, BufferedImage.TYPE_INT_ARGB);
        topBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, 0, cutW, 1, null, 0, cutW), 0, cutW);
        this.verifyBorderImage(topBorder);
        topBorder = this.resizeBorder(topBorder, trimedWidth, 1);
        finalBorder.setRGB(1, 0, trimedWidth, 1, topBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth), 0, trimedWidth);
        // bottom border
        BufferedImage bottomBorder = new BufferedImage(cutW, 1, BufferedImage.TYPE_INT_ARGB);
        bottomBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, cutH + 1, cutW, 1, null, 0, cutW), 0, cutW);
        this.verifyBorderImage(bottomBorder);
        bottomBorder = this.resizeBorder(bottomBorder, trimedWidth, 1);
        finalBorder.setRGB(1, trimedHeight + 1, trimedWidth, 1, bottomBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth), 0, trimedWidth);

        return finalBorder;
    }

    private BufferedImage resizeBorder(final BufferedImage border, int targetWidth, int targetHeight) {
        if (targetWidth > border.getWidth()
                || targetHeight > border.getHeight()) {
            BufferedImage endImage = borderScalerAlgorithm.scale(border, targetWidth, targetHeight);
            this.enforceBorderColors(endImage);
            return endImage;
        }

        int w = border.getWidth();
        int h = border.getHeight();
        int[] data = border.getRGB(0, 0, w, h, null, 0, w);
        int[] newData = new int[targetWidth * targetHeight];

        float widthRatio = (float) Math.max(targetWidth - 1, 1)
                / (float) Math.max(w - 1, 1);
        float heightRatio = (float) Math.max(targetHeight - 1, 1)
                / (float) Math.max(h - 1, 1);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((0xff000000 & data[y * w + x]) != 0) {
                    int newX = Math.min(Math.round(x * widthRatio), targetWidth - 1);
                    int newY = Math.min(Math.round(y * heightRatio), targetHeight - 1);

                    newData[newY * targetWidth + newX] = data[y * w + x];
                }
            }
        }

        BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, targetWidth, targetHeight, newData, 0, targetWidth);

        return img;
    }

    private void verifyBorderImage(BufferedImage border)
            throws NinePatchException {
        int[] rgb = border.getRGB(0, 0, border.getWidth(), border.getHeight(),
                null, 0, border.getWidth());
        for (int i = 0; i < rgb.length; i++) {
            if ((0xff000000 & rgb[i]) != 0) {
                if (rgb[i] != 0xff000000 && rgb[i] != 0xffff0000) {
                    throw new NinePatchException();
                }
            }
        }
    }
}
