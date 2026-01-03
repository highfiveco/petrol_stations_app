package co.highfive.petrolstation.util;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseFragmentActivity;
import co.highfive.petrolstation.hazemhamadaqa.fragment.BaseFragment;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;

public class BaseItem {
    private SessionManager manager;
    private GsonBuilder gsonBuilder;
    private Gson gson;
    Context context;
    BaseActivity baseActivity;
    BaseFragment baseFragment;
    BaseFragmentActivity baseFragmentActivity ;
    public  BaseItem(Context context, BaseActivity baseActivity, BaseFragment baseFragment, BaseFragmentActivity baseFragmentActivity){
        this.context=context;
        manager  =  new SessionManager(context);
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        this.baseActivity=baseActivity;
        this.baseFragment=baseFragment;
        this.baseFragmentActivity=baseFragmentActivity;
    }
    public ArrayList convertArrayToArrayList(Object []  objects){
        return new ArrayList<>(Arrays.asList(objects));
    }

    public SessionManager getManager() {
        return manager;
    }

    public void setManager(SessionManager manager) {
        this.manager = manager;
    }


    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public String formatDateTime1(String dateTime,String fromDateFormat,String toDateFormat){
        String ret_date = "";

        if(fromDateFormat == null){
            fromDateFormat = "yyyy-MM-dd HH:mm:ss";
        }

        if(toDateFormat == null){
            toDateFormat = "yyyy-MM-dd hh:mm a";
        }

        try{
            SimpleDateFormat from = new SimpleDateFormat(fromDateFormat, Locale.ENGLISH);
            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat(toDateFormat,Locale.ENGLISH);
            ret_date = sdf.format(date);

        }catch (Exception e){

        }

        return  ret_date;
    }

    public  String convertTimeZones(final String fromTimeZoneString,
                                    final String toTimeZoneString, final String fromDateTime,String returnFormat) {

        Log.e("fromTimeZoneString",fromTimeZoneString);
        Log.e("toTimeZoneString",toTimeZoneString);


        final DateTimeZone fromTimeZone = DateTimeZone.forID(fromTimeZoneString);
        final DateTimeZone toTimeZone = DateTimeZone.forID(toTimeZoneString);
        final DateTime dateTime = new DateTime(fromDateTime, fromTimeZone);

        if(returnFormat == null){
            returnFormat = "yyyy-MM-dd H:mm:ss";
        }
        final DateTimeFormatter outputFormatter
                = DateTimeFormat.forPattern(returnFormat).withZone(toTimeZone);
        return outputFormatter.print(dateTime);
    }

    public  String convertTimeZones(final String fromTimeZoneString,
                                    final String toTimeZoneString, final String fromDateTime) {


        final DateTimeZone fromTimeZone = DateTimeZone.forID(fromTimeZoneString);
        final DateTimeZone toTimeZone = DateTimeZone.forID(toTimeZoneString);
        final DateTime dateTime = new DateTime(fromDateTime, fromTimeZone);

        final DateTimeFormatter outputFormatter
                = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss").withZone(toTimeZone);
        return outputFormatter.print(dateTime);
    }


    public String formatDateTime(String dateTime,String fromDateFormat,String toDateFormat){
        String ret_date = "";

        if(fromDateFormat == null){
            fromDateFormat = "yyyy-MM-dd HH:mm:ss";
        }

        if(toDateFormat == null){
            toDateFormat = "yyyy-MM-dd   hh:mm a";
        }

        try{
            SimpleDateFormat from = new SimpleDateFormat(fromDateFormat, Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat(toDateFormat,Locale.ENGLISH);
            ret_date = sdf.format(date);

        }catch (Exception e){

        }


        return  ret_date;
    }

    public String formatDateTime(String dateTime){
        String ret_date = "";

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd   hh:mm a",Locale.ENGLISH);
            ret_date = sdf.format(date);

//            ret_date= ret_date.replace("١","1").
//                    replace("٢","2")
//                    .replace("٣","3")
//                    .replace("٤","4")
//                    .replace("٥","5")
//                    .replace("٦","6")
//                    .replace("٧","7")
//                    .replace("٨","8")
//                    .replace("٩","9")
//                    .replace("٠","0");

            if(baseActivity != null){
                ret_date = ret_date.replace("PM",baseActivity.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseActivity.getString(R.string.am));
            }else if(baseFragment != null){
                ret_date = ret_date.replace("PM",baseFragment.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseFragment.getString(R.string.am));
            }else if(baseFragmentActivity != null){
                ret_date = ret_date.replace("PM",baseFragmentActivity.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseFragmentActivity.getString(R.string.am));
            }else{
                ret_date = ret_date.replace("PM",context.getString(R.string.pm));
                ret_date = ret_date.replace("AM",context.getString(R.string.am));
            }


        }catch (Exception e){

        }


        return  ret_date;
    }

    public String formatDate(String dateTime){
        String ret_date = "";

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
            ret_date = sdf.format(date);
        }catch (Exception e){

        }


        return  ret_date;
    }

    public String formatTime(String dateTime,String format){
        String ret_date = "";
        if(format == null){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        try{
            SimpleDateFormat from = new SimpleDateFormat(format, Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);

            ret_date = sdf.format(date);

            if(baseActivity != null){
                ret_date = ret_date.replace("PM",baseActivity.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseActivity.getString(R.string.am));
            }else if(baseFragment != null){
                ret_date = ret_date.replace("PM",baseFragment.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseFragment.getString(R.string.am));
            }else if(baseFragmentActivity != null){
                ret_date = ret_date.replace("PM",baseFragmentActivity.getString(R.string.pm));
                ret_date = ret_date.replace("AM",baseFragmentActivity.getString(R.string.am));
            }else{
                ret_date = ret_date.replace("PM",context.getString(R.string.pm));
                ret_date = ret_date.replace("AM",context.getString(R.string.am));
            }


        }catch (Exception e){

        }
        return  ret_date;
    }

    public String getStringResourceByName(String aString) {
        try{
            String packageName = "";
            int resId = 0;

            if(baseActivity != null){
                packageName = baseActivity.getPackageName();
                resId = baseActivity.getResources().getIdentifier(aString, "string", packageName);
                return baseActivity.getString(resId);
            }else if(baseFragment != null){
                packageName = baseFragment.getActivity().getPackageName();
                resId = baseFragment.getActivity().getResources().getIdentifier(aString, "string", packageName);
                return baseFragment.getActivity().getString(resId);
            }else if(baseFragmentActivity != null){
                resId = baseFragmentActivity.getResources().getIdentifier(aString, "string", packageName);
                return baseFragmentActivity.getString(resId);
            }


            return "";
        }catch (Exception e){
            return "";
        }


    }
    public Spanned getHtmlText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return  Html.fromHtml(text);
        }
    }

    public  void disableLayout(ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                disableLayout((ViewGroup) child);
            } else {
                child.setEnabled(false);
            }
        }
    }

    public  void enableLayout(ViewGroup layout) {
        layout.setEnabled(true);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                enableLayout((ViewGroup) child);
            } else {
                child.setEnabled(true);
            }
        }
    }


}
