package de.luhmer.stundenplanh_brsimporter.app.Model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 27.03.2015.
 */
public class Tuple<E, T> /* implements Parcelable */{
    public final E key;
    public final T value;
    public Tuple(E key, T value) {
        this.key = key;
        this.value = value;
    }

    /*

    public Tuple(Parcel in, ClassLoader loader) {
        key = (E) in.readValue(loader);
        value = (T) in.readValue(loader);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(key);
        dest.writeValue(value);
    }
    */
}
