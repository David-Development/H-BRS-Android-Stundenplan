package de.luhmer.stundenplanh_brsimporter.app.Model;

/**
 * Created by David on 27.03.2015.
 */
public class ProfessorEntry {

    public String mName;
    public String mUrl;
    public String mRoom;
    public String mThumbUrl;
    public String mTel;
    public String mFax;

    public ProfessorEntry(String name, String url, String thumbUrl, String tel, String fax, String room) {
        this.mName = name;
        this.mUrl = url;
        this.mThumbUrl = thumbUrl;
        this.mTel = tel;
        this.mFax = fax;
        this.mRoom = room;
    }

}
