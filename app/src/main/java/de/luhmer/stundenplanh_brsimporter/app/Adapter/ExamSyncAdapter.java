package de.luhmer.stundenplanh_brsimporter.app.Adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.luhmer.stundenplanh_brsimporter.app.Events.ExamSyncFinishedEvent;
import de.luhmer.stundenplanh_brsimporter.app.MainActivity;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamContent;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamRegistrationItem;
import de.luhmer.stundenplanh_brsimporter.app.Model.ModifiedExamItem;
import de.luhmer.stundenplanh_brsimporter.app.Parser.ExamParser;
import de.luhmer.stundenplanh_brsimporter.app.R;

/**
 * Created by David on 05.07.2014.
 */
public class ExamSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String EXAM_LIST_CACHED_STRING = "EXAM_LIST_CACHED_STRING";
    //public static final String AVG_GRADE_FLOAT = "AVG_GRADE_FLOAT";
    private static final String TAG = "ExamSyncAdapter";

    public ExamSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("udinic", "onPerformSync for account[" + account.name + "]");

        boolean changedSomething = true;
        boolean success = false;
        try {
            List<ModifiedExamItem> modifiedExamItems = sync(account);

            if(modifiedExamItems != null) {
                for (ModifiedExamItem cItem : modifiedExamItems) {
                    //Log.v(TAG, cItem.examItem.fachName);
                    ShowNotification(cItem.examItem.examId, cItem.examItem.fachName, cItem.changedText, ExamArrayAdapter.getImageResourceForExamStatus(cItem.examItem));
                }
            }

            success = true;
        } catch (Exception e) {
            ShowNotification(-1, "Sync failed", e.getLocalizedMessage(), R.drawable.ic_launcher);
            e.printStackTrace();
        } finally {
            EventBus.getDefault().post(new ExamSyncFinishedEvent(changedSomething, success));
        }
    }

    public List<ModifiedExamItem> sync(Account account) throws ExamParser.UnauthorizedException, IOException {
        SharedPreferences mPrefs = getSharedPrefs(getContext());

        HashMap<String, String> links = extractLinksFromSis(account);
        List<ExamItem> examItems = downloadExamItems(links);

        //float avgGrade = ExamParser.ParseAvgGrade(links.get("Studienfortschritt"));
        //mPrefs.edit().putFloat(AVG_GRADE_FLOAT, avgGrade).commit();

        List<ModifiedExamItem> modifiedExamItems = getModifiedExamItems(examItems);

        //first sync or something has been changed
        if(modifiedExamItems == null || modifiedExamItems.size() > 0) {
            String serializedValue = ExamContent.toString((java.io.Serializable) examItems);
            mPrefs.edit().putString(EXAM_LIST_CACHED_STRING, serializedValue).commit();
        }

        return modifiedExamItems;
    }

    public List<ExamItem> downloadExamItems(HashMap<String, String> links) throws ExamParser.UnauthorizedException, IOException {
        List<ExamItem> examItems = ExamParser.ParseExams(links.get("Notenspiegel (vollständig)"));
        List<ExamRegistrationItem> examRegItems = ExamParser.ParseExamRegs(links.get("Prüfungsanmeldung"));

        //Match all exams with the corresponding registration items
        for(ExamItem exam : examItems) {
            for(ExamRegistrationItem examReg : examRegItems) {
                if(examReg.examId == exam.examId && examReg.versuch == exam.versuch) {
                    exam.examRegistration = examReg;
                    break;
                }
            }
        }

        return examItems;
    }


    public HashMap<String, String> extractLinksFromSis(Account account) throws ExamParser.UnauthorizedException {
        String username = account.name;
        String password = AccountManager.get(getContext()).getPassword(account);

        return ExamParser.Login(username, password);
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences("", Context.MODE_PRIVATE);
    }

    /**
     *
     * @param examItems is null when no cached items were available. otherwise it returns a list of modified items
     * @return
     */
    public List<ModifiedExamItem> getModifiedExamItems(List<ExamItem> examItems) {
        List<ModifiedExamItem> modifiedExamItems = null;

        SharedPreferences mPrefs = getSharedPrefs(getContext());
        if (mPrefs.contains(EXAM_LIST_CACHED_STRING)) {

            List<ExamItem> cachedExamItems = getCachedExamItems();
            modifiedExamItems = new ArrayList<>();

            //For all downloaded exam items
            for (ExamItem nItem : examItems) {
                boolean foundEntry = false;

                //Compare them with the cached ones
                for (ExamItem cItem : cachedExamItems) {
                    if (cItem.examId == nItem.examId) {
                        //Check is required!
                        if (cItem.versuch == nItem.versuch && !cItem.equals(nItem)) {
                            if (!modifiedExamItems.contains(nItem)) {
                                modifiedExamItems.add(new ModifiedExamItem(nItem, nItem.getChanged(cItem)));
                            } else {
                                Log.e(TAG, "Should not happen!");
                            }
                        }
                        foundEntry = true;
                        break;
                    }
                }

                if (!foundEntry) { //New Entry is available!
                    if (!modifiedExamItems.contains(nItem)) {
                        modifiedExamItems.add(new ModifiedExamItem(nItem, "Neue Prüfung"));
                    }
                }
            }
        }
        return modifiedExamItems;
    }

    private List<ExamItem> getCachedExamItems() {
        SharedPreferences mPrefs = getSharedPrefs(getContext());
        List<ExamItem> cachedExamItems = null;
        try {
            cachedExamItems = (List<ExamItem>) ExamContent.fromString(mPrefs.getString(EXAM_LIST_CACHED_STRING, ""));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cachedExamItems;
    }





    public final void ShowNotification(int id, String title, String content, int imgResourceId) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_OPEN_GRADE_VIEW, true);

        PendingIntent pIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder notification = new Notification.Builder(getContext())
                //.setStyle(new Notification.BigTextStyle().bigText(notificationText))
                .setContentTitle(title)
                .setContentIntent(pIntent)
                .setSmallIcon(imgResourceId)
                .setContentText(content.replace("\n", "; "))
                .setStyle(new Notification.BigTextStyle().bigText(content))
                .setAutoCancel(true);

        ShowNotification(notification.build(), id);
    }

    private void ShowNotification(Notification notification, int id) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}