package co.highfive.petrolstation.hazemhamadaqa.Http;

/**
 * Created by Eng. Hazem Hamadaqa on 9/28/2016.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;

/**
 * This class contains static utility methods.
 */
public class InternetConnection  {

    ConnectivityManager cm;
    NetworkInfo ni;
    public boolean isNetworkConnected(Context context) {
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            ni = cm.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                Log.e("Connection", "Active networks");
                return true;
            } else{ // There are no active networks.
                Log.e("ErrorConnection", "There are no active networks");
                return false;
               }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL(AppConfig.testUrl);
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                Log.e("http_code",""+urlc.getResponseCode());
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                Log.i("warning", "Error checking internet connection", e);
                return false;
            }
        }

        return false;

    }

}