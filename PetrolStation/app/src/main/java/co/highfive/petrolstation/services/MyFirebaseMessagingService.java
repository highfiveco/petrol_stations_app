package co.highfive.petrolstation.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.activities.SplashActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.Constant;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionKeys;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private SessionManager sessionManager;
    private SessionKeys sessionKeys;
    GsonBuilder gsonBuilder;
    Gson gson;
    public static final String FCM_PARAM = "picture";
    private static final String CHANNEL_NAME = "FCM";
    private static final String CHANNEL_DESC = "a Firebase Cloud Messaging";
    private int numMessages = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = "";
        String body = "";
        String type_notification  = "";
        String data_id  = "";
        String customer_name  = "";
        String customer_mobile  = "";
        String customer_account_id  = "";
        String customer_id  = "";

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {

            try{

                if( remoteMessage.getData().containsKey("type_notification")){
                    type_notification = remoteMessage.getData().get("type_notification");
                }else{
                    type_notification= null;
                }

                if( remoteMessage.getData().containsKey("data_id")){
                    data_id = remoteMessage.getData().get("data_id");
                }else{
                    data_id= null;
                }

                if( remoteMessage.getData().containsKey("customer_name")){
                    customer_name = remoteMessage.getData().get("customer_name");
                }else{
                    customer_name= null;
                }
                if( remoteMessage.getData().containsKey("customer_mobile")){
                    customer_mobile = remoteMessage.getData().get("customer_mobile");
                }else{
                    customer_mobile= null;
                }
                if( remoteMessage.getData().containsKey("customer_account_id")){
                    customer_account_id = remoteMessage.getData().get("customer_account_id");
                }else{
                    customer_account_id= null;
                }
                if( remoteMessage.getData().containsKey("customer_id")){
                    customer_id = remoteMessage.getData().get("customer_id");
                }else{
                    customer_id= null;
                }


                title = remoteMessage.getData().get("title");
                body = remoteMessage.getData().get("body");

            }catch (Exception e){

            }

            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Intent intent= new Intent();
        intent.setAction(Constant.ACTION_NEW_NOTIFICATION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        Log.d("FROM", remoteMessage.getFrom());


        sendNotification(notification, data,title,body,type_notification,data_id,customer_name,customer_mobile,customer_account_id,customer_id);
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]



    @Override
    public void onNewToken(String token) {
        Log.e(TAG, "Refreshed token: " + token);
        sessionManager =  new SessionManager(getApplicationContext());
        sessionKeys =  new SessionKeys();

        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        sessionManager.setString(sessionKeys.notification_token,token);
        if(sessionManager.getBoolean(sessionKeys.isLogin)){
            sendRegistrationToServer(token);
        }
    }

    private void sendRegistrationToServer(String deviceToken) {
        // TODO: Implement this method to send token to your app server.

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();
        

        String token  =sessionManager.getString(sessionKeys.token);

        if(token!= null){
            headerKeys.add("Authorization");
            headerValues.add(""+token);
        }

        keys.add("fcm_token");
        values.add(""+deviceToken);

        keys.add("os");
        values.add("android");

        headerKeys.add("PLATFORM");
        headerValues.add("ANDROID");

    }

    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data, String title, String body, String type_notification, String data_id, String customer_name, String customer_mobile, String customer_account_id, String customer_id) {
        Intent intent = null;

        Bundle bundle = new Bundle();
        if(type_notification != null){
            bundle.putString("type_notification",type_notification);
            bundle.putString("data_id",data_id);
            bundle.putString("customer_name",customer_name);
            bundle.putString("customer_mobile",customer_mobile);
            bundle.putString("customer_account_id",customer_account_id);
            bundle.putString("customer_id",customer_id);

            intent = new Intent(this, SplashActivity.class);

        }else{
            intent = new Intent(this, SplashActivity.class);
        }


        intent.putExtras(bundle);

        String tempTitle = "";
        String tempBody = "";

        if(title != null && !title.isEmpty()){
            tempTitle = title;
        }else{
            tempTitle = notification.getTitle();
        }

        if(body != null && !body.isEmpty()){
            tempBody = body;
        }else{
            tempBody = notification.getBody();
        }


        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification);

        Log.e("soundUri",""+soundUri.toString());
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int defaults = 3;
        defaults &= -2;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,getString(R.string.channel_id))
                .setContentTitle(tempTitle)
                .setContentText(tempBody)
                .setAutoCancel(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentInfo("")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(Color.parseColor("#000000"))
                .setLights(Color.BLUE, 1000, 300)
                .setNumber(++numMessages)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri)
                .setDefaults(defaults);


        try {
            String picture = data.get(FCM_PARAM);
            if (picture != null && !"".equals(picture)) {
                URL url = new URL(picture);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        }

        assert notificationManager != null;
        notificationManager.notify(0, notificationBuilder.build());
    }
}