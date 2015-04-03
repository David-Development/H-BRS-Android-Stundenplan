package de.luhmer.stundenplanh_brsimporter.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.ExamArrayAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.ExamSyncAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Authentication.AccountGeneral;
import de.luhmer.stundenplanh_brsimporter.app.Authentication.AuthenticatorActivity;
import de.luhmer.stundenplanh_brsimporter.app.Events.ExamSyncFinishedEvent;
import de.luhmer.stundenplanh_brsimporter.app.Helper.AsyncTaskHelper;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamContent;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class ExamsFragment extends Fragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";
    private static final String TAG = "ExamsFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private int mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    @InjectView(android.R.id.list) AbsListView mListView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.ptr_layout) SwipeRefreshLayout mSwipeRefreshLayout;


    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


    public static ExamsFragment newInstance(String param1, int param2) {
        ExamsFragment fragment = new ExamsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_SECTION_NUMBER, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ExamsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getInt(ARG_SECTION_NUMBER);
        }



        /*
        mAdapter = new ArrayAdapter<ExamItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, ExamContent.ITEMS);*/
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        startUpdateProcess();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private static Account getSisAccount(Context context) {
        AccountManager mAccountManager = AccountManager.get(context);
        Account[] accounts = mAccountManager.getAccounts();
        for (int index = 0; index < accounts.length; index++) {
            if (accounts[index].type.intern().equals(AccountGeneral.ACCOUNT_TYPE)) {
                return accounts[index];
            }
        }
        return null;
    }

    private void startUpdateProcess() {

        Account account = getSisAccount(getActivity());

        if(account != null) {
            AsyncTaskHelper.StartAsyncTask(new ParseExamsAsyncTask(), (Void) null);
        } else {
            AuthenticatorActivity.StartLoginFragment(getActivity());
            //mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exams, container, false);

        ButterKnife.inject(this, view);
        //((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);


        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        return view;
    }

    public static void RequestSync(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        //bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSisAccount(context), AccountGeneral.ACCOUNT_TYPE, bundle);
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(ExamContent.ITEMS.get(position).examId);
            mListener.onFragmentInteraction(((ExamItem) mAdapter.getItem(position)).examId);
        }

        ExamItem examItem = (ExamItem) mAdapter.getItem(position);
        DialogFragment newFragment = ExamDetailFragment.newInstance(getString(R.string.title_fragment_exam_details) + " (" + examItem.examId + ")", examItem);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void onEventMainThread(ExamSyncFinishedEvent syncFinishedEvent) {
        if(syncFinishedEvent.isSuccessful())
            startUpdateProcess();
        mSwipeRefreshLayout.setRefreshing(false);
        //Toast.makeText(getActivity(), testEvent.getTestObject().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        RequestSync(getActivity());
    }


    private class ParseExamsAsyncTask extends AsyncTask<Void, Void, List<ExamItem>> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<ExamItem> doInBackground(Void... urls) {
            String examListSerialized = ExamSyncAdapter.getSharedPrefs(getActivity()).getString(ExamSyncAdapter.EXAM_LIST_CACHED_STRING, null);
            List<ExamItem> examItems = new ArrayList<ExamItem>();
            if(examListSerialized != null) {
                try {
                    examItems = (List<ExamItem>) ExamContent.fromString(examListSerialized);
                    Collections.sort(examItems);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return examItems;
        }

        @Override
        protected void onPostExecute(List<ExamItem> result) {
            mProgressBar.setVisibility(View.GONE);

            mAdapter = new ExamArrayAdapter(getActivity(), 0, result);
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
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
        public void onFragmentInteraction(int examId);
    }

}
