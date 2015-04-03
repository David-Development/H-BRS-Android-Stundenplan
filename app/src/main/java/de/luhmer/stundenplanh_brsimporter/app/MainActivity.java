package de.luhmer.stundenplanh_brsimporter.app;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

import de.luhmer.stundenplanh_brsimporter.app.Authentication.AuthenticatorActivity;
import de.luhmer.stundenplanh_brsimporter.app.Helper.CalendarEventImporter;
import de.luhmer.stundenplanh_brsimporter.app.View.InterceptedSlidingPaneLayout;


public class MainActivity extends ActionBarActivity
        implements SlidingPaneFragment.NavigationDrawerCallbacks, TimetableDayFragment.OnFragmentInteractionListener,
                                                                        TimetableFragment.OnFragmentInteractionListener,
                                                                        ExamsFragment.OnFragmentInteractionListener,
                                                                        ExamDetailFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    public static final String ARG_OPEN_GRADE_VIEW = "open_grade_view";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private SlidingPaneFragment mSlidingPaneFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSlidingPaneFragment = (SlidingPaneFragment) getSupportFragmentManager().findFragmentById(R.id.sliding_pane);

        // Set up the slider.
        mSlidingPaneFragment.setUp(
                R.id.sliding_pane,
                (InterceptedSlidingPaneLayout) findViewById(R.id.sliding_pane_layout));

        if(savedInstanceState == null) {
            mTitle = getTitle();

            if (getIntent().hasExtra(ARG_OPEN_GRADE_VIEW)) {
                onNavigationDrawerItemSelected(1);//Select grade view
            }
        }
    }

    @Override
    protected void onResume() {
        getSupportActionBar().setTitle(mTitle);
        super.onResume();
    }

    TimetableFragment timetableFragment;
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        timetableFragment = null;

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        //PlaceholderFragment.newInstance(position + 1)
        Fragment fragment = null;
        if(position == 0) {
            fragment = TimetableFragment.newInstance(new Date().getTime(), position+1);
            timetableFragment = (TimetableFragment) fragment;
        } else if(position == 1) {
            fragment = ExamsFragment.newInstance(null, position+1);
        } else if(position == 2) {
            fragment = FBScheduleFragment.newInstance(null, position+1);
        } else if(position == 3) {
            fragment = ProfessorFragment.newInstance(null, position+1);
        }

        if(fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    int openedSectionId;
    public void onSectionAttached(int number) {
        openedSectionId = number;

        switch (number) {
            case 1:
                mTitle = getString(R.string.title_stundenplan);
                break;
            case 2:
                mTitle = getString(R.string.title_grades);
                break;
            case 3:
                mTitle = getString(R.string.title_fragment_fb02_schedule);
                break;
            case 4:
                mTitle = getString(R.string.title_fragment_professors);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TimetableFragment.TIME_TABLE_IMPORTER_RESULT) {
            onNavigationDrawerItemSelected(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mSlidingPaneFragment.shouldDrawerStayOpen()) {
            if(openedSectionId == 1)
                getMenuInflater().inflate(R.menu.timetable, menu);
            else if(openedSectionId == 2)
                getMenuInflater().inflate(R.menu.exam, menu);
            restoreActionBar();
            //return true;

        } else if (!mSlidingPaneFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            if(openedSectionId == 1)
                getMenuInflater().inflate(R.menu.timetable, menu);
            else if(openedSectionId == 2)
                getMenuInflater().inflate(R.menu.exam, menu);

            restoreActionBar();
            //return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(!mSlidingPaneFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_donate:
                Intent intentDonate = new Intent(MainActivity.this, DonationsActivity.class);
                startActivity(intentDonate);
                break;
            /*
            case R.id.action_settings:
                return true;*/
            case R.id.action_import:
                Intent intent = new Intent(MainActivity.this, TimetableImporterActivity.class);
                startActivityForResult(intent, TimetableFragment.TIME_TABLE_IMPORTER_RESULT);
                break;

            case R.id.action_login:
                AuthenticatorActivity.StartLoginFragment(this);
                break;

            case R.id.action_remove_items:
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                String[] events = StringUtils.split(mPrefs.getString(CalendarEventImporter.CAL_EVENT_IDS, ""), '\n');
                for(String event : events)
                    RemoveEvent(Long.parseLong(event.trim()));

                mPrefs.edit().putString(CalendarEventImporter.CAL_EVENT_IDS, "").commit();//Clear the string
                break;

            case R.id.action_sync_settings:
                String[] authorities = { "de.luhmer.stundenplanh_brsimporter" };
                Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);

                startActivity(intentSyncSettings);
                break;

            /*
            case R.id.action_jump_to_date:

                //getting current date
                Calendar cDate = Calendar.getInstance();
                mDay = cDate.get(Calendar.DAY_OF_MONTH);
                mMonth = cDate.get(Calendar.MONTH);
                mYear = cDate.get(Calendar.YEAR);


                showDialog(DATE_DIALOG_ID);
                break;
            case R.id.action_today:
                goToTodaysPage();
                break;
                */
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void RemoveEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int rows = getContentResolver().delete(deleteUri, null, null);
        Log.d(TAG, "Rows deleted: " + rows);
    }


    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(int examId) {
    }



    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TimetableFragment.DATE_DIALOG_ID:
                return timetableFragment.getDatePickerDialog();
        }
        return null;
    }

}
