package de.luhmer.stundenplanh_brsimporter.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.DatePicker;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Helper.AsyncTaskHelper;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableFragmentTuple;
import de.luhmer.stundenplanh_brsimporter.app.Model.Tuple;
import de.luhmer.stundenplanh_brsimporter.app.Model.TimetableEntry;
import de.luhmer.stundenplanh_brsimporter.app.View.InterceptedSwipeRefreshLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimetableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimetableFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimetableFragment extends Fragment {

    private final String TAG = getClass().getCanonicalName();
    @InjectView(R.id.viewPager) ViewPager mPager;
    @InjectView(R.id.ptr_layout) InterceptedSwipeRefreshLayout swipeRefreshLayout;
    private Date date;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";
    private static final String ARG_TIME_LONG = "TIME_LONG";


    private int mYear;
    private int mMonth;
    private int mDay;
    public static final int DATE_DIALOG_ID = 0;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimetableFragment.
     */
    public static TimetableFragment newInstance(String param1, int param2) {
        TimetableFragment fragment = new TimetableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_SECTION_NUMBER, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static TimetableFragment newInstance(long time, int sectionNumber) {
        TimetableFragment fragment = new TimetableFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TIME_LONG, time);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);

            date = new Date(getArguments().getLong(ARG_TIME_LONG));
            //SetReminderUpdateTimer(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        ButterKnife.inject(this, view);
        swipeRefreshLayout.setTimetableFragment(this);

        /*
        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(getSelectedListView().getFirstVisiblePosition() == 0 && (getSelectedListView().getCount() == 0 || getSelectedListView().getChildAt(0).getTop() == 0)) {
                    Log.v(TAG, "Ok");
                    return false;
                }
                return true;
            }
        });
        */

        InitAdapter();

        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
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


    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            AsyncTaskHelper.StartAsyncTask(new DownloadTimeTableTask(), (Void) null);
        }
    };

    private void InitAdapter() {
        // Instantiate a ViewPager and a PagerAdapter.
        mPager.setAdapter(new TimetablePagerAdapter(getFragmentManager()));
        mPager.setCurrentItem(182);
    }

    private class DownloadTimeTableTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            TimetableImporterActivity.GetTimeTable(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            int currentPage = mPager.getCurrentItem();
            mPager.setAdapter(new TimetablePagerAdapter(getFragmentManager()));
            mPager.setCurrentItem(currentPage);

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public AbsListView getSelectedListView() {
        TimetableDayFragment selectedFragment = (TimetableDayFragment) ((TimetablePagerAdapter)mPager.getAdapter()).getItem(mPager.getCurrentItem());
        return selectedFragment.mListView;
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
        public void onFragmentInteraction(Uri uri);
    }

    public static int TIME_TABLE_IMPORTER_RESULT = 1112;
    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TIME_TABLE_IMPORTER_RESULT) {
            InitAdapter();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_jump_to_date:
                //getting current date
                Calendar cDate = Calendar.getInstance();
                mDay = cDate.get(Calendar.DAY_OF_MONTH);
                mMonth = cDate.get(Calendar.MONTH);
                mYear = cDate.get(Calendar.YEAR);


                getActivity().showDialog(DATE_DIALOG_ID);
                break;

            case R.id.action_today:
                goToTodaysPage();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToTodaysPage() {
        goToPage(date);
    }

    public void goToPage(Date date) {
        int position = 182 - getDiffInDaysOfDates(new Date(), date);
        mPager.setCurrentItem(position);
    }

    public static int getDiffInDaysOfDates(Date date1, Date date2) {
        long newerDate = date1.getTime();
        long olderDate = date2.getTime();

        newerDate = newerDate - (newerDate % TimetableEntry.MILLIS_PER_DAY);
        olderDate = olderDate - (olderDate % TimetableEntry.MILLIS_PER_DAY);

        int diffInDays = (int)((newerDate - olderDate) / TimetableEntry.MILLIS_PER_DAY);
        return diffInDays;
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class TimetablePagerAdapter extends FragmentStatePagerAdapter {
        //RingBuffer<Long, TimetableDayFragment> items = new RingBuffer(3);
        List<TimetableFragmentTuple> items = new ArrayList<>();

        public TimetablePagerAdapter(FragmentManager fm) {
            super(fm);

            for(Fragment fragment : fm.getFragments()) {
                if(fragment instanceof  TimetableDayFragment) {
                    long date = ((TimetableDayFragment)fragment).getDate().getTime();
                    items.add(new TimetableFragmentTuple(date, (TimetableDayFragment)fragment));
                }
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date date = getDateForPosition(position);
            DateFormat dfmt = new SimpleDateFormat("E dd.MM.yy" );
            String dateString = dfmt.format(date);   // Mi., den 21.03.07 um 09:14:20
            return dateString;
        }

        private Date getDateForPosition(int position) {
            int daysDiffSinceAppStart =  getDiffInDaysOfDates(new Date(), date);

            long millisDiff = (position - 182 + daysDiffSinceAppStart) * TimetableEntry.MILLIS_PER_DAY;
            Date date = new Date(new Date().getTime() + millisDiff);
            return date;
        }

        @Override
        public Fragment getItem(int position) {
            Date date = getDateForPosition(position);
            //Log.v(TAG, "Date pre:" + date.toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            date = cal.getTime();

            //date = new Date(date.getTime() - (date.getTime() % TimetableEntry.MILLIS_PER_DAY)); //Only full days
            //Log.v(TAG, "Date post:" + date.toString());
            //Log.v(TAG, "Date key:" + date.getTime());

            Tuple selected = null;
            for(Tuple t : items) {
                if(t.key.equals(date.getTime())) {
                    selected = t;
                    break;
                }
            }

            TimetableDayFragment ttdf = null;
            if(selected != null) {
                ttdf = (TimetableDayFragment) (selected.value);

                if(ttdf == null) {
                    items.remove(selected);
                }
            }

            if(ttdf == null) {
                ttdf = TimetableDayFragment.newInstance(date.getTime());
                items.add(new TimetableFragmentTuple(date.getTime(), ttdf));
                return ttdf;
            }
            return ttdf;
        }

        @Override
        public int getCount() {
            return 365;//One year
        }
    }




    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;

                    Calendar cal = Calendar.getInstance();
                    cal.set(mYear, mMonth, mDay);

                    goToPage(cal.getTime());
                }
            };


    public Dialog getDatePickerDialog() {
        return new DatePickerDialog(getActivity(), mDateSetListener, mYear, mMonth,mDay);
    }

}
