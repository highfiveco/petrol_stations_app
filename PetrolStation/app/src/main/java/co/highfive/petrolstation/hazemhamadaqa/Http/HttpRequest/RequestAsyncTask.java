package co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest;


import static co.highfive.petrolstation.hazemhamadaqa.app.Constant.isDevelopment;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.Http.InternetConnection;
import co.highfive.petrolstation.hazemhamadaqa.Http.MultipartUtility;
import co.highfive.petrolstation.hazemhamadaqa.Http.Util;


/**
 * Created by Eng. Hazem Hamadaqa on 11/13/2016.
 */

public class RequestAsyncTask  extends AsyncTask <Void, Void, Void>  {
    String a="no_error";
    ResponseObject responseObject=null;
    HttpHandler sh ;
    String url;
    AsyncResponse asyncResponse;
    String method;
    ArrayList<String> headerkeys;
    ArrayList<String> headervalues;
    InternetConnection internetDetector ;
    ArrayList<String> keys;
    ArrayList<String> values;
    Context c;
    boolean isJson=false;
    boolean isUploadFile=false;
    String jsonObject;
    String file_name;
    ArrayList<File> files;

    String file_name1;
    ArrayList<File> files1;
    ArrayList<String> file_names;
    int timeOut =0;
    boolean isCash = false;
    public RequestAsyncTask(boolean isCash,int timeOut  ,Context c,String url, String method,ArrayList<String> keys, ArrayList<String> values,ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.isCash=isCash;
        if (keys != null) {
            if(method.equals(Constant.REQUEST_GET)) {
                if (keys.size() > 0) {
                    url += "?" + keys.get(0) + "=" + URLEncoder.encode(values.get(0), "utf-8");
                    for (int i = 1; i < keys.size(); i++) {

                        Log.e("keys",""+keys.get(i));
                        Log.e("values",""+values.get(i));

                        url += "&" + keys.get(i) + "=" + URLEncoder.encode(values.get(i), "utf-8");
                    }
                }
            }
        }
        this.url=url;
        this.timeOut=timeOut;
        this.asyncResponse=asyncResponse;
        this.method=method;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.keys=keys;
        this.values=values;
        this.c=c;
        internetDetector = new InternetConnection();
    }

    public RequestAsyncTask(boolean isCash ,Context c, String url,String file_name ,ArrayList<File> files, ArrayList<String> keys, ArrayList<String> values, ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.isCash=isCash;
        this.url=url;
        this.asyncResponse=asyncResponse;
        this.method=Constant.REQUEST_POST;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.keys=keys;
        this.values=values;
        this.c=c;
        isUploadFile=true;
        this.file_name=file_name;
        this.files=files;

        internetDetector = new InternetConnection();
    }
    public RequestAsyncTask(boolean isCash ,Context c, String url,ArrayList<String> file_names,ArrayList<File> files, ArrayList<String> keys, ArrayList<String> values, ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.isCash=isCash;
        this.url=url;
        this.asyncResponse=asyncResponse;
        this.method=Constant.REQUEST_POST;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.keys=keys;
        this.values=values;
        this.c=c;
        isUploadFile=true;
        this.file_names=file_names;
        this.files=files;

        internetDetector = new InternetConnection();
    }

    public RequestAsyncTask(boolean isCash ,Context c,String url,String jsonObject,ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.isCash=isCash;
        this.url=url;
        this.asyncResponse=asyncResponse;
        this.method= Constant.REQUEST_POST;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.c=c;
        internetDetector = new InternetConnection();
        isJson=true;
        this.jsonObject=jsonObject;
    }


    public RequestAsyncTask(int timeOut ,Context c,String url, String method,ArrayList<String> keys, ArrayList<String> values,ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        if (keys != null) {
            if(method.equals(Constant.REQUEST_GET)) {
                if (keys.size() > 0) {
                    url += "?" + keys.get(0) + "=" + URLEncoder.encode(values.get(0), "utf-8");
                    for (int i = 1; i < keys.size(); i++) {
                        url += "&" + keys.get(i) + "=" + URLEncoder.encode(values.get(i), "utf-8");
                    }
                }
            }
        }
        this.url=url;
        this.timeOut=timeOut;
        this.asyncResponse=asyncResponse;
        this.method=method;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.keys=keys;
        this.values=values;
        this.c=c;
        internetDetector = new InternetConnection();
    }

    public RequestAsyncTask(Context c, String url,String file_name ,ArrayList<File> files,String file_name1 ,ArrayList<File> files1, ArrayList<String> keys, ArrayList<String> values, ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.url=url;
        this.asyncResponse=asyncResponse;
        this.method=Constant.REQUEST_POST;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.keys=keys;
        this.values=values;
        this.c=c;
        isUploadFile=true;
        this.file_name=file_name;
        this.files=files;
        this.files1=files1;
        this.file_name1=file_name1;
        internetDetector = new InternetConnection();
    }

    public RequestAsyncTask(Context c,String url,String jsonObject,ArrayList<String> headerkeys, ArrayList<String> headervalues, AsyncResponse asyncResponse) throws UnsupportedEncodingException {
        sh = new HttpHandler(c);
        this.url=url;
        this.asyncResponse=asyncResponse;
        this.method= Constant.REQUEST_POST;
        this.headervalues=headervalues;
        this.headerkeys=headerkeys;
        this.c=c;
        internetDetector = new InternetConnection();
        isJson=true;
        this.jsonObject=jsonObject;
    }


    @Override
    protected void onPreExecute() {
    }
    @Override
    protected Void doInBackground(Void... params) {
        if(internetDetector.isNetworkConnected(c) /*&& internetDetector.isConnected(c) */){
            if(method.equals(Constant.REQUEST_GET)) {
                responseObject=sh.makeServiceCall(isCash,0,url,method,headerkeys,headervalues);
            }else{
                if(isJson){
                    responseObject=sh.makeServiceCall(isCash,0,jsonObject,url,method,headerkeys,headervalues);
                }else if(isUploadFile){
                    try {
                        MultipartUtility multipart = new MultipartUtility(url,headerkeys,headervalues, "UTF-8");

                        if (keys != null && values != null) {
                            if (keys.size() == values.size()) {
                                for (int i = 0; i < keys.size(); i++) {

                                    Log.e("keys",""+keys.get(i));
                                    Log.e("values",""+values.get(i));

                                    multipart.addFormField(keys.get(i), values.get(i));
                                }
                            }
                        }
                        if (files != null && files.size()>0) {
                            for (int i = 0; i < files.size(); i++) {
                                if(file_name != null){
                                    multipart.addFilePart(file_name, files.get(i));
                                }else{
                                    multipart.addFilePart(file_names.get(i), files.get(i));
                                }

                            }
                        }

                        if (file_name1 != null && files1 != null && files1.size()>0) {
                            for (int i = 0; i < files1.size(); i++) {
                                multipart.addFilePart(file_name1, files1.get(i));
                            }
                        }

                        responseObject = multipart.finish();

                    } catch (IOException ex) {
                        System.err.println(ex);
                        Log.e("test",""+ex.getMessage());
                        if(responseObject == null){
                            responseObject = new ResponseObject();
                        }
                        try{
                            responseObject.setResponseCode(500);
                            responseObject.setResponseText(ex.getMessage());
                        }catch (Exception e){

                        }

                    }
                } else{
                    responseObject=sh.makeServiceCall(isCash,0,url,method,keys,values,headerkeys,headervalues);

                }
            }

        }else {
            if (isDevelopment)
            Log.e("no_internet","no_internet");
            a= "no_internet";
            if(method.equals(Constant.REQUEST_GET)) {
                if(isCash){
                    //get from cash
                    String fileName = Util.md5(url);
//                    Log.e("reqUrl:md5_after","a:"+ Util.md5(url));

                    if(fileName != null){
                        responseObject =getResponseObjectFromCash(fileName) ;
                        if(responseObject != null){
                            a= "no_error";
                        }
                    }

                }
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
            if (a.equals("no_error")){
                asyncResponse.processFinish(responseObject);
                if (isDevelopment)
                    Log.e("a" ,"Not Null");
            }else if (a.equals("no_internet")){
                asyncResponse.processerror("no_internet");
            } else{
                asyncResponse.processerror(a);
                if (isDevelopment)
                Log.e("a" ,"Null");
            }
    }

    public ResponseObject getResponseObjectFromCash(String fileName){

        ObjectInputStream cashfile=null;
        ResponseObject responseObject =null;
        try {

            FileInputStream fileInputStream = c.openFileInput(fileName+".txt");
            cashfile=new ObjectInputStream(fileInputStream);
            while (true ){
                try {
                    responseObject=(ResponseObject)cashfile.readObject();
                }
                catch (EOFException endException){
                    Log.e("EOFException",""+endException.getMessage());
                    break ;
                }
                catch (IOException ioException){
                    Log.e("ioException",""+ioException.getMessage());
                    ioException.printStackTrace();
                    break ;
                }
                catch (Exception e){
                    Log.e("Exception",""+e.getMessage());
                    break ;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("FileNotFoundException",""+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException",""+e.getMessage());
        }
        finally{
            if(cashfile!=null){
                try {
                    cashfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IOException:1",""+e.getMessage());
                }
            }

        }
        return responseObject;
    }
}
