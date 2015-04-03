package de.luhmer.stundenplanh_brsimporter.app;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;
import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Adapter.ExamSyncAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamContent;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamRegistrationItem;
import de.luhmer.stundenplanh_brsimporter.app.Model.ModifiedExamItem;
import de.luhmer.stundenplanh_brsimporter.app.Parser.ExamParser;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SyncAdapterTest extends AndroidTestCase {

    private final String TAG = getClass().getCanonicalName();
    List<ExamItem> examItems;
    ExamSyncAdapter adapter;
    SharedPreferences mPrefs;


    public SyncAdapterTest() {

    }

    @Override
    protected void setUp() throws Exception {
        adapter = new ExamSyncAdapter(getContext(), false);
        mPrefs = ExamSyncAdapter.getSharedPrefs(getContext());

        if(mPrefs.contains(ExamSyncAdapter.EXAM_LIST_CACHED_STRING)) {
            mPrefs.edit().remove(ExamSyncAdapter.EXAM_LIST_CACHED_STRING).commit();
        }

        examItems = ExamParser.handleExamList(TestData.NOTENSPIEGEL);
        List<ExamRegistrationItem> examRegItems = ExamParser.handleExamRegList(TestData.PRUEFUNGSANMELDUNG);

        //Match all exams with the corresponding registration items
        for(ExamItem exam : examItems) {
            for(ExamRegistrationItem examReg : examRegItems) {
                if(examReg.examId == exam.examId) {
                    exam.examRegistration = examReg;
                    break;
                }
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if(mPrefs.contains(ExamSyncAdapter.EXAM_LIST_CACHED_STRING)) {
            mPrefs.edit().remove(ExamSyncAdapter.EXAM_LIST_CACHED_STRING).commit();
        }
    }

    private void storeExamItems(List<ExamItem> examItems) {
        try {
            String serializedValue = ExamContent.toString((java.io.Serializable) examItems);
            mPrefs.edit().putString(ExamSyncAdapter.EXAM_LIST_CACHED_STRING, serializedValue).commit();
        } catch (Exception ex) {
            fail();
        }
    }

    public void testInitSync() {
        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);

        //first sync should return only null
        assertNull(modifiedExamItems);
    }

    public void testSyncNothingChanged() {
        //Save the exam items to the shared prefs
        storeExamItems(examItems);

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);

        //list should be empty
        assertNotNull(modifiedExamItems);

        assertEquals(0, modifiedExamItems.size());
    }

    public void testGradeChanged() {
        storeExamItems(examItems);

        double newGrade = examItems.get(0).note + 0.1;

        examItems.get(0).note = newGrade;

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
        assertNotNull(modifiedExamItems);
        assertEquals(1, modifiedExamItems.size());
        assertEquals("Note: " + String.valueOf(newGrade), modifiedExamItems.get(0).changedText);
    }


    public void testCreditsChanged() {
        storeExamItems(examItems);

        int newCredits = examItems.get(0).credits + 1;
        examItems.get(0).credits = newCredits;

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
        assertNotNull(modifiedExamItems);
        assertEquals(1, modifiedExamItems.size());
        assertEquals("Credits: " + newCredits, modifiedExamItems.get(0).changedText);
    }

    public void testTerminChanged() {
        storeExamItems(examItems);

        String newTermin = "26.03.2015";
        Date klausurTermin = new Date();
        examItems.get(0).termin = newTermin;
        examItems.get(0).terminKlausur = klausurTermin;

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
        assertNotNull(modifiedExamItems);
        assertEquals(1, modifiedExamItems.size());
        assertEquals("Termin: " + newTermin + " \nTermin Klausur: " + DateFormatUtils.format(klausurTermin, "dd.MM.yyyy"), modifiedExamItems.get(0).changedText);

        //adapter.ShowNotification(modifiedExamItems.get(0).examItem.examId, modifiedExamItems.get(0).examItem.fachName, modifiedExamItems.get(0).changedText, ExamArrayAdapter.getImageResourceForExamStatus(modifiedExamItems.get(0).examItem));
    }

    public void testStatusChanged() {
        storeExamItems(examItems);

        String newStatus = "AN2";
        examItems.get(0).status = newStatus;

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
        assertNotNull(modifiedExamItems);
        assertEquals(1, modifiedExamItems.size());
        assertEquals("Status: " + newStatus, modifiedExamItems.get(0).changedText);
    }

    public void testVermerkChanged() {
        storeExamItems(examItems);

        String newVermerk = "AN2";
        String newFreiVermerk = "AN2";
        examItems.get(0).vermerk = newVermerk;
        examItems.get(0).freiVermerk = newFreiVermerk;

        List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
        assertNotNull(modifiedExamItems);
        assertEquals(1, modifiedExamItems.size());
        assertEquals("Vermerk: " + newVermerk + " \nFreivermerk: " + newFreiVermerk, modifiedExamItems.get(0).changedText);
    }



    public void testExamRegChanged() {
        storeExamItems(examItems);

        int index = -1;
        for(ExamItem item : examItems) {
            if(item.examRegistration != null) {
                index = examItems.indexOf(item);
                break;
            }
        }

        if(index != -1) {
            examItems.get(index).examRegistration.raum = examItems.get(index).examRegistration.raum + " test";

            List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
            assertNotNull(modifiedExamItems);
            assertEquals(1, modifiedExamItems.size());
            assertEquals("Zulassungsinformationen geändert", modifiedExamItems.get(index).changedText);
        } else {
            fail("No item with examRegistration found");
        }
    }

    public void testExamRegRemoved() {
        storeExamItems(examItems);

        int index = -1;
        for(ExamItem item : examItems) {
            if(item.examRegistration != null) {
                index = examItems.indexOf(item);
                break;
            }
        }

        if(index != -1) {
            examItems.get(index).examRegistration = null;

            List<ModifiedExamItem> modifiedExamItems = adapter.getModifiedExamItems(examItems);
            assertNotNull(modifiedExamItems);
            assertEquals(1, modifiedExamItems.size());
            assertEquals("Prüfungsinformationen entfernt", modifiedExamItems.get(index).changedText);
        } else {
            fail("No item with examRegistration found");
        }
    }


}