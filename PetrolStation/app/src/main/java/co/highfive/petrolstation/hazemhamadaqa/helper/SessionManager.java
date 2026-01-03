package co.highfive.petrolstation.hazemhamadaqa.helper;

/**
 * Created by Eng Hazem Hamadaqa on 21/12/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();
    SharedPreferences pref;

    Editor editor;
    Context _context;
    private SharedPreferences sp;
    private Editor spEditor;

    public SessionManager(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public boolean setBoolean(String key,boolean value) {
        spEditor = sp.edit();
        spEditor.putBoolean(key, value);
        spEditor.commit();
        return true;
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public boolean setFloat(String key,float value) {
        spEditor = sp.edit();
        spEditor.putFloat(key, value);
        spEditor.commit();
        return true;
    }

    public float getFloat(String key) {
        return sp.getFloat(key, -1);
    }

    public boolean setLong(String key,long value) {
        spEditor = sp.edit();
        spEditor.putLong(key, value);
        spEditor.commit();
        return true;
    }

    public long getLong(String key) {
        return sp.getLong(key, -1);
    }

    public boolean setInt(String key,int value) {
        spEditor = sp.edit();
        spEditor.putInt(key, value);
        spEditor.commit();
        return true;
    }

    public int getInt(String key) {
        return sp.getInt(key, -1);
    }

    public boolean setString(String key,String value) {
        spEditor = sp.edit();
        spEditor.putString(key, value);
        spEditor.commit();
        return true;
    }

    public String getString(String key) {
        try{
            return sp.getString(key, null);
        }catch (Exception e){
            return null;
        }

    }

}
