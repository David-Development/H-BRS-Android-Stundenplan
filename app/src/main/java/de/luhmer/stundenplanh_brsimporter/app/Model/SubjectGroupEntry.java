package de.luhmer.stundenplanh_brsimporter.app.Model;

/**
 * Created by David on 03.10.2014.
 */
public class SubjectGroupEntry {

    public SubjectGroupEntry(String title, Boolean selected, String id) {
        this.title = title;
        this.selected = selected;
        this.id = id;
    }

    public String title;
    public boolean selected;
    public String id;
}
