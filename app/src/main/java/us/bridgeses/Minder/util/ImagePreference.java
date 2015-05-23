package us.bridgeses.Minder.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.orhanobut.logger.Logger;

import android.net.Uri;

import java.io.IOException;

import us.bridgeses.Minder.R;

/**
 * Created by Tony on 5/15/2015.
 */
public class ImagePreference extends Preference {

    Context context;
    private String uri;
    ImageView thumbnail;

    public ImagePreference(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        this.uri = getPersistedString("");
    }

    @Override
    protected void onClick(){
        Intent intent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity)context).startActivityForResult(intent, 1);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
        setThumbnail();
    }

    private String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    private int getOrientationByBucket(String path){
        int orientation = -1;
        String bucket = getBucketId(path);
        ContentResolver contentResolver = context.getContentResolver();
        Cursor mediaCursor = contentResolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                new String[] {MediaStore.Images.ImageColumns.ORIENTATION },
                MediaStore.Images.ImageColumns.BUCKET_ID + " = ?",
                new String[]{bucket},
                MediaStore.Images.ImageColumns.BUCKET_ID + " desc");
        if (mediaCursor.getCount() == 0){
            Logger.d("No matching bucket id");
        }
        else{
            mediaCursor.moveToFirst();
            orientation = mediaCursor.getInt(0);
            switch (orientation){
                case 0: {
                    orientation = 1;
                    break;
                }
                case 90:{
                    orientation = 8;
                    break;
                }
                case 180:{
                    orientation  = 3;
                    break;
                }
                case 270:{
                    orientation = 6;
                    break;
                }
            }
        }
        return orientation;
    }

    private int getOrientation(String path){
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

    public void setThumbnail(){
        if (uri == null){
            uri = getPersistedString("");
        }
        Logger.d("Attempting to set thumbnail to "+uri);
        if (uri != "") {
            try{
                int orientation = getOrientation(uri);
                Logger.d("Found orientation: " + orientation);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri)),null,options);
                Logger.d("Decoded stream");
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;

                if ((imageHeight > 150)||(imageWidth > 150)) {
                    options.inSampleSize = calculateInSampleSize(options, 150, 150);
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
                Logger.d("Rotated image");
                thumbnail.setImageBitmap(thumbBM);
                Logger.d("Set thumbnail");
            }
            catch (Exception e){
                Log.e("Minder","Invalid file path");
                e.printStackTrace();
            }
        }
        else{
            Bitmap b = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.drawColor(Color.BLACK);

    /* Create your gradient. */
            LinearGradient grad = new LinearGradient(0, 0, 0, 150, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);

    /* Draw your gradient to the top of your bitmap. */
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setShader(grad);
            c.drawRect(0, 0, 150, 150, p);
            thumbnail.setImageBitmap(b);
        }
    }

    public void setImage(String uri){
        Logger.d("Setting image to "+uri);
        this.uri = uri;
        persistString(uri);
        setThumbnail();
    }

    @Override
    protected View onCreateView(ViewGroup parent){
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        return layoutInflater.inflate(R.layout.image_preference, parent, false);
    }
}
