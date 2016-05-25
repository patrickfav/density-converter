package at.favre.tools.dconvert.util;

/*

   Copyright 2001-2004  The Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
/*
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.renderable.PaintRable;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.Any2LsRGBRed;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.TranslateRed;
*/

/**
 * Set of utility methods for Graphics.
 * These generally bypass broken methods in Java2D or provide tweaked
 * implementations.
 *
 * @author <a href="mailto:Thomas.DeWeeese@Kodak.com">Thomas DeWeese</a>
 * @version $Id: GraphicsUtil.java,v 1.36 2005/03/27 08:58:32 cam Exp $
 */
public class GraphicsUtil {

    public static AffineTransform IDENTITY = new AffineTransform();

    /**
     * Standard prebuilt Linear_sRGB color model with no alpha.
     */
    public final static ColorModel Linear_sRGB =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 24,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0x0, false,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with premultiplied alpha.
     */
    public final static ColorModel Linear_sRGB_Pre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, true,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with unpremultiplied alpha.
     */
    public final static ColorModel Linear_sRGB_Unpre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, false,
                    DataBuffer.TYPE_INT);

    /**
     * Standard prebuilt sRGB color model with no alpha.
     */
    public final static ColorModel sRGB =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 24,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0x0, false,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with premultiplied alpha.
     */
    public final static ColorModel sRGB_Pre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, true,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with unpremultiplied alpha.
     */
    public final static ColorModel sRGB_Unpre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, false,
                    DataBuffer.TYPE_INT);

    /**
     * Method that returns either Linear_sRGB_Pre or Linear_sRGB_UnPre
     * based on premult flag.
     * @param premult True if the ColorModel should have premultiplied alpha.
     * @return        a ColorMdoel with Linear sRGB colorSpace and
     *                the alpha channel set in accordance with
     *                <tt>premult</tt>
     */
    public static ColorModel makeLinear_sRGBCM(boolean premult) {
        if (premult)
            return Linear_sRGB_Pre;
        return Linear_sRGB_Unpre;
    }

    /**
     * Constructs a BufferedImage with a linear sRGB colorModel, and alpha.
     * @param width   The desired width of the BufferedImage
     * @param height  The desired height of the BufferedImage
     * @param premult The desired state of alpha premultiplied
     * @return        The requested BufferedImage.
     */
    public static BufferedImage makeLinearBufferedImage(int width,
                                                        int height,
                                                        boolean premult) {
        ColorModel cm = makeLinear_sRGBCM(premult);
        WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
        return new BufferedImage(cm, wr, premult, null);
    }


    /**
     * Creates a new raster that has a <b>copy</b> of the data in
     * <tt>ras</tt>.  This is highly optimized for speed.  There is
     * no provision for changing any aspect of the SampleModel.
     *
     * This method should be used when you need to change the contents
     * of a Raster that you do not "own" (ie the result of a
     * <tt>getData</tt> call).
     * @param ras The Raster to copy.
     * @return    A writable copy of <tt>ras</tt>
     */
    public static WritableRaster copyRaster(Raster ras) {
        return copyRaster(ras, ras.getMinX(), ras.getMinY());
    }


    /**
     * Creates a new raster that has a <b>copy</b> of the data in
     * <tt>ras</tt>.  This is highly optimized for speed.  There is
     * no provision for changing any aspect of the SampleModel.
     * However you can specify a new location for the returned raster.
     *
     * This method should be used when you need to change the contents
     * of a Raster that you do not "own" (ie the result of a
     * <tt>getData</tt> call).
     *
     * @param ras The Raster to copy.
     *
     * @param minX The x location for the upper left corner of the
     *             returned WritableRaster.
     *
     * @param minY The y location for the upper left corner of the
     *             returned WritableRaster.
     *
     * @return    A writable copy of <tt>ras</tt>
     */
    public static WritableRaster copyRaster(Raster ras, int minX, int minY) {
        WritableRaster ret = Raster.createWritableRaster
                (ras.getSampleModel(),
                        new Point(0,0));
        ret = ret.createWritableChild
                (ras.getMinX()-ras.getSampleModelTranslateX(),
                        ras.getMinY()-ras.getSampleModelTranslateY(),
                        ras.getWidth(), ras.getHeight(),
                        minX, minY, null);

        // Use System.arraycopy to copy the data between the two...
        DataBuffer srcDB = ras.getDataBuffer();
        DataBuffer retDB = ret.getDataBuffer();
        if (srcDB.getDataType() != retDB.getDataType()) {
            throw new IllegalArgumentException
                    ("New DataBuffer doesn't match original");
        }
        int len   = srcDB.getSize();
        int banks = srcDB.getNumBanks();
        int [] offsets = srcDB.getOffsets();
        for (int b=0; b< banks; b++) {
            switch (srcDB.getDataType()) {
                case DataBuffer.TYPE_BYTE: {
                    DataBufferByte srcDBT = (DataBufferByte)srcDB;
                    DataBufferByte retDBT = (DataBufferByte)retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                }
                case DataBuffer.TYPE_INT: {
                    DataBufferInt srcDBT = (DataBufferInt)srcDB;
                    DataBufferInt retDBT = (DataBufferInt)retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                }
                case DataBuffer.TYPE_SHORT: {
                    DataBufferShort srcDBT = (DataBufferShort)srcDB;
                    DataBufferShort retDBT = (DataBufferShort)retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                }
                case DataBuffer.TYPE_USHORT: {
                    DataBufferUShort srcDBT = (DataBufferUShort)srcDB;
                    DataBufferUShort retDBT = (DataBufferUShort)retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                }
            }
        }

        return ret;
    }

    /**
     * Coerces <tt>ras</tt> to be writable.  The returned Raster continues to
     * reference the DataBuffer from ras, so modifications to the returned
     * WritableRaster will be seen in ras.<p>
     *
     * This method should only be used if you need a WritableRaster due to
     * an interface (such as to construct a BufferedImage), but have no
     * intention of modifying the contents of the returned Raster.  If
     * you have any doubt about other users of the data in <tt>ras</tt>,
     * use copyRaster (above).
     * @param ras The raster to make writable.
     * @return    A Writable version of ras (shares DataBuffer with
     *            <tt>ras</tt>).
     */
    public static WritableRaster makeRasterWritable(Raster ras) {
        return makeRasterWritable(ras, ras.getMinX(), ras.getMinY());
    }

    /**
     * Coerces <tt>ras</tt> to be writable.  The returned Raster continues to
     * reference the DataBuffer from ras, so modifications to the returned
     * WritableRaster will be seen in ras.<p>
     *
     * You can specify a new location for the returned WritableRaster, this
     * is especially useful for constructing BufferedImages which require
     * the Raster to be at (0,0).
     *
     * This method should only be used if you need a WritableRaster due to
     * an interface (such as to construct a BufferedImage), but have no
     * intention of modifying the contents of the returned Raster.  If
     * you have any doubt about other users of the data in <tt>ras</tt>,
     * use copyRaster (above).
     *
     * @param ras The raster to make writable.
     *
     * @param minX The x location for the upper left corner of the
     *             returned WritableRaster.
     *
     * @param minY The y location for the upper left corner of the
     *             returned WritableRaster.
     *
     * @return A Writable version of <tT>ras</tt> with it's upper left
     *         hand coordinate set to minX, minY (shares it's DataBuffer
     *         with <tt>ras</tt>).
     */
    public static WritableRaster makeRasterWritable(Raster ras,
                                                    int minX, int minY) {
        WritableRaster ret = Raster.createWritableRaster
                (ras.getSampleModel(),
                        ras.getDataBuffer(),
                        new Point(0,0));
        ret = ret.createWritableChild
                (ras.getMinX()-ras.getSampleModelTranslateX(),
                        ras.getMinY()-ras.getSampleModelTranslateY(),
                        ras.getWidth(), ras.getHeight(),
                        minX, minY, null);
        return ret;
    }

    /**
     * Create a new ColorModel with it's alpha premultiplied state matching
     * newAlphaPreMult.
     * @param cm The ColorModel to change the alpha premult state of.
     * @param newAlphaPreMult The new state of alpha premult.
     * @return   A new colorModel that has isAlphaPremultiplied()
     *           equal to newAlphaPreMult.
     */
    public static ColorModel
    coerceColorModel(ColorModel cm, boolean newAlphaPreMult) {
        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            return cm;

        // Easiest way to build proper colormodel for new Alpha state...
        // Eventually this should switch on known ColorModel types and
        // only fall back on this hack when the CM type is unknown.
        WritableRaster wr = cm.createCompatibleWritableRaster(1,1);
        return cm.coerceData(wr, newAlphaPreMult);
    }

    /**
     * Coerces data within a bufferedImage to match newAlphaPreMult,
     * Note that this can not change the colormodel of bi so you
     *
     * @param wr The raster to change the state of.
     * @param cm The colormodel currently associated with data in wr.
     * @param newAlphaPreMult The desired state of alpha Premult for raster.
     * @return A new colormodel that matches newAlphaPreMult.
     */
    public static ColorModel
    coerceData(WritableRaster wr, ColorModel cm, boolean newAlphaPreMult) {

        // System.out.println("CoerceData: " + cm.isAlphaPremultiplied() +
        //                    " Out: " + newAlphaPreMult);
        if (cm.hasAlpha()== false)
            // Nothing to do no alpha channel
            return cm;

        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            // nothing to do alpha state matches...
            return cm;

        // System.out.println("CoerceData: " + wr.getSampleModel());

        if (newAlphaPreMult) {
            multiplyAlpha(wr);
        } else {
            divideAlpha(wr);
        }

        return coerceColorModel(cm, newAlphaPreMult);
    }

    public static void multiplyAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            mult_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            mult_INT_PACK_Data(wr);
        else {
            int [] pixel = null;
            int    bands = wr.getNumBands();
            float  norm = 1f/255f;
            int x0, x1, y0, y1, a, b;
            float alpha;
            x0 = wr.getMinX();
            x1 = x0+wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0+wr.getHeight();
            for (int y=y0; y<y1; y++)
                for (int x=x0; x<x1; x++) {
                    pixel = wr.getPixel(x,y,pixel);
                    a = pixel[bands-1];
                    if ((a >= 0) && (a < 255)) {
                        alpha = a*norm;
                        for (b=0; b<bands-1; b++)
                            pixel[b] = (int)(pixel[b]*alpha+0.5f);
                        wr.setPixel(x,y,pixel);
                    }
                }
        }
    }

    public static void divideAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            divide_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            divide_INT_PACK_Data(wr);
        else {
            int x0, x1, y0, y1, a, b;
            float ialpha;
            int    bands = wr.getNumBands();
            int [] pixel = null;

            x0 = wr.getMinX();
            x1 = x0+wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0+wr.getHeight();
            for (int y=y0; y<y1; y++)
                for (int x=x0; x<x1; x++) {
                    pixel = wr.getPixel(x,y,pixel);
                    a = pixel[bands-1];
                    if ((a > 0) && (a < 255)) {
                        ialpha = 255/(float)a;
                        for (b=0; b<bands-1; b++)
                            pixel[b] = (int)(pixel[b]*ialpha+0.5f);
                        wr.setPixel(x,y,pixel);
                    }
                }
        }
    }

    public static boolean is_INT_PACK_Data(SampleModel sm,
                                           boolean requireAlpha) {
        // Check ColorModel is of type DirectColorModel
        if(!(sm instanceof SinglePixelPackedSampleModel)) return false;

        // Check transfer type
        if(sm.getDataType() != DataBuffer.TYPE_INT)       return false;

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)sm;

        int [] masks = sppsm.getBitMasks();
        if (masks.length == 3) {
            if (requireAlpha) return false;
        } else if (masks.length != 4)
            return false;

        if(masks[0] != 0x00ff0000) return false;
        if(masks[1] != 0x0000ff00) return false;
        if(masks[2] != 0x000000ff) return false;
        if ((masks.length == 4) &&
                (masks[3] != 0xff000000)) return false;

        return true;
    }

    public static boolean is_BYTE_COMP_Data(SampleModel sm) {
        // Check ColorModel is of type DirectColorModel
        if(!(sm instanceof ComponentSampleModel))    return false;

        // Check transfer type
        if(sm.getDataType() != DataBuffer.TYPE_BYTE) return false;

        return true;
    }

    protected static void divide_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Divide Int");

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                sppsm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                        wr.getMinY()-wr.getSampleModelTranslateY()));
        int pixel, a, aFP;
        // Access the pixel data array
        final int pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width;
            while (sp < end) {
                pixel = pixels[sp];
                a = pixel>>>24;
                if (a<=0) {
                    pixels[sp] = 0x00FFFFFF;
                }
                else if (a<255) {
                    aFP = (0x00FF0000/a);
                    pixels[sp] =
                            ((a << 24) |
                                    (((((pixel&0xFF0000)>>16)*aFP)&0xFF0000)    ) |
                                    (((((pixel&0x00FF00)>>8) *aFP)&0xFF0000)>>8 ) |
                                    (((((pixel&0x0000FF))    *aFP)&0xFF0000)>>16));
                }
                sp++;
            }
        }
    }

    protected static void mult_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                sppsm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                        wr.getMinY()-wr.getSampleModelTranslateY()));
        // Access the pixel data array
        final int pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width;
            while (sp < end) {
                int pixel = pixels[sp];
                int a = pixel>>>24;
                if ((a>=0) && (a<255)) {
                    pixels[sp] = ((a << 24) |
                            ((((pixel&0xFF0000)*a)>>8)&0xFF0000) |
                            ((((pixel&0x00FF00)*a)>>8)&0x00FF00) |
                            ((((pixel&0x0000FF)*a)>>8)&0x0000FF));
                }
                sp++;
            }
        }
    }


    protected static void divide_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride  = csm.getPixelStride();
        final int [] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                csm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                        wr.getMinY()-wr.getSampleModelTranslateY()));


        int a=0;
        int aOff = bandOff[bandOff.length-1];
        int bands = bandOff.length-1;
        int b, i;
        // Access the pixel data array
        final byte pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width*pixStride;
            while (sp < end) {
                a = pixels[sp+aOff]&0xFF;
                if (a==0) {
                    for (b=0; b<bands; b++)
                        pixels[sp+bandOff[b]] = (byte)0xFF;
                } else if (a<255) {
                    int aFP = (0x00FF0000/a);
                    for (b=0; b<bands; b++) {
                        i = sp+bandOff[b];
                        pixels[i] = (byte)(((pixels[i]&0xFF)*aFP)>>>16);
                    }
                }
                sp+=pixStride;
            }
        }
    }

    protected static void mult_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel)wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride  = csm.getPixelStride();
        final int [] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                csm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(),
                        wr.getMinY()-wr.getSampleModelTranslateY()));


        int a=0;
        int aOff = bandOff[bandOff.length-1];
        int bands = bandOff.length-1;
        int b, i;

        // Access the pixel data array
        final byte pixels[] = db.getBankData()[0];
        for (int y=0; y<wr.getHeight(); y++) {
            int sp = base + y*scanStride;
            final int end = sp + width*pixStride;
            while (sp < end) {
                a = pixels[sp+aOff]&0xFF;
                if (a!=0xFF)
                    for (b=0; b<bands; b++) {
                        i = sp+bandOff[b];
                        pixels[i] = (byte)(((pixels[i]&0xFF)*a)>>8);
                    }
                sp+=pixStride;
            }
        }
    }

/*
  This is skanky debugging code that might be useful in the future:

            if (count == 33) {
                String label = "sub [" + x + ", " + y + "]: ";
                org.ImageDisplay.showImage
                    (label, subBI);
                org.ImageDisplay.printImage
                    (label, subBI,
                     new Rectangle(75-iR.x, 90-iR.y, 32, 32));

            }


            // if ((count++ % 50) == 10)
            //     org.ImageDisplay.showImage("foo: ", subBI);


            Graphics2D realG2D = g2d;
            while (realG2D instanceof sun.java2d.ProxyGraphics2D) {
                realG2D = ((sun.java2d.ProxyGraphics2D)realG2D).getDelegate();
            }
            if (realG2D instanceof sun.awt.image.BufferedImageGraphics2D) {
                count++;
                if (count == 34) {
                    RenderedImage ri;
                    ri = ((sun.awt.image.BufferedImageGraphics2D)realG2D).bufImg;
                    // g2d.setComposite(SVGComposite.OVER);
                    // org.ImageDisplay.showImage("Bar: " + count, cr);
                    org.ImageDisplay.printImage("Bar: " + count, cr,
                                                new Rectangle(75, 90, 32, 32));

                    org.ImageDisplay.showImage ("Foo: " + count, ri);
                    org.ImageDisplay.printImage("Foo: " + count, ri,
                                                new Rectangle(75, 90, 32, 32));

                    System.out.println("BI: "   + ri);
                    System.out.println("BISM: " + ri.getSampleModel());
                    System.out.println("BICM: " + ri.getColorModel());
                    System.out.println("BICM class: " + ri.getColorModel().getClass());
                    System.out.println("BICS: " + ri.getColorModel().getColorSpace());
                    System.out.println
                        ("sRGB CS: " +
                         ColorSpace.getInstance(ColorSpace.CS_sRGB));
                    System.out.println("G2D info");
                    System.out.println("\tComposite: " + g2d.getComposite());
                    System.out.println("\tTransform" + g2d.getTransform());
                    java.awt.RenderingHints rh = g2d.getRenderingHints();
                    java.util.Set keys = rh.keySet();
                    java.util.Iterator iter = keys.iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();

                        System.out.println("\t" + o.toString() + " -> " +
                                           rh.get(o).toString());
                    }

                    ri = cr;
                    System.out.println("RI: "   + ri);
                    System.out.println("RISM: " + ri.getSampleModel());
                    System.out.println("RICM: " + ri.getColorModel());
                    System.out.println("RICM class: " + ri.getColorModel().getClass());
                    System.out.println("RICS: " + ri.getColorModel().getColorSpace());
                }
            }
*/

}
