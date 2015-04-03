package de.luhmer.stundenplanh_brsimporter.app.Model;

import java.io.Serializable;

/**
 * Created by David on 04.09.2014.
 */
public class ExamRegistrationItem implements Serializable {

    public int examId;
    public String status;
    public String form;
    public String hilfsmittel;
    public Long semester;
    public Long versuch;
    public String zeit;
    public String raum;


    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;

        ExamRegistrationItem exReg = (ExamRegistrationItem) obj;
        return (examId == exReg.examId
                && status.equals(exReg.status)
                && form.equals(exReg.form)
                && hilfsmittel.equals(exReg.hilfsmittel)
                && semester.equals(exReg.semester)
                && versuch.equals(exReg.versuch)
                && zeit.equals(exReg.zeit)
                && raum.equals(exReg.raum));
    }
}
