package bm.iut.informatique.partielandroid.vues;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bm.iut.informatique.partielandroid.R;


public class FragmentHome extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FRAGMENT_HOME";

    private String mParam1;
    private String mParam2;

    private TextView textIsCircleDraw;
    private TextView textDummyLocation;

    private SharedPreferences prefs;

    View root = null;

    private OnFragmentInteractionListener mListener;

    public FragmentHome() {
        // Required empty public constructor
    }

    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root =  inflater.inflate(R.layout.fragment_home, container, false);

        textIsCircleDraw = root.findViewById(R.id.id_circle_dawn);
        textDummyLocation = root.findViewById(R.id.id_dummy_location);

        Log.d(TAG, "onCreateView: " + textIsCircleDraw);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        textIsCircleDraw.setText(prefs.getBoolean("cle_dessiner_cercle", true) + "");
//        float longitude = prefs.getFloat("cle_longitude", 0);
//        float latitude = prefs.getFloat("cle_latitude", 0);
//        textDummyLocation.setText("(" + longitude + " , " + latitude  + ")");
        return root;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
