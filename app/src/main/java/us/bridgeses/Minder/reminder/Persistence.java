package us.bridgeses.Minder.reminder;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Created by Laura on 7/9/2015.
 */
public class Persistence implements Serializable, Parcelable{
    public static enum Flags {
        REQUIRE_CODE,
        OVERRIDE_VOLUME,
        DISPLAY_SCREEN,
        WAKE_UP,
        CONFIRM_DISMISS,
        KEEP_TRYING
    }

    private EnumSet<Flags> flags;
    private String code;
    private byte volume;
    private byte snoozeLimit;
    private int snoozeTime;

    public boolean hasFlag(Flags flag){
        return flags.contains(flag);
    }

    public String getCode() {
        return code;
    }

    public byte getVolume() {
        return volume;
    }

    public byte getSnoozeLimit() {
        return snoozeLimit;
    }

    public int getSnoozeTime() {
        return snoozeTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(flags);
        parcel.writeString(code);
        parcel.writeByte(volume);
        parcel.writeByte(snoozeLimit);
        parcel.writeInt(snoozeTime);
    }

    public static final Creator<Persistence> CREATOR = new Creator<Persistence>() {
        @Override
        public Persistence createFromParcel(Parcel parcel) {
            return new Builder().readFromParcel(parcel).build();
        }

        @Override
        public Persistence[] newArray(int i) {
            return new Persistence[0];
        }
    };

    private Persistence(Builder builder){
        this.flags = builder.flags;
        this.code = builder.code;
        this.volume = builder.volume;
        this.snoozeLimit = builder.snoozeLimit;
        this.snoozeTime = builder.snoozeTime;
    }

    public static final class Builder {
        private EnumSet<Flags> flags = EnumSet.of(
                Flags.DISPLAY_SCREEN,
                Flags.WAKE_UP,
                Flags.KEEP_TRYING
        );
        private String code = "";
        private byte volume = 75;
        private byte snoozeLimit = -1;
        private int snoozeTime = 300000;

        public Builder() {}

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

        public Builder setCode(@NonNull String code){
            this.code = code;
            return this;
        }

        public Builder setVolume(byte volume){
            if ((volume < 0)||(volume > 100)){
                throw new IllegalArgumentException("Volume must be between 0 and 100. Was: " + volume);
            }
            this.volume = volume;
            return this;
        }

        public Builder setSnoozeLimit(byte snoozeLimit){
            this.snoozeLimit = snoozeLimit;
            return this;
        }

        public Builder setSnoozeTime(int snoozeTime){
            if (snoozeTime <= 0){
                throw new IllegalArgumentException("Time must be greater than zero");
            }
            this.snoozeTime = snoozeTime;
            return this;
        }

        public Builder copyPersistence(Persistence persistence){
            this.flags = persistence.flags;
            this.code = persistence.code;
            this.volume = persistence.volume;
            this.snoozeLimit = persistence.snoozeLimit;
            this.snoozeTime = persistence.snoozeTime;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder readFromParcel(Parcel parcel){
            this.flags = (EnumSet<Flags>)parcel.readSerializable();
            this.code = parcel.readString();
            this.volume = parcel.readByte();
            this.snoozeLimit = parcel.readByte();
            this.snoozeTime = parcel.readByte();
            return this;
        }

        public Persistence build() {
            return new Persistence(this);
        }
    }
}
