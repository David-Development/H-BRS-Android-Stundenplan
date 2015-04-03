package de.luhmer.stundenplanh_brsimporter.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.TimetableEntryAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TimetableDayFragment extends Fragment implements AbsListView.OnItemClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DATE_LONG = "ARG_DATE_LONG";
    private final String TAG = getClass().getCanonicalName();

    private List<TimetableEntry> entries;
    private OnFragmentInteractionListener mListener;
    private Handler handler;
    private Date mDate;

    /**
     * The fragment's ListView/GridView.
     */
    //@InjectView(R.id.fr_layout_tt) FrameLayout mFrameLayoutTimetable;
    @InjectView(android.R.id.list) AbsListView mListView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.img_no_items) ImageView imgNoItems;
    @InjectView(R.id.tv_no_items) TextView tvNoItems;


    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private TimetableEntryAdapter mAdapter;

    public static TimetableDayFragment newInstance(long dateInMillis) {
        TimetableDayFragment fragment = new TimetableDayFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE_LONG, dateInMillis);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TimetableDayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setRetainInstance(true);

        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);

            mDate = new Date(getArguments().getLong(ARG_DATE_LONG));
            Context context = getActivity();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new LoadTimetableEntrysAsyncTask(mDate, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else {
                new LoadTimetableEntrysAsyncTask(mDate, context).execute();
            }
        }


    }

    Runnable updateListView = new Runnable() {
        @Override
        public void run() {
            int count = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition();
            for(int i = 0; i < count; i++)
                mAdapter.notifyDataSetChanged();
            handler.postDelayed(this, 60 * 1000);
        }
    };

    @Override
    public void onResume() {
        int diffDays = TimetableFragment.getDiffInDaysOfDates(new Date(), mDate);
        if(diffDays == 0) {
            handler = new Handler();
            handler.postDelayed(updateListView, 60 * 1000);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        if(handler != null){
            handler.removeCallbacks(updateListView);
            handler = null;
        }

        super.onPause();
    }

    public Date getDate() {
        return mDate;
    }

    private class LoadTimetableEntrysAsyncTask extends AsyncTask<Void, Void, Void> {
        Date date;
        Context context;

        public LoadTimetableEntrysAsyncTask(Date date, Context context) {
            this.date = date;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create a period starting now with a duration of one (1) day..
            Period period = new Period(new DateTime(date), new Dur(1, 0, 0, 0));
            Filter filter = new Filter(new PeriodRule(period));

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String cal_Data = mPrefs.getString(TimetableImporterActivity.calendarString, "");

            List<TimetableEntry> entries = TimetableImporterActivity.getTimetableEntrysByCalendar(context, cal_Data, filter);
            Collections.sort(entries);

            String[] stringsToRemove = new String[] { "(Übung)", "(Vorlesung)" };

            for(TimetableEntry entry : entries) {
                for(String remove : stringsToRemove)
                    entry.summary = entry.summary.replace(remove, "").trim();

                entry.location = entry.location.replace("Hochschule Bonn-Rhein-Sieg, Raum:", "").trim();
            }

            TimetableDayFragment.this.entries = entries;

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter = new TimetableEntryAdapter(context, R.layout.timetable_list_item, entries, date);

            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

            mProgressBar.setVisibility(View.GONE);

            if(entries.size() <= 0) {
                imgNoItems.setVisibility(View.VISIBLE);
                tvNoItems.setVisibility(View.VISIBLE);
            }
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetableday, container, false);

        ButterKnife.inject(this, view);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction((mAdapter.getItem(position)).description);//TODO this is crap
        }
    }


    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }
}
