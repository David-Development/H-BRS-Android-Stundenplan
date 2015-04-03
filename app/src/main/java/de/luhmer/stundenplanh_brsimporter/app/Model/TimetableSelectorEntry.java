package de.luhmer.stundenplanh_brsimporter.app.Model;

import android.util.SparseArray;

/**
 * Created by David on 03.10.2014.
 */
public class TimetableSelectorEntry {

    public TimetableSelectorEntry(String title, String semesterId) {
        this.title = title;
        this.semesterId = semesterId;
        this.subjects = new SparseArray<String>();
    }

    public String semesterId;
    public String title;
    public SparseArray<String> subjects;

}
