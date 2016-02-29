package com.wq.photo.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

public class BitMapUtils {

    public static Bitmap resizeImage(String path, int newWidth) {
        BitmapFactory.Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int dx = width / newWidth;
        int scale = dx;
        opts.inSampleSize = scale;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        return bitmap;
    }

    public static void saveBitmapFile(Bitmap bitmap, File file) {
        if(bitmap!=null){
            try {
                if(file!=null&&!file.exists()){
                    file.createNewFile();
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap resizeImage(String path, int newWidth, int newHeight) {
        BitmapFactory.Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        int dx = width / newWidth;
        int dy = height / newHeight;
        int scale = 1;
        if (dx > dy && dy > 1) {
            scale = dx;
        }
        if (dy >= dx && dx > 1) {
            scale = dy;
        }
        opts.inSampleSize = scale;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        return bitmap;

    }
    
    public static Bitmap resizeImage(String path, int newWidth, int newHeight,File file) {
        BitmapFactory.Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        int dx = width / newWidth;
        int dy = height / newHeight;
        int scale = 1;
        if (dx > dy && dy > 1) {
            scale = dx;
        }
        if (dy >= dx && dx > 1) {
            scale = dy;
        }
        opts.inSampleSize = scale;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        BitMapUtils.saveBitmapFile(bitmap, file);
        return bitmap;

    }
    

    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {
        if (context == null || uri == null)
            return null;

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
}
