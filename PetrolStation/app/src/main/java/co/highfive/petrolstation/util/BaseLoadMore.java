package co.highfive.petrolstation.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import co.highfive.petrolstation.hazemhamadaqa.helper.SessionKeys;
import co.highfive.petrolstation.hazemhamadaqa.helper.SessionManager;

public class BaseLoadMore {
    private SessionManager manager;
    private SessionKeys sessionKeys;
    private GsonBuilder gsonBuilder;
    private Gson gson;

    public  BaseLoadMore(Context context){
        manager  =  new SessionManager(context);
        sessionKeys = new SessionKeys();
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
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

    public SessionKeys getSessionKeys() {
        return sessionKeys;
    }

    public void setSessionKeys(SessionKeys sessionKeys) {
        this.sessionKeys = sessionKeys;
    }
}
