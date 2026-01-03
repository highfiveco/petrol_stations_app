package co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse;


import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;

/**
 * Created by Eng. Hazem Hamadaqa on 11/13/2016.
 */

public interface AsyncResponse {
    void processFinish(ResponseObject responseObject);
    void processerror(String output);
}
