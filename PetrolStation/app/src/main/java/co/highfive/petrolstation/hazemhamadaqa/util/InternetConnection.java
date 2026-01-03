package co.highfive.petrolstation.hazemhamadaqa.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;


public class InternetConnection  {

    ConnectivityManager cm;
    NetworkInfo ni;
    public boolean isNetworkConnected(Context context) {
        try {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            ni = cm.getActiveNetworkInfo();

            if (ni == null) {
                // There are no active networks.
                return false;
            } else
                return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            if (ipAddr.equals("")) {

                return false;
            } else {

                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }
}