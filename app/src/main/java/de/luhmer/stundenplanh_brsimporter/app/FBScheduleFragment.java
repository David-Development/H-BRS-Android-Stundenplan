package de.luhmer.stundenplanh_brsimporter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.stundenplanh_brsimporter.app.Adapter.FB02ScheduleArrayAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.ProfessorArrayAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;
import de.luhmer.stundenplanh_brsimporter.app.Helper.SSLHelper;
import de.luhmer.stundenplanh_brsimporter.app.Model.FB02ScheduleEntry;
import de.luhmer.stundenplanh_brsimporter.app.Model.ProfessorEntry;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FBScheduleFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static FBScheduleFragment newInstance(String param1, int param2) {
        FBScheduleFragment fragment = new FBScheduleFragment();
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
    public FBScheduleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        initAdapter();
    }


    private void initAdapter() {
        SSLHelper.AllowAllSSLCertificates();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Constants.API_URL + "?action=fb02_schedule";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);


                            List<FB02ScheduleEntry> entries = new ArrayList<>();
                            JSONArray jsonArr = json.getJSONArray("data");

                            JSONObject jObj;
                            for(int i = 0; i < jsonArr.length(); i++) {
                                jObj = ((JSONObject)jsonArr.get(i));
                                String startDate = jObj.optString("startdate");
                                String endDate = jObj.getString("enddate");
                                String description = jObj.getString("desc");
                                String orig_date = jObj.getString("orig_date");

                                entries.add(new FB02ScheduleEntry(startDate, endDate, description, orig_date));
                            }

                            setListAdapter(new FB02ScheduleArrayAdapter(getActivity(), R.layout.fb02_schedule_list_item, entries));
                        } catch (JSONException e) {
                            e.printStackTrace();

                            setListAdapter(new FB02ScheduleArrayAdapter(getActivity(), R.layout.fb02_schedule_list_item, new ArrayList<FB02ScheduleEntry>()));
                            setEmptyText(e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                setListAdapter(new FB02ScheduleArrayAdapter(getActivity(), R.layout.fb02_schedule_list_item, new ArrayList<FB02ScheduleEntry>()));
                setEmptyText(error.getLocalizedMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            //throw new ClassCastException(activity.toString()
            //        + " must implement OnFragmentInteractionListener"); //TODO implement this
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FB02ScheduleEntry scheduleEntry = ((FB02ScheduleArrayAdapter)getListAdapter()).getItem(position);
        String text = scheduleEntry.original_time + "\n\n" + scheduleEntry.description;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Details")
                .setMessage(text)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();


        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(FB02ScheduleEntry.ITEMS.get(position).id); //TODO FIX THIS HERE!
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
