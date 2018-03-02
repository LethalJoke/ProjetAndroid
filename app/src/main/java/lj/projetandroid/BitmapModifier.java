package lj.projetandroid;

//Classe servant à implémenter toutes les méthodes de traitement d'image
// Implémenter méthodes static

import android.graphics.Bitmap;
import android.graphics.Color;

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

    public static Bitmap changeContraste(Bitmap bmp, double value)
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

    public static Bitmap changeGris(Bitmap bmp)
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
            int grey = (int)(0.299 * red + 0.114 * blue + 0.587 * green);
            pixs[i] = Color.argb(alpha,grey,grey,grey);
        }
        bmpResult.setPixels(pixs,0,bmpResult.getWidth(),0,0,bmpResult.getWidth(), bmpResult.getHeight());
        return bmpResult;
    }

}
