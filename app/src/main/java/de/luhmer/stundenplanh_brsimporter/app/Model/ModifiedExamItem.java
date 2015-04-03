package de.luhmer.stundenplanh_brsimporter.app.Model;

/**
 * Created by David on 14.03.2015.
 */
public class ModifiedExamItem {

    public final ExamItem examItem;
    public final String changedText;

    public ModifiedExamItem(ExamItem examItem, String changedText) {
        this.examItem = examItem;
        this.changedText = changedText;
    }
}
