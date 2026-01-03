package co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest;

import static co.highfive.petrolstation.hazemhamadaqa.app.Constant.isDevelopment;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.Http.Util;


/**
 * Created by Eng. Hazem Hamadaqa on 11/13/2016.
 */

public class HttpHandler{
    private static final String TAG = HttpHandler.class.getSimpleName();
    int responseCode=0;
    ResponseObject responseObject;
    Context context;
    public HttpHandler(Context context) {
        responseObject=new ResponseObject();
        this.context=context;
        freeMemory();
    }
    // for get
    public ResponseObject makeServiceCall(boolean isCash ,int timeOut, String reqUrl, String method, ArrayList<String> headerkeys, ArrayList<String> headervalues) {
        String response = null;
        HttpURLConnection conn = null;
        try {
            Log.e("reqUrl",""+reqUrl);
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if(timeOut !=0){
                conn.setConnectTimeout(timeOut);
            }


            if (headerkeys != null && headervalues != null) {
                if (headerkeys.size() == headervalues.size()) {
                    for (int i = 0; i < headerkeys.size(); i++) {
                        Log.e("headerkeys",""+headerkeys.get(i));
                        Log.e("headervalues",""+headervalues.get(i));
                        conn.setRequestProperty(headerkeys.get(i), headervalues.get(i));
                    }
                }
            }
            // read the response
            responseCode = conn.getResponseCode();
            // read the response
            Log.e("responseCode",""+responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = new BufferedInputStream(conn.getInputStream());
                //  response = convertStreamToString(in);

               // responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
                responseObject.setResponseText(convertStreamToString(in));
                //response= new ToJson<>(responseObject).toJson();
               // return responseObject;
            }else{
                InputStream in=conn.getErrorStream();
                //response = convertStreamToString(in);

              //  responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseText(convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
            //    return responseObject;
               // response= new ToJson<>(responseObject).toJson();
            }


        } catch (MalformedURLException e) {
           // responseObject= new ResponseObject(responseCode,"MalformedURLException: " + e.getMessage());
            responseObject.setResponseText("MalformedURLException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
          //  return responseObject;
        } catch (ProtocolException e) {
           // responseObject= new ResponseObject(responseCode,"ProtocolException: " + e.getMessage());
            responseObject.setResponseText("ProtocolException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
          //  return responseObject;
        } catch (IOException e) {
         //   responseObject= new ResponseObject(responseCode,"IOException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            responseObject.setResponseText("IOException: " + e.getMessage());
         //   return responseObject;
        } catch (Exception e) {
          //  responseObject= new ResponseObject(responseCode,"Exception: " + e.getMessage());
            responseObject.setResponseText("Exception: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
         //   return responseObject;
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if(isDevelopment)
            Log.e("response",""+responseObject.getResponseText());

        if(method.equals(Constant.REQUEST_GET)) {
            if(isCash){
                // make cash
                String fileName = Util.md5(reqUrl);
//                Log.e("reqUrl:md5","a:"+Util.md5(reqUrl));
                cashResponseObject(fileName,responseObject);
            }
        }
        return responseObject;
    }

    // for post
    public ResponseObject makeServiceCall(boolean isCash ,int timeOut,String reqUrl, String method,ArrayList<String> keys, ArrayList<String> values, ArrayList<String> headerkeys, ArrayList<String> headervalues) {
        String response = null;
        HttpURLConnection conn = null;
        try {
            Log.e("reqUrl", "" + reqUrl);
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if(timeOut !=0){
                conn.setConnectTimeout(timeOut);
            }

            if (headerkeys != null && headervalues != null) {
                if (headerkeys.size() == headervalues.size()) {
                    for (int i = 0; i < headerkeys.size(); i++) {

                        Log.e("headerkeys",""+headerkeys.get(i));
                        Log.e("headervalues",""+headervalues.get(i));

                        conn.setRequestProperty(headerkeys.get(i), headervalues.get(i));
                    }
                }
            }

            List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
            if (keys != null && values != null) {
                if (keys.size() == values.size()) {
                    for (int i = 0; i < keys.size(); i++) {

                        Log.e("keys",""+keys.get(i));
                        Log.e("values",""+values.get(i));

                        params.add(new Pair<>(keys.get(i), values.get(i)));
                    }
                }
            }
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            responseCode = conn.getResponseCode();
            // read the response
            Log.e("responseCode", "" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                //  response = convertStreamToString(in);

                // responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
                responseObject.setResponseText(convertStreamToString(in));
                //response= new ToJson<>(responseObject).toJson();
                // return responseObject;
            } else {
                InputStream in = conn.getErrorStream();
                //response = convertStreamToString(in);

                //  responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseText(convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
                //    return responseObject;
                // response= new ToJson<>(responseObject).toJson();
            }


        } catch (MalformedURLException e) {
            // responseObject= new ResponseObject(responseCode,"MalformedURLException: " + e.getMessage());
            responseObject.setResponseText("MalformedURLException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //  return responseObject;
        } catch (ProtocolException e) {
            // responseObject= new ResponseObject(responseCode,"ProtocolException: " + e.getMessage());
            responseObject.setResponseText("ProtocolException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //  return responseObject;
        } catch (IOException e) {
            //   responseObject= new ResponseObject(responseCode,"IOException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            responseObject.setResponseText("IOException: " + e.getMessage());
            //   return responseObject;
        } catch (Exception e) {
            //  responseObject= new ResponseObject(responseCode,"Exception: " + e.getMessage());
            responseObject.setResponseText("Exception: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //   return responseObject;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if (isDevelopment)
            Log.e("response", "" + responseObject.getResponseText());

        if(method.equals(Constant.REQUEST_GET)) {
            if(isCash){
                // make cash
                String fileName = Util.md5(reqUrl);
//                Log.e("reqUrl:md5","a:"+Util.md5(reqUrl));
                cashResponseObject(fileName,responseObject);
            }
        }
        return responseObject;
    }

     // for post json object
    public ResponseObject makeServiceCall(boolean isCash ,int timeOut,String jsonObject,String reqUrl, String method, ArrayList<String> headerkeys, ArrayList<String> headervalues) {
        String response = null;
        HttpURLConnection conn = null;
        try {
            Log.e("reqUrl", "" + reqUrl);
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if(timeOut !=0){
                conn.setConnectTimeout(timeOut);
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("accept-charset", "utf-8");

            if (headerkeys != null && headervalues != null) {
                if (headerkeys.size() == headervalues.size()) {
                    for (int i = 0; i < headerkeys.size(); i++) {
                        Log.e("headerkeys",""+headerkeys.get(i));
                        Log.e("headervalues",""+headervalues.get(i));
                        conn.setRequestProperty(headerkeys.get(i), headervalues.get(i));
                    }
                }
            }


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject);
            writer.flush();
            writer.close();
            os.close();


            responseCode = conn.getResponseCode();
            // read the response
            Log.e("responseCode", "" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                //  response = convertStreamToString(in);

                // responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
                responseObject.setResponseText(convertStreamToString(in));
                //response= new ToJson<>(responseObject).toJson();
                // return responseObject;
            } else {
                InputStream in = conn.getErrorStream();
                //response = convertStreamToString(in);

                //  responseObject= new ResponseObject(responseCode,convertStreamToString(in));
                responseObject.setResponseText(convertStreamToString(in));
                responseObject.setResponseCode(responseCode);
                //    return responseObject;
                // response= new ToJson<>(responseObject).toJson();
            }


        } catch (MalformedURLException e) {
            // responseObject= new ResponseObject(responseCode,"MalformedURLException: " + e.getMessage());
            responseObject.setResponseText("MalformedURLException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //  return responseObject;
        } catch (ProtocolException e) {
            // responseObject= new ResponseObject(responseCode,"ProtocolException: " + e.getMessage());
            responseObject.setResponseText("ProtocolException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //  return responseObject;
        } catch (IOException e) {
            //   responseObject= new ResponseObject(responseCode,"IOException: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            responseObject.setResponseText("IOException: " + e.getMessage());
            //   return responseObject;
        } catch (Exception e) {
            //  responseObject= new ResponseObject(responseCode,"Exception: " + e.getMessage());
            responseObject.setResponseText("Exception: " + e.getMessage());
            responseObject.setResponseCode(responseCode);
            //   return responseObject;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if (isDevelopment)
            Log.e("response", "" + responseObject.getResponseText());

        if(method.equals(Constant.REQUEST_GET)) {
            if(isCash){
                // make cash
                String fileName = Util.md5(reqUrl);
//                Log.e("reqUrl:md5","a:"+Util.md5(reqUrl));
                cashResponseObject(fileName,responseObject);
            }
        }
        return responseObject;
    }


    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }
        return result.toString();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void cashResponseObject(String fileName,ResponseObject responseObject){

        ObjectOutputStream cashfile=null;
        try {
            FileOutputStream file=context.openFileOutput(fileName+".txt", Context.MODE_PRIVATE);
            cashfile = new ObjectOutputStream (file);
            cashfile.writeObject(responseObject);
            Log.e("cashResponseObject","done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("FileNotFoundException",""+e.getMessage());
        }catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException",""+e.getMessage());
        }finally{
            if(cashfile!=null){
                try {
                    cashfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IOException1",""+e.getMessage());
                }
            }

        }
    }

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
}
