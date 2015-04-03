package de.luhmer.stundenplanh_brsimporter.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.TimetableSelectGroupAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;
import de.luhmer.stundenplanh_brsimporter.app.Model.SubjectGroupEntry;


public class TimetableSelectGroupActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_select_group);

        String idSemester = getIntent().getStringExtra("idsemester");
        int idSubject = getIntent().getIntExtra("idsubject", -1);
        String title = getIntent().getStringExtra("title");
        setTitle(title);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, TimetableSelectGroupFragment.newInstance(idSemester, idSubject))
                    .commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timetable_select_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        */


        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TimetableSelectGroupFragment extends ListFragment {

        private static final String TAG = "TimetableSelectGroupFragment";
        @InjectView(R.id.ptr_layout) SwipeRefreshLayout mPullToRefreshLayout;
        String idSemester;
        int idSubject;

        public static TimetableSelectGroupFragment newInstance(String idSemester, int idSubject) {
            TimetableSelectGroupFragment fragment = new TimetableSelectGroupFragment();
            Bundle args = new Bundle();
            args.putString("idsemester", idSemester);
            args.putInt("idsubject", idSubject);
            fragment.setArguments(args);
            return fragment;
        }

        public TimetableSelectGroupFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            idSemester = getArguments().getString("idsemester");
            idSubject = getArguments().getInt("idsubject");

            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_timetable_select_group, container, false);
            ButterKnife.inject(this, rootView);

            mPullToRefreshLayout.setOnRefreshListener(onRefreshListener);
            mPullToRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            doRefresh();

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String idGroup = ((TimetableSelectGroupAdapter)getListAdapter()).getItem(i).id;

                    //TODO make the save request here!
                }
            });

            super.onViewCreated(view, savedInstanceState);
        }

        private class ReceiveGroupsAsyncTask extends AsyncTask<Void, Void, List<SubjectGroupEntry>> {

            Context context;

            public ReceiveGroupsAsyncTask(Context context) {
                this.context = context;
            }

            @Override
            protected void onPreExecute() {
                mPullToRefreshLayout.setRefreshing(true);

                super.onPreExecute();
            }

            @Override
            protected List<SubjectGroupEntry> doInBackground(Void... urls) {
                List<SubjectGroupEntry> subjectGroupEntryList = new ArrayList<SubjectGroupEntry>();
                try {
                    //Download list of semesters
                    String result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=get_groups_for_semester_subject&idsemester=" + idSemester + "&idsubject=" + idSubject, context);
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray data = jsonResult.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        String title = data.getJSONObject(i).getString("name");
                        String idGroup = data.getJSONObject(i).getString("idgroup");
                        //boolean selected = data.getJSONObject(i).optInt("group_selected") == idSubject;
                        boolean selected = false;
                        subjectGroupEntryList.add(new SubjectGroupEntry(title, selected, idGroup));
                    }


                    result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=get_group_selection&idsubject=" + idSubject, context);
                    jsonResult = new JSONObject(result);
                    data = jsonResult.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        String idGroup = data.getJSONObject(i).getString("group_idgroup");

                        for(SubjectGroupEntry entry : subjectGroupEntryList) {
                            if(entry.id.equals(idGroup)) {
                                entry.selected = true;
                                break;
                            }
                        }
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return subjectGroupEntryList;
            }


            @Override
            protected void onPostExecute(List<SubjectGroupEntry> result) {
                try {
                    //Try catch here to prevent app crash when the user exists selector before loading was finished

                    setListAdapter(new TimetableSelectGroupAdapter(getActivity(), R.layout.timetable_selector_group_list_item, result, idSubject));
                    mPullToRefreshLayout.setRefreshing(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        };

        private void doRefresh() {
            new ReceiveGroupsAsyncTask(getActivity()).execute((Void)null);
        }
    }
}
