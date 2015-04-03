package de.luhmer.stundenplanh_brsimporter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Model.ExamItem;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExamDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExamDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ExamDetailFragment extends DialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_EXAM = "ARG_EXAM";

    DecimalFormat formatter;
    DecimalFormatSymbols symbols;
    DateFormat dateFormat;

    private String mTitle;
    private ExamItem mExam;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment RouteDetailFragment.
     */

    public static ExamDetailFragment newInstance(String title, ExamItem exam) {
        ExamDetailFragment fragment = new ExamDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putSerializable(ARG_EXAM, exam);
        fragment.setArguments(args);
        return fragment;
    }
    public ExamDetailFragment() {
        // Required empty public constructor

        formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
        symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        dateFormat = new SimpleDateFormat();
    }


    @InjectView(R.id.tv_description) TextView tvDescription;
    @InjectView(R.id.tv_tryCount) TextView tvTryCount;
    @InjectView(R.id.tv_date) TextView tvDate;
    @InjectView(R.id.tv_time) TextView tvTime;
    @InjectView(R.id.tv_cp) TextView tvCreditPoints;


    @InjectView(R.id.tv_room) TextView tvRoom;
    @InjectView(R.id.tv_help_material) TextView tvHelpMaterial;
    @InjectView(R.id.tv_grade) TextView tvGrade;

    @InjectView(R.id.tv_note) TextView tvNote;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mExam = (ExamItem) getArguments().getSerializable(ARG_EXAM);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = getActivity().getLayoutInflater().cloneInContext(contextThemeWrapper);
        View view = localInflater.inflate(R.layout.fragment_exam_detail, null);
        ButterKnife.inject(this, view);

        tvDescription.setText(mExam.fachName);
        tvTryCount.setText("Versuch: " + mExam.versuch);
        tvDate.setText("Termin: " + DateFormatUtils.format(mExam.terminKlausur, "dd.MM.yyyy") + " (" + mExam.termin + ")");

        tvGrade.setText("Note: " + mExam.note);
        tvNote.setText("Vermerk: " + mExam.vermerk + " - " + mExam.freiVermerk);

        tvCreditPoints.setText("CP: " + mExam.credits);

        if(mExam.examRegistration != null) {
            tvTime.setText("Zeit: " + (mExam.examRegistration.raum.isEmpty() ? "-" : mExam.examRegistration.zeit));
            String raum = mExam.examRegistration.raum.isEmpty() ? "-" : mExam.examRegistration.raum;
            String hilfsmittel = mExam.examRegistration.hilfsmittel.isEmpty() ? "-" : mExam.examRegistration.hilfsmittel;
            tvRoom.setText("Raum: " + raum);
            tvHelpMaterial.setText("Hilfsmittel: " + hilfsmittel);
        } else {
            tvTime.setVisibility(View.GONE);
            tvRoom.setVisibility(View.GONE);
            tvHelpMaterial.setVisibility(View.GONE);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        return builder
                .setTitle(mTitle)
                .setNeutralButton(android.R.string.ok, null)
                .setView(view)
                .create();
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

}
