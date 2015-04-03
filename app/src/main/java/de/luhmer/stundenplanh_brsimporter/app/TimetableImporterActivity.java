package de.luhmer.stundenplanh_brsimporter.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryImpl;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.luhmer.stundenplanh_brsimporter.app.Helper.AsyncTaskHelper;
import de.luhmer.stundenplanh_brsimporter.app.Helper.CalendarEventImporter;
import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableSelectorEntry;


public class TimetableImporterActivity extends ActionBarActivity {

    private static final String FIRST_STARTUP_IMPORTER = "FIRST_STARTUP_IMPORTER";
    private final String TAG = getClass().getCanonicalName();

    @InjectView(R.id.progressbar_webview) ProgressBar progressbar_webview;
    @InjectView(R.id.btn_download_tt) Button btn_download;
    @InjectView(R.id.ll_importer) LinearLayout ll_importer;
    //@InjectView(R.id.expListView) ExpandableListView expListView;
    @InjectView(R.id.timetable_selector_webview) WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_importer);
        ButterKnife.inject(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        initWebView();


        loadStuffFromServer();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(mPrefs.getBoolean(FIRST_STARTUP_IMPORTER, true)) {
            //ShowcaseDimHelper.dimView(ll_importer);

            /*
            ViewTarget target = new ViewTarget(R.id.webView, this);
            ShowcaseView sv = ShowcaseView.insertShowcaseView(target, this, "Login", "Bitte einloggen um Stundenpläne herunterladen zu können. Zum Login sind die Unix-Anmeldedaten notwendig.");
            sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    ShowcaseDimHelper.undoDimView(ll_importer);
                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                }
            });
            sv.show();
            */

            mPrefs.edit().putBoolean(FIRST_STARTUP_IMPORTER, false).commit();
        }



        /*
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                Intent intent = new Intent(TimetableImporterActivity.this, TimetableSelectGroupActivity.class);
                TimetableSelectorAdapter adapter = (TimetableSelectorAdapter)expListView.getExpandableListAdapter();

                intent.putExtra("idsemester", adapter.getGroup(i).semesterId);
                intent.putExtra("idsubject", adapter.getKeyAt(i, i2));
                intent.putExtra("title", adapter.getChild(i, i2));
                startActivity(intent);

                return false;
            }
        });
        */
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView()
    {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSupportZoom(false);
        //webSettings.setAppCacheEnabled(true);
        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //webSettings.setAppCacheMaxSize(200);
        //webSettings.setDatabaseEnabled(true);
        //webview.clearCache(true);



        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });
    }


    private void loadStuffFromServer() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = mPrefs.getString(Constants.PREF_USERNAME_TIMETABLE, null);
        String password = mPrefs.getString(Constants.PREF_PASSWORD_TIMETABLE, null);


        if(username != null && password != null) {
            //AsyncTaskHelper.StartAsyncTask(new DownloadSemesterListTask(), (Void) null);
            String postData = "username=" + username + "&password=" + password;
            webView.postUrl(Constants.SITE_URL, EncodingUtils.getBytes(postData, "BASE64"));

            Toast.makeText(this, "Loading.. please wait.", Toast.LENGTH_LONG).show();

        } else {
            startActivityForResult(new Intent(this, TimetableLoginActivity.class), 0);
        }


    }

    @OnClick(R.id.btn_download_tt)
    public void importTimetable() {
        //String selectedItem = spinner_timetable.getSelectedItem().toString();
        //String urlToTimetable = "https://lilith.fslab.de" + getTimeTableUrl(selectedItem);//here u have to pass the value that is selected on the spinner

        AsyncTaskHelper.StartAsyncTask(new DownloadTimeTableTask(TimetableImporterActivity.this), (Void) null);

        Log.v(TAG, "Download started");
    }


    private class DownloadSemesterListTask extends AsyncTask<Void, Void, List<TimetableSelectorEntry>> {

        List<TimetableSelectorEntry> entryList = new ArrayList<TimetableSelectorEntry>();

        @Override
        protected List<TimetableSelectorEntry> doInBackground(Void... voids) {
            try {

                //Download list of semesters
                String result = getBodyFromPageGet(Constants.API_URL + "?action=get_list_of_semesters", TimetableImporterActivity.this);
                JSONObject jsonResult = new JSONObject(result);
                JSONArray data = jsonResult.getJSONArray("data");

                for(int i = 0; i < data.length(); i++) {
                    String title = data.getJSONObject(i).getString("name");
                    String idSemester = data.getJSONObject(i).getString("idsemester");

                    entryList.add(new TimetableSelectorEntry(title, idSemester));
                }


                //Download subjects for each semester
                for(TimetableSelectorEntry entry : entryList) {
                    result = getBodyFromPageGet(Constants.API_URL + "?action=get_subjects_for_semester&idsemester=" + entry.semesterId, TimetableImporterActivity.this);
                    jsonResult = new JSONObject(result);
                    data = jsonResult.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        String title = data.getJSONObject(i).getString("name");
                        int idSubject = data.getJSONObject(i).getInt("idsubject");

                        entry.subjects.put(idSubject, title);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return entryList;
        }

        @Override
        protected void onPostExecute(List<TimetableSelectorEntry> semesters) {
            /*
            expListView.setAdapter(new TimetableSelectorAdapter(TimetableImporterActivity.this, semesters));
            */

            super.onPostExecute(semesters);
        }
    }



    private class DownloadTimeTableTask extends AsyncTask<Void, Void, List<TimetableEntry>> {
        ProgressDialog progressDialog;
        Context context;

        DownloadTimeTableTask(Activity activity) {
            context = activity;

            progressDialog = new ProgressDialog(activity);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading please wait..");

            progressDialog.show();
        }

        @Override
        protected List<TimetableEntry> doInBackground(Void... params) {
            return GetTimeTable(context);
        }

        @Override
        protected void onPostExecute(final List<TimetableEntry> result) {
            progressDialog.dismiss();

            final HashMap calendars = CalendarEventImporter.getAvailableCalendars(context);
            final String[] keys = (String[]) calendars.keySet().toArray(new String[calendars.keySet().size()]);
            final String[] items = (String[]) calendars.values().toArray(new String[calendars.size()]);

            for(int i = 0; i < items.length; i++) {
                items[i] = keys[i] + " - " + items[i];
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Android-Kalender Import")
                .setMessage("Möchten Sie die Termine in den auch in den Standard Android Kalender importieren?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Kalender auswählen");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                long cal_id = Long.parseLong(keys[item]);
                                Toast.makeText(context, cal_id + " - " + items[item], Toast.LENGTH_SHORT).show();

                                for (TimetableEntry entry : result) {
                                    CalendarEventImporter.InsertCalEvents(context, entry, cal_id);
                                    //CalendarEventImporter.InsertCalEvents(context, result.get(0), cal_id);
                                }

                                TimetableImporterActivity.this.finish();
                            }
                        }).show();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TimetableImporterActivity.this.finish();
                    }
                })
                .show();





        }
    }

    static TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
    };


    // Create all-trusting host name verifier
    static HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


    public static String calendarString = "CAL_STRING";
    //Pattern timetablePattern = Pattern.compile("<a href='(.*?\\/schedules\\/.*?)'>(.*?)<\\/a>", Pattern.MULTILINE);
    //Pattern timetableUrlPattern = Pattern.compile("(https:.*?.ics)");
    //Pattern usernamePattern = Pattern.compile("<abbr title=\".*?>(.*?)<");

    public static List<TimetableEntry> GetTimeTable(Context context) {
        List<TimetableEntry> entryList = new ArrayList<>();


        try {
            String result = getBodyFromPageGet(Constants.API_URL + "?action=get_timetable", context);
            JSONObject jsonResult = new JSONObject(result);

            boolean success = jsonResult.getBoolean("success");
            String data = jsonResult.getString("data");

            if(!success)
                throw new Exception(data);

            //JSONArray listOfSemesters = jsonResult.getJSONArray("data");

            /*
            String[] lines = StringUtils.split(data, '\n');
            for(int i = 0; i < lines.length; i++) {
                if(lines[i].startsWith("EXDATE")) {
                    lines[i] += "T000000Z";
                }
            }
            data = StringUtils.join(lines, '\n');
            */

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mPrefs.edit().putString(calendarString, data).commit();

            entryList = parseTimetableEntryList(context, data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entryList;
    }


    public static List<TimetableEntry> parseTimetableEntryList(Context context, String calendarString) {
        List<TimetableEntry> entryList = getTimetableEntrysByCalendar(context, calendarString, null);
        return entryList;
    }


    public static List<TimetableEntry> getTimetableEntrysByCalendar(Context context, String calendarString, Filter filter) {
        ArrayList<TimetableEntry> entryList = new ArrayList<>();

        try {
            //TimeZone.setDefault(TimeZone.getDefault());
            //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            long currentTime = System.currentTimeMillis();

            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            CompatibilityHints.setHintEnabled("ical4j.unfolding.relaxed", true);

            //System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
            //Log.v("TimetableImporter", System.getProperty("net.fortuna.ical4j.timezone.update.enabled"));


            UnfoldingReader reader = new UnfoldingReader(new StringReader(calendarString), 5000);
            Calendar calendar = new CalendarBuilder().build(reader);

            Log.v("bla", "Time needed:" + (System.currentTimeMillis() - currentTime));

            List eventsFiltered;
            if(filter != null) {
                eventsFiltered = (List) filter.filter(calendar.getComponents(Component.VEVENT));
            } else {
                eventsFiltered = calendar.getComponents(Component.VEVENT);
            }

            // For each VEVENT in the ICS
            for (Object o : eventsFiltered) {
                Component c = (Component) o;
                entryList.add(getTimetableEntryByVEvent(c));
            }


            /*
            // For each VEVENT in the ICS
            for (Object o : calendar.getComponents("VEVENT")) {
                VEvent vEvent = (VEvent) o;
                Component c = (Component) o;


                entryList.add(getTimetableEntryByVEvent(vEvent, c));
            }
            */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entryList;
    }

    public static TimetableEntry getTimetableEntryByVEvent(Component c) {
        TimetableEntry entry = new TimetableEntry();

        for (Object propObj : c.getProperties()) {
            Property property = (Property) propObj;

            if (property instanceof ExDate) {
                ExDate exdate = (ExDate) property;
                entry.exdate.add(exdate.getValue());

                            /*
                            for(Object dt : exdate.getDates()) {
                                Date date = (Date)dt;
                                //entry.exdate.add(date.getTime());
                            }
                            */
            } else if (property instanceof Categories) {
                Categories categories = (Categories) property;
                entry.categories = categories.getValue();
            } else if (property instanceof DtStart) {
                //vEvent.getStartDate().setUtc(true);

                //DtStart dtStart = (DtStart) property;
                entry.dtstart = getNormalizedDate(property);

                //if(vEvent.getStartDate().getDate().getTimezoneOffset() == -120)
                //    entry.dtstart -= AlarmManager.INTERVAL_HOUR;

            } else if (property instanceof DtEnd) {
                //vEvent.getEndDate().setUtc(true);
                //Date dtEnd = vEvent.getEndDate().getDate();
                //entry.dtend = dtEnd.getTime();

                //DtEnd dtEnd = (DtEnd) property;
                entry.dtend = getNormalizedDate(property);

                //if(vEvent.getEndDate().getDate().getTimezoneOffset() == -120)
                //    entry.dtend -= AlarmManager.INTERVAL_HOUR;

            } else if (property instanceof Description) {
                Description description = (Description) property;
                entry.description = description.getValue();
            } else if (property instanceof Summary) {
                Summary summary = (Summary) property;
                entry.summary = summary.getValue();
            } else if (property instanceof RRule) {
                RRule rrule = (RRule) property;
                entry.rrule = rrule.getValue();

                //if (entry.rrule.contains("UNTIL"))
                //    entry.rrule += "T000000Z";

            } else if (property instanceof Location) {
                Location location = (Location) property;
                entry.location = location.getValue();
            }
        }
        return entry;
    }


    public static long getNormalizedDate(Property property) {
        DateProperty dateProperty = (DateProperty) property;

        boolean isWinterTime = (new Date().getTimezoneOffset() == 120); //Wintertime : 60 min, Summertime : 120min

        String time = property.getValue().substring(9);//19980130T134500Z
        time = time.replace("Z", "");

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));




        //Log.v("TimetableImporter", "Date Timezone: " + date.getTimezoneOffset());
        //Log.v("TimetableImporter", "Current Timezone: " + new Date().getTimezoneOffset());


        /*
        java.util.Calendar calCurrent = java.util.Calendar.getInstance();
        calCurrent.setTime(new Date());
        java.util.Calendar calDate = java.util.Calendar.getInstance();
        calDate.setTime(date);


        long timeZone = (calDate.get(java.util.Calendar.ZONE_OFFSET) + calDate.get(java.util.Calendar.DST_OFFSET)) / 60000;
        long timeZoneCurrent = (calCurrent.get(java.util.Calendar.ZONE_OFFSET) + calCurrent.get(java.util.Calendar.DST_OFFSET)) / 60000;


        Log.v("TimetableImporter", "Date Timezone: " + timeZone);
        Log.v("TimetableImporter", "Current Timezone: " + timeZoneCurrent);

        //if(timeZone != timeZoneCurrent) {
        hour++;
        //}

*/



        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(dateProperty.getDate());
        //cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        //cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour-1);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);

        java.util.Calendar calCurrent = java.util.Calendar.getInstance();
        calCurrent.setTime(new Date());



        if(isWinterTime) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, cal.get(java.util.Calendar.HOUR_OF_DAY)-1);
        }



        /*
        long timeZoneCurrent = (calCurrent.get(java.util.Calendar.ZONE_OFFSET) + calCurrent.get(java.util.Calendar.DST_OFFSET)) / 60000;
        //if(60 != timeZoneCurrent) {//UTC+60 = Winter Time
        if(Math.abs(timeZoneCurrent - 60) != 0) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, cal.get(java.util.Calendar.HOUR_OF_DAY)+1);
        }
        */

        return cal.getTime().getTime();
    }



    public static String getBodyFromPageGet(String urlString, Context context) {
        String result = "";

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = mPrefs.getString(Constants.PREF_USERNAME_TIMETABLE, null);
        String password = mPrefs.getString(Constants.PREF_PASSWORD_TIMETABLE, null);

        if(username != null)
            urlString += "&username=" + username;
        if(password != null)
            urlString += "&password=" + password;

        try {
            URL url = new URL(urlString);

            HttpURLConnection connection;

            if(urlString.startsWith("https")) {
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setFollowRedirects(true);

                connection = (HttpsURLConnection) url.openConnection();
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestMethod("GET");

            connection.setRequestProperty("User-Agent", "Mozilla 5.0");

            int code = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));


            for (String line; (line = reader.readLine()) != null; ) {
                result += line + "\n";
            }

            reader.close();


        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.importer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;

            case R.id.action_login:
                startActivityForResult(new Intent(this, TimetableLoginActivity.class), 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadStuffFromServer();

        super.onActivityResult(requestCode, resultCode, data);
    }
}
