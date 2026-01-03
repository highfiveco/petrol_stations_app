package co.highfive.petrolstation.util;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;

import java.util.Calendar;

/**
 * Created by Eng. Hazem Hamadaqa on 3/31/2017.
 */

public class DateHigriMelady {
    private String day;
    private String month;
    private String year;

    public DateHigriMelady(){
    }

    public DateHigriMelady(String day, String month, String year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public DateTime addDayToMeladyDate(DateTime dtIslamic,int day){
        return dtIslamic.plusDays(day);
    }
    public DateTime addDayToHigriDate(DateTime dtIslamic,int day){
        return dtIslamic.plusDays(day);
    }
    public DateTime getMeladyDate(String year,String month,String day){

        Chronology iso = ISOChronology.getInstanceUTC();
        Chronology hijri = IslamicChronology.getInstanceUTC();
        DateTime dtHijri = new DateTime(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),22,22,hijri);

        DateTime dtIso = new DateTime(dtHijri, iso);
        return dtIso;
    }

    public DateTime getHigriDate(String year,String month,String day){
        try{
            DateTime dtISO = new DateTime(Integer.parseInt(year), Integer.parseInt(month)+1, Integer.parseInt(day), 12, 0, 0, 0);

            // find out what the same instant is using the Islamic Chronology
            DateTime dtIslamic = dtISO.withChronology(IslamicChronology.getInstance());
            return dtIslamic;
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);

            return null;
        }

    }

    public DateTime getHigriDate(Calendar today){
        DateTime dtISO = new DateTime(today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1, today.get(Calendar.DAY_OF_MONTH), 12, 0, 0, 0);

        // find out what the same instant is using the Islamic Chronology
        DateTime dtIslamic = dtISO.withChronology(IslamicChronology.getInstance());
        return dtIslamic;
    }
    public DateHigriMelady getlsamicDate(Calendar today,int days) {
        DateTime dateTime=getHigriDate(today);
        dateTime=addDayToHigriDate(dateTime,days);
        String[] iMonthNames = {"محرم", "صفر", "ربيع الاول",
                "ربيع الثاني", "جمادي الاول", "جمادي الثاني", "رجب",
                "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};
        DateHigriMelady dateHigriMelady =null;
        if(dateTime!=null){
            String d=dateTime.getDayOfMonth()+"";
            if(d.length() ==1){
                d="0"+d;
            }
            dateHigriMelady = new DateHigriMelady(d+"",iMonthNames[dateTime.getMonthOfYear()-1],dateTime.getYear()+"");
        }
        return dateHigriMelady;
    }
    public DateHigriMelady getlsamicDate(Calendar today) {
        DateTime dateTime=getHigriDate(today);
        String[] iMonthNames = {"محرم", "صفر", "ربيع الاول",
                "ربيع الثاني", "جمادي الاول", "جمادي الثاني", "رجب",
                "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};
        DateHigriMelady dateHigriMelady =null;
        if(dateTime!=null){
            String d=dateTime.getDayOfMonth()+"";
            if(d.length() ==1){
                d="0"+d;
            }
            dateHigriMelady = new DateHigriMelady(d+"",iMonthNames[dateTime.getMonthOfYear()-1],dateTime.getYear()+"");
        }
        return dateHigriMelady;
    }


    public String getlsamicda(String year,String month,String day) {
        Chronology iso = ISOChronology.getInstanceUTC();
        Chronology hijri = IslamicChronology.getInstanceUTC();

        LocalDate todayIso = new LocalDate(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), iso);
        LocalDate todayHijri = new LocalDate(todayIso.toDateTimeAtStartOfDay(),
                hijri);
        return todayHijri.toString();


    }
    public DateHigriMelady getlsamicDate(String year,String month,String day) {
        DateTime dateTime=getHigriDate(year,month,day);
        String[] iMonthNames = {"محرم", "صفر", "ربيع الاول",
                "ربيع الثاني", "جمادي الاول", "جمادي الثاني", "رجب",
                "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};
        DateHigriMelady dateHigriMelady =null;
        if(dateTime!=null){
            String d=dateTime.getDayOfMonth()+"";
            if(d.length() ==1){
                d="0"+d;
            }
            dateHigriMelady = new DateHigriMelady(d+"",iMonthNames[dateTime.getMonthOfYear()-2],dateTime.getYear()+"");
        }
        return dateHigriMelady;
    }

    public DateHigriMelady getMeladyhDate( Calendar today ) {
        String[] iMonthNames = {"يناير", "فبراير", "مارس",
                "أبريل", "مايو", "يونيو", "يوليو",
                "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
        DateHigriMelady meladydate=null;
        if(today!=null){
            String d=today.get(Calendar.DAY_OF_MONTH)+"";
            if(d.length() ==1){
                d="0"+d;
            }
            meladydate= new DateHigriMelady(d+"",iMonthNames[today.get(Calendar.MONTH)],today.get(Calendar.YEAR)+"");
        }
        return meladydate;
    }

    public DateHigriMelady getMeladyhDate(String year,String month,String dayy) {
        String[] iMonthNames = {"يناير", "فبراير", "مارس",
                "أبريل", "مايو", "يونيو", "يوليو",
                "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
        DateHigriMelady meladydate=null;
        if(dayy.length() ==1){
            dayy="0"+dayy;
        }
        meladydate= new DateHigriMelady(dayy+"",iMonthNames[Integer.parseInt(month)-1],year+"");

        return meladydate;
    }

    public String toString(){
        return day+" "+month+" "+year;
    }

    public String getDay() {
        return day;
    }

    public String getDayName() {
//        getMonth()
//        getYear()
//        getDay()
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}