package bm.iut.informatique.partielandroid.vues;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import bm.iut.informatique.partielandroid.R;
import bm.iut.informatique.partielandroid.outils.GetHttp;

public class FragmentGoogleMap extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private final String TAG = "FRAGMENT GOOGLE MAP :";

    private FusedLocationProviderClient serviceLocalisationClient;
    private LocationRequest requeteLocalisation;
    private LocationCallback miseAJourLocalisation;

    private final int CODE_REQUETE = 1;

    private GoogleMap googleMapCourante;
    private GoogleApiClient mGoogleApiClient;

    Location dernierePosition;

    Boolean  mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest = null;
    float mRadius = 0;

    private static final int REQUEST_LOCATION_PERMISSION = 2;


    BroadcastReceiver requeteApi;

    public FragmentGoogleMap() {
        // Required empty public constructor
    }

    public static FragmentGoogleMap newInstance(String param1, String param2) {
        FragmentGoogleMap fragment = new FragmentGoogleMap();
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

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        serviceLocalisationClient = LocationServices.getFusedLocationProviderClient(getContext());

        requeteApi = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("bonsoir");
                Toast.makeText(context, intent.getExtras().getString("json data"), Toast.LENGTH_SHORT).show();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //On récumère l'objet Map et on la fait passée dans le OnMapReady
        SupportMapFragment mapCourante;
        View vue = inflater.inflate(R.layout.fragment_google_map, container, false);
        mapCourante = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapCourante.getMapAsync(this);
        return vue;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapCourante = googleMap;
        demanderPermissionsUtilisateur();
        creerRequeteLocalisation();
    }

    /**
     * Méthode qui défini les paramètres de la localisation
     */
    @SuppressLint("RestrictedApi")
    protected void creerRequeteLocalisation() {
        requeteLocalisation = new LocationRequest();
        requeteLocalisation.setInterval(10000);
        requeteLocalisation.setFastestInterval(5000);
        requeteLocalisation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void demanderPermissionsUtilisateur() {
        //Demande la permission à l'utilisateur pour utiliser la localisation
        String permissions[] = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ActivityCompat.checkSelfPermission(getContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            activerToutLesServicesDeLocalisation();
        } else {
            requestPermissions(permissions, CODE_REQUETE);
        }
    }

    /**
     * Méthode appelé après le résultat de la demande de permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CODE_REQUETE) {
            if (permissions.length == 2 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    activerToutLesServicesDeLocalisation();
            } else {
                Toast.makeText(getContext(), "L'application ne fonctionnera pas si vous n'acceptez pas les demandes de permissions", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Active tout les services de localisation nécéssaire pour la GoogleMap
     */
    @SuppressLint("MissingPermission")
    private void activerToutLesServicesDeLocalisation() {
        googleMapCourante.setMyLocationEnabled(true);

        //Ecouteur pour surcharger la méthode de clique du calque de localisation
        googleMapCourante.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public boolean onMyLocationButtonClick() {
                serviceLocalisationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location localisation) {
                        if(localisation != null) {
                            changerLocalisationCamera(localisation, 17);
                        }
                    }
                });
                return true;
            }
        });

        // Récupère la dernière localisation connu au démarrage de l'application
        serviceLocalisationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location localisation) {
                        if (localisation != null) {
                            changerLocalisationCamera(localisation, 17);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(localisation.getLatitude(), localisation.getLongitude()));
                            markerOptions.title("Altitude");
                            markerOptions.snippet(localisation.getAltitude()+"");
                            googleMapCourante.addMarker(markerOptions);
                        }
                    }
                });

        //A chaque nouvelle mise à jour de la localisation, le code passe dans cette méthode
        miseAJourLocalisation = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult resultatLocalisation) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(resultatLocalisation.getLastLocation().getLatitude(), resultatLocalisation.getLastLocation().getLongitude()));
                markerOptions.title("Altitude");
                markerOptions.snippet(resultatLocalisation.getLastLocation().getAltitude()+"");
                googleMapCourante.addMarker(markerOptions);
            }
        };

        //Active la récupération régulière des données de localisation
        serviceLocalisationClient.requestLocationUpdates(requeteLocalisation, miseAJourLocalisation, null);
    }
    /**
     * Méthode qui déplace la caméra vers une nouvelle localisation
     * @param nouvelleLocalisation
     */
    private void changerLocalisationCamera(Location nouvelleLocalisation, int niveauZoom) {
        CameraUpdate pointACentrer = CameraUpdateFactory.newLatLng(new LatLng(nouvelleLocalisation.getLatitude(), nouvelleLocalisation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(niveauZoom);
        googleMapCourante.moveCamera(pointACentrer);
        googleMapCourante.animateCamera(zoom);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("je passe ici");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            demanderPermissionsUtilisateur();

        } else {
            // permission has been granted, continue as usual
            dernierePosition = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (dernierePosition != null) {

                String lats = "" + dernierePosition.getLatitude();
                String longs = "" + dernierePosition.getLongitude();
                String alt = "" + dernierePosition.getAltitude();

            }

            /*
            double elevation;
            double lat = 39.7391536;
            double longi = -104.9847034;
            String key = "AIzaSyDTKVdoPOF_l2GVO2WAd_722fzpzYMbBGc";
            String url = "https://maps.googleapis.com/maps/api/elevation/json?locations="+lat+","+longi+"&key="+key;
            StringBuffer response = new StringBuffer();

            int ret = GetHttp.GetHttpToServer(url,response);

            if (ret == 0){
                try {
                    JSONObject jsonObj = new JSONObject(response.toString());
                    JSONArray resultEl = jsonObj.getJSONArray("results");
                    JSONObject current = resultEl.getJSONObject(0);
                    elevation = Double.parseDouble(current.getString("elevation"));
                    System.out.println(elevation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            */
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

    }

    @Override
    public void onResume() {
        super.onResume();
        setLocationParmeters();
        if (mGoogleApiClient.isConnected()){
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        }
        getContext().registerReceiver(requeteApi,
                new IntentFilter(getResources().getString(R.string.key_requete_api_intent)));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(requeteApi);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();

        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, (LocationListener) this);
        }
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(
                getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (LocationListener) this);
    }

    @SuppressLint("RestrictedApi")
    private void setLocationParmeters(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRequestingLocationUpdates = sharedPref.getBoolean(getResources().getString(R.string.key_location_switch), false);
        mRadius = Float.parseFloat(sharedPref.getString(getResources().getString(R.string.key_search_radius),"0"));

        if (mRequestingLocationUpdates) {
            int interval = Integer.parseInt(sharedPref.getString(getResources().getString(R.string.key_search_delay), "10"));
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000 * interval);
            mLocationRequest.setFastestInterval(1000 * interval / 2);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private String getAltitudePointDemande(){
        String r = null;

        try {
            String urls = "https://maps.googleapis.com/maps/api/elevation/json?locations=39.7391536,-104.9847034&key=AIzaSyDTKVdoPOF_l2GVO2WAd_722fzpzYMbBGc";

            URL url = new URL(urls);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();
            r = convertStreamToString(conn.getInputStream());
            System.out.println(r);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return r;
    }

    private String convertStreamToString(InputStream in){
        String reponse = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer =new byte[1024];

        try {
            for (int count; (count = in.read(buffer)) != -1;){
                out.write(buffer, 0, count);
            }

            byte[] response = out.toByteArray();
            reponse = new String(response, "UTF-8");
        } catch (Exception e){

        }
        return reponse;
    }
}
