package co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model;

import java.io.Serializable;

/**
 * Created by Eng. Hazem Hamadaqa on 11/13/2017.
 */

public class ResponseObject implements Serializable{
    private int responseCode;
    private String responseText;
    public ResponseObject() {
    }

    public ResponseObject(int responseCode,String responseText) {
        this.responseCode = responseCode;
        this.responseText = responseText;
    }


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}