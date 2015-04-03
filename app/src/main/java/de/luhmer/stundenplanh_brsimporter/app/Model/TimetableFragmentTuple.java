package de.luhmer.stundenplanh_brsimporter.app.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.ref.WeakReference;

import de.luhmer.stundenplanh_brsimporter.app.TimetableDayFragment;

/**
 * Created by David on 27.03.2015.
 */
public class TimetableFragmentTuple extends Tuple<Long, TimetableDayFragment> {
    public final Long key;
    public final WeakReference<TimetableDayFragment> value;


    public TimetableFragmentTuple(Long key, TimetableDayFragment value) {
        super(key, value);

        this.key = key;
        this.value = new WeakReference<>(value);
    }

    /*
    public TimetableFragmentTuple(Parcel in, ClassLoader loader) {
        super(in, loader);

        key = (Long) in.readValue(loader);
        value = new WeakReference<>((TimetableDayFragment) in.readValue(loader));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(key);
        dest.writeValue(value.get());
    }
    */
}
