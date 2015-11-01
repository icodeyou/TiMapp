package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.RestFeedback;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.Feedback;
import com.timappweb.timapp.utils.MyLocationListener;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;

import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddSpotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddSpotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddSpotFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "AddSpotFragment";

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
     * @return A new instance of fragment AddSpotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddSpotFragment newInstance(String param1, String param2) {
        AddSpotFragment fragment = new AddSpotFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AddSpotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating add spot fragment");
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        mResultReceiver = new AddressResultReceiver(new Handler());
        this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage("Please wait...");

        this.lp = new MyLocationProvider(getActivity());
        this.lp.getUserLocation(mLocationListener);

        Log.d(TAG, "Location provider has been setBounds up: " + lp);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_spot, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "OnViewCreated");


        if (savedInstanceState == null){

        }
        else{
            Log.d(TAG, "Instance saved for map fragment");
        }

        Button b = (Button) getView().findViewById(R.id.button_submit_spot);

        final AddSpotFragment that = this;
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                that.broadcastPosition();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.d(TAG, activity.toString()
                    + " must implement OnFragmentInteractionListener");
            //throw new ClassCastException(activity.toString()
            //        + " must implement OnFragmentInteractionListener");
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






    ProgressDialog dialog;


    public void broadcastPosition(){
        // 1) Get the user position
        // if (! this.lp.hasKnownPosition()){
        Location location;
        try{
            location = this.lp.getLastLocation();
        }
        catch (NoLastLocationException ex){
            Log.i(TAG, "Cannot get position.");
            AlertDialog.show(getActivity(), R.string.error_cannot_get_location);
            return;
        }

        //Location location = lp.getLastLocation();
        // if precision sucks..
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "User position is: " + ll + " with an accuracy of " + location.getAccuracy());

        // 2) Get input tags
        String inputTags = ((EditText) getView().findViewById(R.id.input_tags)).getText().toString();
        Log.d(TAG, "User tags: " + inputTags);

        // 3) Call the service to add the spot
        // - Build the spot
        final Spot spot = new Spot(ll);
        spot.tag_string = inputTags;
        Log.d(TAG, "Building spot: " + spot);
        RestClient.instance().getService().addSpot(spot, new RestCallback<RestFeedback>() {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                dialog.hide();

                if (restFeedback.success){
                    Log.i(TAG, "Request post success: " + response);
                    Object o = restFeedback.data.get("expired");
                    Log.i(TAG, "-->" + o);
                    spot.tag_string = restFeedback.data.get("tag_string");
                    //spot.setExpired(Long.valueOf(restFeedback.data.get("expired")));
                    spot.setCreated(Integer.valueOf(restFeedback.data.get("created"))); // TODO check exception ClassCastException + NullPointerException
                    spot.writeToPref();
                    Feedback.show(getActivity().getApplicationContext(), R.string.feedback_webservice_add_spot);
                    dialog.hide();

                    // TODO change view
                    //Intent intent = new Intent(getActivity().getApplicationContext(), CurrentSpotActivity.class);
                    //startActivity(intent);
                }
                else{
                    Log.i(TAG, "Cannot add spot: " + response);
                    Feedback.show(getActivity().getApplicationContext(), R.string.error_webservice_connection);
                }
            }

        });
    }

    private final LocationListener mLocationListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.i(TAG, "User location has changed: " + Util.print(location));
            startIntentService(location);
        }
    };
    private MyLocationProvider lp;

    private AddressResultReceiver mResultReceiver;

    protected void startIntentService(Location location) {
        Log.d(TAG, "Starting IntentService to get use adress from location");
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        getActivity().startService(intent);
    }

    private void displayAddressOutput() {
        TextView tvUserLocation = (TextView) getView().findViewById(R.id.tv_user_location);
        tvUserLocation.setText(this.mAddressOutput);
    }

    private String mAddressOutput;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG);
            }

        }

    }
}
