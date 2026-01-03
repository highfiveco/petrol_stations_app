package co.highfive.petrolstation.hazemhamadaqa.helper;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.highfive.petrolstation.hazemhamadaqa.app.Constant;


/**
 * Created by Eng. Hazem Hamadaqa on 6/3/2017.
 */

public class ToJson <T> implements Constant {

    T object;

    GsonBuilder gsonBuilder = new GsonBuilder();

    private Gson gson ;

    public ToJson(T object){
        this.object=object;
        gson = gsonBuilder.create();
    }

    public String toJson() throws Exception {
        String retVal="";
        try{
            retVal=gson.toJson(object);
            if(isDevelopment){
                int maxLogSize = 1000;
                for(int i = 0; i <= retVal.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > retVal.length() ? retVal.length() : end;
                    Log.e("retval", retVal.substring(start, end));
                }
//                Log.e("retval",""+retVal);
            }

        }catch (Exception e){
            throw  new Exception(""+e.getMessage());
        }
        return retVal;
    }
}