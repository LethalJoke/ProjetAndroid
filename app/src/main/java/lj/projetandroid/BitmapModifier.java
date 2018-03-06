package lj.projetandroid;

//Classe servant à implémenter toutes les méthodes de traitement d'image
// Implémenter méthodes static

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public abstract class BitmapModifier {

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
            if(red > 255)
                red = 255;
            if(blue > 255)
                blue = 255;
            if(green > 255)
                green = 255;
            if(red < 0)
                red = 0;
            if(blue < 0)
                blue = 0;
            if(green < 0)
                green = 0;
            pixs[i] = Color.argb(alpha,red,green,blue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

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
            if(red > 255)
                red = 255;
            if(blue > 255)
                blue = 255;
            if(green > 255)
                green = 255;
            if(red < 0)
                red = 0;
            if(blue < 0)
                blue = 0;
            if(green < 0)
                green = 0;
            pixs[i] = Color.argb(alpha,red,green,blue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

    //Mode : 0-> Grey
    // 1-> Sepia
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
                int grey = (int)((0.299 * red) + (0.114 * blue) + (0.587 * green));
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
            if(newred > 255)
                newred = 255;
            if(newblue > 255)
                newblue = 255;
            if(newgreen > 255)
                newgreen = 255;
            if(newred < 0)
                newred = 0;
            if(newblue < 0)
                newblue = 0;
            if(newgreen < 0)
                newgreen = 0;
            pixs[i] = Color.argb(alpha,newred,newgreen,newblue);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

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
            pixsHSV[i][2] =  histo[(int)(pixHSV[2] * 255)] * 255 / totalSize;
            pixs[i] = Color.HSVToColor(pixsHSV[i]);
        }

        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

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

    public static Bitmap rotateBitmap(Bitmap bmp, float value)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(value);
        return Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }
}
