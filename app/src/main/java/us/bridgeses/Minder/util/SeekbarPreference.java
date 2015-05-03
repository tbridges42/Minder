package us.bridgeses.Minder.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import us.bridgeses.Minder.R;

/**
 * Created by Tony on 5/1/2015.
 * This was largely developed independently, but after discovering a very similar class in the current
 * Android repository, I cribbed the saved state stuff from there
 */
public class SeekbarPreference extends Preference implements SeekBar.OnSeekBarChangeListener{

	public SeekbarPreference(Context context, AttributeSet attrs){
		super(context, attrs);
	}

	private int mMax = 100;
	private int mPosition;
	private SeekBar mSeekBar;
	private boolean mTrackingTouch;

	public void setMax(int max){
		if (max >= 0) {
			mMax = max;
			if (mSeekBar != null) {
				mSeekBar.setMax(mMax);
			}
			notifyChanged();
		}
		else {
			throw new IllegalArgumentException("Max must be positive");
		}
	}

	public void setPosition(int position, boolean notify){
		if (position > mMax){
			position = mMax;
		}
		if (position < 0) {
			position = 0;
		}
		if (position != mPosition){
			mPosition = position;
			persistInt(mPosition);
			if (notify){
				notifyChanged();
			}
		}
	}

	public void setPosition(int position){
		setPosition(position,true);
	}

	public int getPosition(){
		return mPosition;
	}

	void syncProgress(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		if (progress != mPosition) {
			if (callChangeListener(progress)) {
				setPosition(progress, false);
			} else {
				seekBar.setProgress(mPosition);
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekbar, int position, boolean fromUser){
		if (fromUser && !mTrackingTouch) {
			syncProgress(seekbar);
		}
	}

	@Override
	protected View onCreateView(ViewGroup parent){
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		return layoutInflater.inflate(R.layout.seekbar_preference, parent, false);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar){
		mTrackingTouch = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar){
		mTrackingTouch = false;
		if (seekBar.getProgress() != mPosition) {
			syncProgress(seekBar);
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mMax = mSeekBar.getMax();
		mSeekBar.setProgress(mPosition);
		mSeekBar.setEnabled(isEnabled());
	}

	@Override
	protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}
		// Save the instance state
		final SavedState myState = new SavedState(superState);
		myState.progress = mPosition;
		myState.max = mMax;
		return myState;
	}
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}
		// Restore the instance state
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		mPosition = myState.progress;
		mMax = myState.max;
		notifyChanged();
	}
	/**
	 * SavedState, a subclass of {@link BaseSavedState}, will store the state
	 * of MyPreference, a subclass of Preference.
	 * <p>
	 * It is important to always call through to super methods.
	 */
	private static class SavedState extends BaseSavedState {
		int progress;
		int max;
		public SavedState(Parcel source) {
			super(source);
			// Restore the click counter
			progress = source.readInt();
			max = source.readInt();
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Save the click counter
			dest.writeInt(progress);
			dest.writeInt(max);
		}
		public SavedState(Parcelable superState) {
			super(superState);
		}
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}
					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}
}
