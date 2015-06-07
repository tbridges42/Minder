package us.bridgeses.Minder.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.orhanobut.logger.Logger;

/**
 * Created by Tony on 6/5/2015.
 */
public class ImageHelper {

    public static int getImageOrientation(Context context, String path){
        Cursor cursor = context.getContentResolver().query(Uri.parse(path),
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int x, int y){
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new RectF(0, 0, x, y), Matrix.ScaleToFit.CENTER);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    public static void saveBitmap(Bitmap bitmap, String path){

    }

    public static Bitmap getScaledImage(Context context, String uri, int x, int y){
        if (uri == null){
            uri = "";
        }
        if (!uri.equals("")) {
            try{
                Logger.d("URI: " + uri);
                int orientation = getImageOrientation(context, uri);
                Logger.d("Found orientation: " + orientation);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri)),null,options);
                Logger.d("Decoded stream");
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;

                if ((imageHeight > y)||(imageWidth > x)) {
                    options.inSampleSize = calculateInSampleSize(options, x, y);
                }
                Logger.d("Calculated Size");

                options.inJustDecodeBounds = false;
                options.inMutable = true;
                Bitmap thumbBM = BitmapFactory.decodeStream(
                        context.getContentResolver().openInputStream(Uri.parse(uri)),null,options);
                Logger.d("Created raw image");

                Matrix matrix = new Matrix();
                if (orientation != 0f) {
                    matrix.preRotate(orientation);
                    thumbBM = Bitmap.createBitmap(thumbBM, 0, 0, thumbBM.getWidth(), thumbBM.getHeight(), matrix, true);
                }
                return thumbBM;
            }
            catch (Exception e){
                Log.e("Minder", "Invalid file path");
                e.printStackTrace();
                return null;
            }
        }
        else{
            Logger.d("Setting default gradient");
            Bitmap b = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.drawColor(Color.BLACK);

            /* Create your gradient. */
            LinearGradient grad = new LinearGradient(0, 0, 0, y, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);

            /* Draw your gradient to the top of your bitmap. */
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setShader(grad);
            c.drawRect(0, 0, x, y, p);
            return b;
        }
    }


}
