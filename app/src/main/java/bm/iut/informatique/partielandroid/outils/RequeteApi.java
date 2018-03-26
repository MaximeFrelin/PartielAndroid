package bm.iut.informatique.partielandroid.outils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;

import bm.iut.informatique.partielandroid.R;

public class RequeteApi extends AsyncTask {

    Context mContext;
    Location lo = null;

    public RequeteApi(Context mContext, Location lo){
        super();
        this.mContext = mContext;
        this.lo = lo;
    }


    @Override
    protected Object doInBackground(Object[] objects) {
        String st = "json data";
        Intent i = new Intent(mContext.getResources().getString(R.string.key_requete_api_intent));
        i.putExtra(mContext.getResources().getString(R.string.key_requete_api_path),st);
        mContext.sendBroadcast(i);
        return null;
    }
}
