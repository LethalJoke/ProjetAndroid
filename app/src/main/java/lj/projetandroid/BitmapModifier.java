package lj.projetandroid;

//Classe servant à implémenter toutes les méthodes de traitement d'image
// Implémenter méthodes static

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

/**
 * Responsible for all image modifications
 */
public abstract class BitmapModifier {

    /**
     * Scale the color between a maximum value and a minimum value
     * @param color float
     * @param min float
     * @param max float
     * @return color
     */
    private static float scaleColor(float color, float min, float max)
    {
        if(color > max)
            color = max;
        if(color < min)
            color = min;
        return color;
    }

    /**
     * Scale the color between a maximum value and a minimum value
     * @param color int
     * @param min int
     * @param max int
     * @return color
     */
    private static int scaleColor(int color, int min, int max)
    {
        if(color > max)
            color = max;
        if(color < min)
            color = min;
        return color;
    }

    /**
     * Change the luminosity
     * @param bmp Bitmap
     * @param value int
     * @return Bitmap
     */
    public static Bitmap changeLuminosity(Bitmap bmp, int value)
    {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int totalSize = bmpResult.getWidth() * bmpResult.getHeight();
        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());
        for(int i = 0; i < totalSize; i++)
        {
            int p = pixs[i];
            int red = Color.red(p) + value;
            int green = Color.green(p)+ value;
            int blue = Color.blue(p)+ value;
            int alpha = Color.alpha(p);

            red = scaleColor(red, 0, 255);
            blue = scaleColor(blue, 0, 255);
            green = scaleColor(green, 0, 255);

            pixs[i] = Color.argb(alpha,red,green,blue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Change the contrast
     * @param bmp Bitmap
     * @param value double
     * @return bitmap
     */
    public static Bitmap changeContrast(Bitmap bmp, double value)
    {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int totalSize = bmpResult.getWidth() * bmpResult.getHeight();
        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());
        for(int i = 0; i < totalSize; i++)
        {
            int p = pixs[i];
            int red = (int)(value * Color.red(p));
            int green = (int)(value * Color.green(p)) ;
            int blue = (int)(value * Color.blue(p));
            int alpha = Color.alpha(p);

            red = scaleColor(red, 0, 255);
            blue = scaleColor(blue, 0, 255);
            green = scaleColor(green, 0, 255);

            pixs[i] = Color.argb(alpha,red,green,blue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Change the tint
     * @param bmp Bitmap
     * @param mode int  0-> GreyScale 1-> Sepia
     * @return bitmap
     */
    public static Bitmap changeTint(Bitmap bmp, int mode)
    {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);

        int totalSize = bmpResult.getWidth() * bmpResult.getHeight();
        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());
        for(int i = 0; i < totalSize; i++)
        {
            int p = pixs[i];
            int red = Color.red(p);
            int green = Color.green(p);
            int blue = Color.blue(p);
            int alpha = Color.alpha(p);
            int newred, newblue, newgreen;
            if(mode == 0)
            {
                int grey = (int)((0.2126  * red) + (0.0722  * blue) + (0.7152 * green));
                newred = grey;
                newblue = grey;
                newgreen = grey;
            }
            else
            {
                newred = (int)((red * 0.393) + (green * 0.769) + (blue * 0.189));
                newgreen = (int)((red * 0.349) + (green * 0.686) + (blue * 0.168));
                newblue = (int)((red * 0.272) + (green * 0.534) + (blue * 0.131));
            }

            newred = scaleColor(newred, 0, 255);
            newblue = scaleColor(newblue, 0, 255);
            newgreen = scaleColor(newgreen, 0, 255);

            pixs[i] = Color.argb(alpha,newred,newgreen,newblue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Equalize the histogram
     * @param bmp Bitmap
     * @return bitmap
     */
    public static Bitmap equalizeHisto(Bitmap bmp)
    {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int totalSize = bmpResult.getWidth() * bmpResult.getHeight();
        int[] pixs = new int[totalSize];
        float[][] pixsHSV = new float[totalSize][3];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());
        float[] pixHSV = new float[3];
        int[] histo = new int[256];

        for(int i = 0; i < 256; i++)
            histo[i] = 0;

        for(int i = 0; i < totalSize; i++) {
            Color.colorToHSV(pixs[i], pixHSV);
            pixsHSV[i][0] = pixHSV[0]; //Hue
            pixsHSV[i][1] = pixHSV[1]; //Saturation
            pixsHSV[i][2] = pixHSV[2]; //Value

            histo[(int)(pixHSV[2] * 255)] += 1;
        }

        //Histo cumulé
        for(int i = 1; i < 256; i++)
            histo[i] += histo[i-1];


        for(int i = 0; i < totalSize; i++) {
            pixsHSV[i][2] =  scaleColor(((float)histo[(int)(pixsHSV[i][2] * 255)]) / totalSize,0.f,1.f);
            pixs[i] = Color.HSVToColor(pixsHSV[i]);
        }

        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Convolute the Bitmap using a matrice
     * @param bmp Bitmap
     * @param matrice float[][]
     * @param size int
     * @return bitmap
     */
    public static Bitmap convolution(Bitmap bmp, float[][] matrice, int size) {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int width = bmpResult.getWidth();
        int height = bmpResult.getHeight();
        int totalSize =  width * height;
        int marge = size / 2;
        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());
        for(int i = marge * width + marge; i < totalSize - (marge * width + marge); i++)
        {
            if(i % width <= marge)
                continue;
            if(i % width > width - marge) //?
                continue;
            int pix;
            int vRed = 0;
            int vGreen = 0;
            int vBlue = 0;
            for(int j = 0; j < size; j++)
                for(int k = 0; k < size; k++)
                {
                    //Traitement matrice
                     pix = pixs[(i - (marge * width + marge)) + j*width + k];
                     vRed +=(int)(Color.red(pix) * matrice[j][k]);
                     vGreen +=(int)(Color.green(pix) * matrice[j][k]);
                     vBlue +=(int)(Color.blue(pix) * matrice[j][k]);
                }
            pixs[i] = Color.argb(Color.alpha(pixs[i]), vRed, vGreen, vBlue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Rotate the image
     * @param bmp Bitmap
     * @param value float
     * @return bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bmp, float value)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(value);
        return Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * Detect Eges using Sobel's kernels
     * @param bmp Bitmap
     * @return bitmap
     */
    public static Bitmap sobelEdgeDetection(Bitmap bmp)
    {
        final float[][] sobel_y = {{-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}};
        final float[][] sobel_x = {{-1, 0,1},
                {-2, 0, 2},
                {-1, 0, 1}};

        Bitmap bmpResult = changeTint(bmp, 0);

        int width = bmpResult.getWidth();
        int height = bmpResult.getHeight();
        int totalSize =  width * height;

        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());

        int[] pixs_final = new int[totalSize];

        int index;
        double magX, magY;
        int pix, grey, mag, xn, yn;
        int mag_max = 0;

        for(int i = 1; i < width - 1; i++)
        {
            for(int j = 1; j < height - 1; j++)
            {
                magX = 0.0;
                magY = 0.0;

                //Boucle de traitement des matrices
                for(int a = 0; a < 3; a++)
                {
                    for(int b = 0; b < 3; b++)
                    {
                        xn = i + a - 1;
                        yn = j + b - 1;
                        index = xn + yn * width;

                        pix = pixs[index];
                        grey = Color.red(pix); //Car l'image est en niveau de gris

                        magX += grey * sobel_x[a][b];
                        magY += grey * sobel_y[a][b];
                    }
                }
                mag = (int)Math.sqrt(Math.pow(magX, 2) + Math.pow(magY, 2));
                if(mag > mag_max)
                    mag_max = mag;

                pixs_final[i + j * width] = mag;
            }
        }

        for(int i = 0; i < totalSize; i ++)
        {
            int value = (pixs_final[i] * 255) / mag_max;
            pixs_final[i] = Color.argb(Color.alpha(pixs[i]), value, value, value);
        }

        bmpResult.setPixels(pixs_final,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Inverse Black & White pixel + increase the outline
     * @param bmp Bitmap
     * @return bitmap
     */
    private static Bitmap increaseOutline(Bitmap bmp)
    {
        Bitmap bmpResult = bmp.copy(Bitmap.Config.ARGB_8888, true);
        int width = bmpResult.getWidth();
        int height = bmpResult.getHeight();
        int totalSize =  width * height;

        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());

        for(int i = 0; i<totalSize; i++)
        {
            int value = 255 - Color.red(pixs[i]);
            pixs[i] = Color.argb(Color.alpha(pixs[i]), value,value,value);
        }

        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    private static Bitmap combineBitmaps(Bitmap color, Bitmap oulines)
    {
        Bitmap bmpResult = color.copy(Bitmap.Config.ARGB_8888, true);
        int width = bmpResult.getWidth();
        int height = bmpResult.getHeight();
        int totalSize =  width * height;

        int[] pixs = new int[totalSize];
        bmpResult.getPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());

        int[] pixs_outline = new int[totalSize];
        oulines.getPixels(pixs_outline,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(),bmpResult.getHeight());

        for(int i = 0; i < totalSize; i ++)
        {
            int pix_o = pixs_outline[i];
            if(Color.red(pix_o) < 165)
                pixs[i] = pix_o;
        }

        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    /**
     * Do a cartoonish transformation of a picture
     * @param bmp Bitmap
     * @return bitmap
     */
    public static Bitmap cartoonFilter(Bitmap bmp)
    {
        Bitmap bmp_outline = sobelEdgeDetection(bmp);
        bmp_outline = increaseOutline(bmp_outline);

        Bitmap bmpResult = changeLuminosity(bmp, -50);
        bmpResult = changeContrast(bmpResult, 6);

        bmpResult = combineBitmaps(bmpResult, bmp_outline);
        return bmpResult;
    }
}
