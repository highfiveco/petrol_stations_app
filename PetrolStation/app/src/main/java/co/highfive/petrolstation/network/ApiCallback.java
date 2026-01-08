package co.highfive.petrolstation.network;


public interface ApiCallback<T> {
    void onSuccess(T data, String message, String rawJson);

    // status=false أو أي خطأ HTTP غير 401
    void onError(ApiError error);

    // 401
    void onUnauthorized(String rawJson);

    // no_internet / timeout / ...
    void onNetworkError(String reason);

    // JSON parsing
    void onParseError(String rawJson, Exception e);
}
