package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Spot;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpotItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpotItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpotItemFragment extends Fragment {
    private static final String TAG = "SpotItemFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;


    private static final String DESCRIBABLE_KEY = "com.timappweb.timapp.SPOT_ITEM_KEY";
    private Spot mSpot;
    private View mView;

    public static SpotItemFragment newInstance(Spot spot) {
        SpotItemFragment fragment = new SpotItemFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, spot);
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpotItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpotItemFragment newInstance(String param1, String param2) {
        SpotItemFragment fragment = new SpotItemFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SpotItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //mSpot = (Spot) getArguments().getSerializable(DESCRIBABLE_KEY);
        mView = inflater.inflate(R.layout.item_spot, container, false);
        return mView;
    }

    public void setSpot(Spot spot){
        mSpot = spot;
        if (mSpot != null){
            // Lookup view for data population
            TextView tvName = (TextView) mView.findViewById(R.id.tv_username);
            tvName.setText(String.valueOf(mSpot.user_id));

            TextView tvTags = (TextView) mView.findViewById(R.id.tv_tags);
            tvTags.setText(mSpot.tag_string);

            TextView tvCreated = (TextView) mView.findViewById(R.id.tv_created);
            tvCreated.setText(mSpot.getCreatedDate());
        }
        else{
            Log.i(TAG, "No spot defined for the fragment");
        }
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
