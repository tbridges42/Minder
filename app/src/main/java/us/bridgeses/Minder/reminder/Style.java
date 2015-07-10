package us.bridgeses.Minder.reminder;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Created by Laura on 7/9/2015.
 */
public class Style implements Parcelable, Serializable {
    public static enum Flags {
        VIBRATE,
        REPEAT_VIBRATE,
        LED,
        BUILD_VOLUME
    }
    private  EnumSet<Flags> flags;
    private int ledColor;
    private String imagePath;
    private int fontColor;

    public boolean hasFlag(Flags flag){
        return flags.contains(flag);
    }

    public int getLedColor() {
        return ledColor;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getFontColor() {
        return fontColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int returnCode) {
        parcel.writeSerializable(flags);
        parcel.writeInt(ledColor);
        parcel.writeString(imagePath);
        parcel.writeInt(fontColor);
    }

    public static final Creator<Style> CREATOR = new Creator<Style>() {
        @Override
        public Style createFromParcel(Parcel parcel) {
            return new Builder().readFromParcel(parcel).build();
        }

        @Override
        public Style[] newArray(int i) {
            return new Style[0];
        }
    };

    private Style(Builder builder){
        this.flags = builder.flags;
        this.ledColor = builder.ledColor;
        this.imagePath = builder.imagePath;
        this.fontColor = builder.fontColor;
    }

    public static final class Builder{
        private  EnumSet<Flags> flags = EnumSet.of(Flags.LED);
        private int ledColor = 0xff000000;
        private String imagePath = "";
        private int fontColor = 0xff000000;

        public Builder(){}

        public Builder setFlag(Flags flag, boolean value){
            if (value){
                if (!flags.contains(flag)){
                    flags.add(flag);
                }
            }
            else{
                if (flags.contains(flag)){
                    flags.remove(flag);
                }
            }
            return this;
        }

        public Builder setLedColor(int color){
            if (color < 0){
                throw new IllegalArgumentException("Color must be non-negative");
            }
            this.ledColor = color;
            return this;
        }

        public Builder setFontColor(int color){
            if (color < 0){
                throw new IllegalArgumentException("Color must be non-negative");
            }
            this.fontColor = color;
            return this;
        }

        public Builder setImagePath(@NonNull String path){
            this.imagePath = path;
            return this;
        }

        public Builder copyStyle(Style style){
            this.flags = style.flags;
            this.ledColor = style.ledColor;
            this.imagePath = style.imagePath;
            this.fontColor = style.fontColor;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder readFromParcel(@NonNull Parcel parcel){
            this.flags = (EnumSet<Flags>)parcel.readSerializable();
            this.ledColor = parcel.readInt();
            this.imagePath = parcel.readString();
            this.fontColor = parcel.readInt();
            return this;
        }

        public Style build(){
            return new Style(this);
        }
    }
}
