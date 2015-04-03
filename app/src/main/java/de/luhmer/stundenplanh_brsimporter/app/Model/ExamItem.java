package de.luhmer.stundenplanh_brsimporter.app.Model;

import org.apache.commons.lang.time.DateFormatUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by David on 04.07.2014.
 */
public class ExamItem implements Serializable, Comparable<ExamItem> {

    private static final String TAG = "ExamItem";

    public int examId;
    public String fachName;
    public Double note;
    public int credits;
    public String termin;
    public String status;
    public int versuch;
    public String vermerk;
    public String freiVermerk;
    public Date terminKlausur;
    public ExamRegistrationItem examRegistration;

    @Override
    public boolean equals(Object o) {
        if(o instanceof ExamItem) {
            ExamItem examItem = (ExamItem) o;

            boolean examRegEq = false;
            if(examRegistration != null && examItem.examRegistration != null && examRegistration.equals(examItem.examRegistration))
                examRegEq = true;
            else if(examRegistration == null && examItem.examRegistration == null)
                examRegEq = true;

            return (examId == examItem.examId &&
                    fachName.equals(examItem.fachName) &&
                    note.equals(examItem.note) &&
                    credits == examItem.credits &&
                    termin.equals(examItem.termin) &&
                    status.equals(examItem.status) &&
                    versuch == examItem.versuch &&
                    vermerk.equals(examItem.vermerk) &&
                    freiVermerk.equals(examItem.freiVermerk) &&
                    terminKlausur.getTime() == examItem.terminKlausur.getTime() &&
                    examRegEq);
        } else {
            return super.equals(o);
        }
    }

    /**
     *
     * @param o Cached item (should be older than this object)
     * @return
     */
    public String getChanged (Object o) {
        String text = "";
        if(o instanceof ExamItem) {
            ExamItem examItem = (ExamItem) o;

            if(!note.equals(examItem.note))
                text += "Note: " + String.valueOf(note) + "\n";
            if(credits != examItem.credits)
                text += "Credits: " + credits + " \n";
            if(!termin.equals(examItem.termin))
                text += "Termin: " + termin + " \n";
            if(!status.equals(examItem.status))
                text += "Status: " + status + " \n";
            if(!vermerk.equals(examItem.vermerk))
                text += "Vermerk: " + vermerk + " \n";
            if(!terminKlausur.equals(examItem.terminKlausur))
                text += "Termin Klausur: " + DateFormatUtils.format(terminKlausur, "dd.MM.yyyy") + " \n";
            if(!freiVermerk.equals(examItem.freiVermerk))
                text += "Freivermerk: " + freiVermerk + " \n";

            if(examRegistration != null && !examRegistration.equals(examItem.examRegistration))
                text += "Zulassungsinformationen geändert \n";

            if(examRegistration == null && examItem.examRegistration != null)
                text += "Prüfungsinformationen entfernt \n";
        }
        return text.trim();
    }

    @Override
    public int compareTo(ExamItem e) {
        return terminKlausur.compareTo(e.terminKlausur);
    }
}
