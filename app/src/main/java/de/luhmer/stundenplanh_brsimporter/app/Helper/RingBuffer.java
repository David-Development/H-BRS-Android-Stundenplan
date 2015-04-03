package de.luhmer.stundenplanh_brsimporter.app.Helper;

import de.luhmer.stundenplanh_brsimporter.app.Model.Tuple;

/**
 * Created by David on 27.03.2015.
 */
public class RingBuffer<E, T> {

    Tuple[] data;
    int     currentPos = 0;

    // init(maxSize)
    public RingBuffer(int maxSize) {
        data = new Tuple[maxSize];  }

    public void add(E key, T value) {
        data[currentPos] = new Tuple(key, value);
        currentPos = (currentPos + 1) % data.length;//Move pointer to the next free position
    }

    public T get(E key) {
        for(int i = 0; i < data.length; i++) {
            if(data[i] != null && data[i].key.equals(key))
                return (T) data[i].value;
        }
        return null;
    }

}
