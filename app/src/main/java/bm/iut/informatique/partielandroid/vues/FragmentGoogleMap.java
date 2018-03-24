package bm.iut.informatique.partielandroid.vues;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import bm.iut.informatique.partielandroid.R;

public class FragmentGoogleMap extends Fragment implements
        OnMapReadyCallback {

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
        serviceLocalisationClient = LocationServices.getFusedLocationProviderClient(getContext());
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
                        }
                    }
                });

        //A chaque nouvelle mise à jour de la localisation, le code passe dans cette méthode
        miseAJourLocalisation = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult resultatLocalisation) {
                for (Location localisation : resultatLocalisation.getLocations()) {
                }
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



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
