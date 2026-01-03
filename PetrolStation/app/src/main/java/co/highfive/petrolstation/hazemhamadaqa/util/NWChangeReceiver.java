package co.highfive.petrolstation.hazemhamadaqa.util;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;


public class NWChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (isAppForground(context)) {
            // App is in Foreground
            Log.e("isAppForground","True");
            String status = NetworkUtil.getConnectivityStatusString(context);

            if(status.equals("Wifi enabled")){
               /* Intent i=new Intent(context,SplashActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);*/
                Toast.makeText(context, status, Toast.LENGTH_LONG).show();
                Log.e("NWChangeReceiver",""+status);
            }else if(status.equals("Not connected to Internet")){
               /* Intent i=new Intent(context,NWCheckActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);*/
                Toast.makeText(context, status, Toast.LENGTH_LONG).show();
                Log.e("NWChangeReceiver",""+status);
            }
        } else {
            // App is in Background
            Log.e("isAppForground","False");
        }


    }

    public boolean isAppForground(Context mContext) {

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }

        return true;
    }
}
