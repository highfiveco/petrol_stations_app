package co.highfive.petrolstation.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;

public class ApiClient {

    // ===== Interfaces =====
    public interface HeaderProvider {
        String getToken();     // "Bearer ...."
        String getLang();      // "ar" / "en"
        boolean isLoggedIn();
    }

    public interface UnauthorizedHandler {
        void onUnauthorizedGlobal(); // logout() مثلاً
    }

    // ===== Fields =====
    private final Context context;
    private final Gson gson;
    private final HeaderProvider headerProvider;
    private final UnauthorizedHandler unauthorizedHandler;

    // ===== Constructor =====
    public ApiClient(Context context, Gson gson, HeaderProvider headerProvider, UnauthorizedHandler unauthorizedHandler) {
        this.context = context.getApplicationContext();
        this.gson = gson;
        this.headerProvider = headerProvider;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    // ============================================================
    // Public API - Request using ApiParams (supports repeated keys)
    // ============================================================
    public <T> void request(
            String method,
            String endpointPath,
            ApiParams params,
            Map<String, String> extraHeaders,
            Type baseResponseType,  // new TypeToken<BaseResponse<T>>(){}.getType()
            int retry,
            ApiCallback<T> callback
    ) {

        ArrayList<String> keys = params != null ? params.keys : new ArrayList<>();
        ArrayList<String> values = params != null ? params.values : new ArrayList<>();

        requestInternal(method, endpointPath, keys, values, extraHeaders, baseResponseType, retry, callback);
    }

    // ============================================================
    // Public API - Request using Map (simple requests only)
    // WARNING: Map does NOT support repeated keys
    // ============================================================
    public <T> void request(
            String method,
            String endpointPath,
            Map<String, String> queryOrBodyParams,
            Map<String, String> extraHeaders,
            Type baseResponseType,
            int retry,
            ApiCallback<T> callback
    ) {
        ApiParams params = new ApiParams();
        if (queryOrBodyParams != null) {
            for (Map.Entry<String, String> e : queryOrBodyParams.entrySet()) {
                params.add(e.getKey(), e.getValue());
            }
        }
        request(method, endpointPath, params, extraHeaders, baseResponseType, retry, callback);
    }

    // ===== Core Internal Implementation =====
    private <T> void requestInternal(
            String method,
            String endpointPath,
            ArrayList<String> keys,
            ArrayList<String> values,
            Map<String, String> extraHeaders,
            Type baseResponseType,
            int retry,
            ApiCallback<T> callback
    ) {

        // headers -> arrays
        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();

        // lang header (if present)
        String lang = headerProvider != null ? headerProvider.getLang() : null;
        if (lang != null && !lang.trim().isEmpty()) {
            headerKeys.add("lang");
            headerValues.add(lang);
        }

        // Authorization header (only when logged in)
        if (headerProvider != null && headerProvider.isLoggedIn()) {
            String token = headerProvider.getToken();
            if (token != null && !token.trim().isEmpty()) {
                headerKeys.add("Authorization");
                headerValues.add(token);
            }
        }

        // Extra headers (if any)
        if (extraHeaders != null) {
            for (Map.Entry<String, String> e : extraHeaders.entrySet()) {
                headerKeys.add(e.getKey());
                headerValues.add(e.getValue());
            }
        }

        String url = ApiConfig.BASE_URL + endpointPath;

        try {
            new RequestAsyncTask(
                    0,
                    context,
                    url,
                    method,
                    keys,
                    values,
                    headerKeys,
                    headerValues,
                    new AsyncResponse() {
                        @Override
                        public void processFinish(ResponseObject responseObject) {
                            int code = responseObject.getResponseCode();
                            String raw = responseObject.getResponseText();

                            // 401 Unauthorized
                            if (code == 401) {
                                if (unauthorizedHandler != null) unauthorizedHandler.onUnauthorizedGlobal();
                                if (callback != null) callback.onUnauthorized(raw);
                                return;
                            }

                            // HTTP not OK or empty response -> retry
                            if (code != 200 || raw == null || raw.trim().isEmpty()) {
                                if (retry < ApiConfig.MAX_RETRIES) {
                                    postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                                            requestInternal(method, endpointPath, keys, values, extraHeaders, baseResponseType, retry + 1, callback)
                                    );
                                } else {
                                    if (callback != null) callback.onError(new ApiError(code, "HTTP Error", raw));
                                }
                                return;
                            }

                            // Parse BaseResponse<T>
                            try {
                                @SuppressWarnings("unchecked")
                                BaseResponse<T> base = gson.fromJson(raw, baseResponseType);

                                if (base == null) {
                                    if (callback != null) callback.onParseError(raw, new Exception("Parsed response is null"));
                                    return;
                                }

                                if (base.status) {
                                    if (callback != null) callback.onSuccess(base.data, base.message, raw);
                                } else {
                                    // status=false: business error (مثل: بيانات خاطئة)
                                    String msg = (base.message != null && !base.message.trim().isEmpty())
                                            ? base.message
                                            : "Unknown error";
                                    if (callback != null) callback.onError(new ApiError(200, msg, raw));
                                }

                            } catch (JsonSyntaxException jse) {
                                if (callback != null) callback.onParseError(raw, jse);
                            } catch (Exception e) {
                                if (callback != null) callback.onParseError(raw, e);
                            }
                        }

                        @Override
                        public void processerror(String output) {
                            // "no_internet" or timeout or custom error text
                            if (retry < ApiConfig.MAX_RETRIES) {
                                postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                                        requestInternal(method, endpointPath, keys, values, extraHeaders, baseResponseType, retry + 1, callback)
                                );
                            } else {
                                if (callback != null) callback.onNetworkError(output);
                            }
                        }
                    }
            ).execute();

        } catch (Exception e) {
            if (retry < ApiConfig.MAX_RETRIES) {
                postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                        requestInternal(method, endpointPath, keys, values, extraHeaders, baseResponseType, retry + 1, callback)
                );
            } else {
                if (callback != null) callback.onNetworkError(String.valueOf(e.getMessage()));
            }
        }
    }

    // ===== Utilities =====
    private void postDelay(long ms, Runnable r) {
        new Handler(Looper.getMainLooper()).postDelayed(r, ms);
    }

    /**
     * Simple helper for building Map params for simple requests.
     * WARNING: does not support repeated keys.
     */
    public static Map<String, String> mapOf(Object... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length - 1; i += 2) {
            m.put(String.valueOf(kv[i]), String.valueOf(kv[i + 1]));
        }
        return m;
    }

    // ===== Params helper (supports repeated keys) =====
    public static class ApiParams {
        public final ArrayList<String> keys = new ArrayList<>();
        public final ArrayList<String> values = new ArrayList<>();

        public ApiParams add(String key, Object value) {
            keys.add(String.valueOf(key));
            values.add(value == null ? "" : String.valueOf(value));
            return this;
        }

        public int size() {
            return keys.size();
        }
    }

    public <T> void requestRawLists(
            String method,
            String endpointPath,
            ArrayList<String> keys,
            ArrayList<String> values,
            Map<String, String> extraHeaders,
            Type responseType,
            int retry,
            ApiCallback<T> callback
    ) {

        // local mutable (not used in lambdas)
        ArrayList<String> k = (keys != null) ? keys : new ArrayList<>();
        ArrayList<String> v = (values != null) ? values : new ArrayList<>();

        // make them final for lambdas
        final ArrayList<String> finalKeys = k;
        final ArrayList<String> finalValues = v;

        // headers -> arrays
        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();

        // lang دائماً (إذا موجود)
        String lang = headerProvider != null ? headerProvider.getLang() : null;
        if (lang != null && !lang.trim().isEmpty()) {
            headerKeys.add("lang");
            headerValues.add(lang);
        }

        // Authorization تلقائياً إذا logged in
        if (headerProvider != null && headerProvider.isLoggedIn()) {
            String token = headerProvider.getToken();
            if (token != null && !token.trim().isEmpty()) {
                headerKeys.add("Authorization");
                headerValues.add(token);
            }
        }

        // Extra headers
        if (extraHeaders != null) {
            for (Map.Entry<String, String> e : extraHeaders.entrySet()) {
                headerKeys.add(e.getKey());
                headerValues.add(e.getValue());
            }
        }

        String url = ApiConfig.BASE_URL + endpointPath;

        try {
            new RequestAsyncTask(
                    0,
                    context,
                    url,
                    method,
                    finalKeys,
                    finalValues,
                    headerKeys,
                    headerValues,
                    new AsyncResponse() {
                        @Override
                        public void processFinish(ResponseObject responseObject) {

                            int code = responseObject.getResponseCode();
                            String raw = responseObject.getResponseText();

                            // Unauthorized
                            if (code == 401) {
                                if (unauthorizedHandler != null) unauthorizedHandler.onUnauthorizedGlobal();
                                if (callback != null) callback.onUnauthorized(raw);
                                return;
                            }

                            // HTTP error أو empty body
                            if (code != 200 || raw == null || raw.trim().isEmpty()) {
                                if (retry < ApiConfig.MAX_RETRIES) {
                                    postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                                            requestRawLists(method, endpointPath, finalKeys, finalValues, extraHeaders, responseType, retry + 1, callback)
                                    );
                                } else {
                                    if (callback != null) callback.onError(new ApiError(code, "HTTP Error", raw));
                                }
                                return;
                            }

                            // Parse
                            try {
                                @SuppressWarnings("unchecked")
                                T parsed = (T) gson.fromJson(raw, responseType);

                                if (parsed == null) {
                                    if (callback != null) callback.onParseError(raw, new Exception("Null parsed response"));
                                    return;
                                }

                                // لا نفترض BaseResponse هنا
                                if (callback != null) callback.onSuccess(parsed, null, raw);

                            } catch (JsonSyntaxException jse) {
                                if (callback != null) callback.onParseError(raw, jse);
                            } catch (Exception e) {
                                if (callback != null) callback.onParseError(raw, e);
                            }
                        }

                        @Override
                        public void processerror(String output) {
                            if (retry < ApiConfig.MAX_RETRIES) {
                                postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                                        requestRawLists(method, endpointPath, finalKeys, finalValues, extraHeaders, responseType, retry + 1, callback)
                                );
                            } else {
                                if (callback != null) callback.onNetworkError(output);
                            }
                        }
                    }
            ).execute();

        } catch (Exception e) {
            if (retry < ApiConfig.MAX_RETRIES) {
                postDelay(ApiConfig.RETRY_DELAY_MS, () ->
                        requestRawLists(method, endpointPath, finalKeys, finalValues, extraHeaders, responseType, retry + 1, callback)
                );
            } else {
                if (callback != null) callback.onNetworkError(e.getMessage());
            }
        }
    }

}
