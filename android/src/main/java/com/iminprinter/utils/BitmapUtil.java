package com.iminprinter.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BitmapUtil {
    // 图片转换为bitmap
    public static Bitmap setPictureToBitmap(String loadPath) {
        Bitmap bitmap = null;
        if(!"".equals(loadPath)){
            FileInputStream is= null;
            try {
                is = new FileInputStream(loadPath);
                bitmap = BitmapFactory.decodeFileDescriptor(is.getFD());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static byte[] PrintDiskImagefile(Bitmap bitmap) {
        byte[] bytes;


//        if (!strPath.substring(strPath.toLowerCase().indexOf(".") + 1).equals("bmp")) {
//            bitmap = convertToBlackWhite(bitmap);
//            int width = bitmap.getWidth();
//            int  heigh = bitmap.getHeight();
//            int iDataLen = width * heigh;
//            int[] pixels = new int[iDataLen];
//            bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
//            bytes = PrintDiskImagefile(pixels,width,heigh);
//        }else
//        {
        int width = bitmap.getWidth();
        int heigh = bitmap.getHeight();
        int iDataLen = width * heigh;
        int[] pixels = new int[iDataLen];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
        bytes = PrintDiskImagefile(pixels, width, heigh);

//        }

        return bytes;
    }

    public static byte[] PrintDiskImagefile(int[] pixels, int iWidth, int iHeight) {
        int iBw = iWidth / 8;
        int iMod = iWidth % 8;
        if (iMod > 0)
            iBw = iBw + 1;
        int iDataLen = iBw * iHeight;
        byte[] bCmd = new byte[iDataLen + 8];
        int iIndex = 0;
        bCmd[iIndex++] = 0x1D;
        bCmd[iIndex++] = 0x76;
        bCmd[iIndex++] = 0x30;
        bCmd[iIndex++] = 0x0;
        bCmd[iIndex++] = (byte) iBw;
        bCmd[iIndex++] = (byte) (iBw >> 8);
        bCmd[iIndex++] = (byte) iHeight;
        bCmd[iIndex++] = (byte) (iHeight >> 8);

        int iValue1 = 0;
        int iValue2 = 0;
        int iRow = 0;
        int iCol = 0;
        int iW = 0;
        int iValue3 = 0;
        int iValue4 = 0;
        for (iRow = 0; iRow < iHeight; iRow++) {
            for (iCol = 0; iCol < iBw - 1; iCol++) {
                iValue2 = 0;

                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                //Log.d("dzm","=== iValue1 = " + iValue1);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x80;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x40;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x20;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x10;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x8;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x4;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x2;
                iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                if (iValue1 == 1)
                    iValue2 = iValue2 + 0x1;
                if (iValue3 < -1) // w1
                    iValue4 = iValue4 + 0x10;
                bCmd[iIndex++] = (byte) (iValue2);
            }
            iValue2 = 0;
            if (iValue4 > 0)      // w2
                iValue3 = 1;
            if (iMod == 0) {
                for (iCol = 8; iCol > iMod; iCol--) {
                    iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                    if (iValue1 == 1)
                        iValue2 = iValue2 + (1 << iCol);
                }
            } else {
                for (iCol = 0; iCol < iMod; iCol++) {
                    iValue1 = getPixelBlackWhiteValue(pixels[iW++]);
                    if (iValue1 == 1)
                        iValue2 = iValue2 + (1 << (8 - iCol));
                }
            }
            bCmd[iIndex++] = (byte) (iValue2);
        }
        return bCmd;
    }

    public static int getPixelBlackWhiteValue(int pixel){
        int alpha = pixel >> 24 & 0xFF;
        int red = pixel >> 16 & 0xFF;
        int green = pixel >> 8 & 0xFF;
        int blue = pixel & 0xFF;
        //>180的都认为是白色,Value 0为白色,1为黑色
        int value = alpha == 0 ? 0 : (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11) > 180 ? 0 : 1;
        return value;
    }


    /**
     * 转换Bitmap为黑白图片(Floyd-Steinberg)，图片颜色只有0x000000和0xffffff两种
     *
     * @param img   需要转换的图片
     * @param shake 是否抖动模式
     */
    public static Bitmap threshold(Bitmap img, boolean shake) {
        //转灰色图
        int height = img.getHeight();
        int width = img.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, img.getConfig());
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(img, 0.0F, 0.0F, paint);
        img = bmpGrayscale;

        width = img.getWidth();
        height = img.getHeight();
        //通过位图的大小创建像素点数组
        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                gray[width * i + j] = red;
            }
        }
        int e = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = 0xffffffff;
                    e = g - 255;

                } else {
                    pixels[width * i + j] = 0xff000000;
                    e = g - 0;
                }
                //处理抖动
                if (shake) {
                    if (j < width - 1 && i < height - 1) {
                        //右边像素处理
                        gray[width * i + j + 1] += 7 * e / 16;
                        //下
                        gray[width * (i + 1) + j] += 5 * e / 16;
                        //右下
                        gray[width * (i + 1) + j + 1] += e / 16;
                        //左下
                        if (j > 0) {
                            gray[width * (i + 1) + j - 1] += 3 * e / 16;
                        }
                    } else if (j == width - 1 && i < height - 1) {
                        //下方像素处理
                        gray[width * (i + 1) + j] += 5 * e / 16;
                    } else if (j < width - 1 && i == height - 1) {
                        //右边像素处理
                        gray[width * (i) + j + 1] += 7 * e / 16;
                    }
                }
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, img.getConfig());
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    /**
     * 把图片转换为可打印的数据格式
     *
     * @param bitmap 图片对象
     */
    public static byte[] toPrintData(Bitmap bitmap) {
        int columnSize = (int) Math.ceil(bitmap.getWidth() / 8.0);
        byte[] data = new byte[columnSize * bitmap.getHeight()];
        int dataIndex = 0;
        int dataBit, lineIndex;
        int A, R, G, B, x, y;
        int pixelColor;
        int height = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        int[] pixels = new int[bitmapWidth * height];
        bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, height);
        for (y = 0; y < height; y++) {
            for (x = 0; x < columnSize; x++) {
                for (dataBit = 0; dataBit < 8; dataBit++) {
                    lineIndex = x * 8 + dataBit;
                    if (lineIndex < bitmapWidth) {
                        pixelColor = pixels[y * bitmapWidth + lineIndex];
                        R = Color.red(pixelColor);
                        if (R < 200) {
                            data[dataIndex] = (byte) ((data[dataIndex] << 1) | 0x01);
                        } else {
                            data[dataIndex] = (byte) ((data[dataIndex] << 1) & 0xFE);
                        }
                    }
                }
                dataIndex++;
            }
        }
        return data;
    }
    public static byte[] getCompressedBinaryzationBytes(Bitmap bitmap, int threshold, boolean reverse) {
        if (bitmap == null) {
            return null;
        } else {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int lineBytes = (width - 1) / 8 + 1;
            byte[] data = new byte[lineBytes * height];
            byte[] imageBytes = getBinaryzationBytes(bitmap, threshold, reverse);

            for(int i = 0; i < height; ++i) {
                for(int j = 0; j < lineBytes; ++j) {
                    byte unit = 0;

                    for(int k = 0; k < 8; ++k) {
                        if ((j << 3) + k < width) {
                            byte pixel = imageBytes[i * width + (j << 3) + k];
                            unit = (byte)(unit | (pixel & 1) << 7 - k);
                        }
                    }

                    data[i * lineBytes + j] = unit;
                }
            }

            return data;
        }
    }
    /**
     * Bitmap转单色位图数据
     * @param bitmap    源图
     * @param threshold 灰度值
     * @param reverse   反显
     * @return
     */
    private static byte[] getBinaryzationBytes(Bitmap bitmap, int threshold, boolean reverse) {
        if (bitmap == null) {
            return null;
        } else {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            byte[] data = new byte[width * height];
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);//取得BITMAP的所有像素点

            for(int i = 0; i < height; ++i) {
                for(int j = 0; j < width; ++j) {
                    int color =pixels[i*width+j];
//                    int red = (color & 16711680) >> 16;
//                    int green = (color & '\uff00') >> 8;
//                    int blue = color & 255;
//                    int gray = (int)(0.299D * (double)red + 0.587D * (double)green + 0.114D * (double)blue);
                    int gray = (int) ((0.299d * ((color & 0xFF0000) >> 16)) + (0.587d * ((color & 0xFF00) >> 8)) + (0.114d * (color & 0xFF)));
                    byte y = (byte)(reverse ? 1 : 0);
                    byte n = (byte)(reverse ? 0 : 1);
                    data[i * width + j] = gray < threshold ? y : n;
                }
            }

            return data;
        }
    }
    public static List<Bitmap> cutBitmap(Bitmap bitmap, int maxHeight) {
        List<Bitmap> list = new ArrayList<>();
        if (bitmap == null || maxHeight < 1) {
            return list;
        }
        for (int startHeight = 0; startHeight < bitmap.getHeight(); startHeight += maxHeight) {
            int endHeight = startHeight + maxHeight;
            if (endHeight > bitmap.getHeight()) {
                endHeight = bitmap.getHeight();
            }
            Bitmap sub = Bitmap.createBitmap(bitmap, 0, startHeight, bitmap.getWidth(), endHeight - startHeight);
            list.add(sub);
        }
        return list;
    }


    public static boolean save(Bitmap bitmap, File file) {
        if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() < 1 || bitmap.getHeight() < 1) {
            return false;
        }
        try {
            FileOutputStream fo = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 缩放图片
     *
     * @param bitmap    源图片
     * @param dstWidth  目标图宽度
     * @param dstHeight 目标图高度
     * @return 缩放后的图片
     */
    public static Bitmap resize(Bitmap bitmap, int dstWidth, int dstHeight) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() == dstWidth && bitmap.getHeight() == dstHeight) {
            return bitmap;
        }
        if (dstWidth < 1 || dstHeight < 1) {
            throw new IllegalArgumentException("Bitmap output width and height must greater than 1");
        }
        float scaleX = ((float) dstWidth) / bitmap.getWidth();
        float scaleY = ((float) dstHeight) / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 生成条形码（不支持中文）
     *
     * @param content
     * @return
     */
    public static Bitmap createBarcode(String content, int w, int h) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, w, h);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xff000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
