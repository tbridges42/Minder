package us.bridgeses.Minder.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
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
        this.uri = getPersistedString(null);
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

    private void setThumbnail(){
        if (uri == null){
            uri = getPersistedString(null);
        }
        Logger.d("Attempting to set thumbnail to "+uri);
        if (uri != null) {
            try{
                thumbnail.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri))), 150, 150));
            }
            catch (Exception e){
                Log.e("Minder","Invalid file path");
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
