package co.highfive.petrolstation.hazemhamadaqa.fragment;


import androidx.fragment.app.DialogFragment;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.highfive.petrolstation.R;

public class BaseDialogFragment extends DialogFragment {

    public BaseDialogFragment(){
    }



    public String formatDateTime(String dateTime,String fromDateFormat,String toDateFormat){
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
                                    final String toTimeZoneString, final String fromDateTime) {


        final DateTimeZone fromTimeZone = DateTimeZone.forID(fromTimeZoneString);
        final DateTimeZone toTimeZone = DateTimeZone.forID(toTimeZoneString);
        final DateTime dateTime = new DateTime(fromDateTime, fromTimeZone);

        final DateTimeFormatter outputFormatter
                = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss").withZone(toTimeZone);
        return outputFormatter.print(dateTime);
    }



    public String formatDateTime(String dateTime){
        String ret_date = "";

        try{
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date = from.parse(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd   hh:mm a",Locale.ENGLISH);
            ret_date = sdf.format(date);

            ret_date = ret_date.replace("PM",getString(R.string.pm));
            ret_date = ret_date.replace("AM",getString(R.string.am));

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

            ret_date = ret_date.replace("PM",getString(R.string.pm));
            ret_date = ret_date.replace("AM",getString(R.string.am));

        }catch (Exception e){

        }
        return  ret_date;
    }

}
