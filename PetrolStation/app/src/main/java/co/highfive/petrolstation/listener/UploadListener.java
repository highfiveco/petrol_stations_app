package co.highfive.petrolstation.listener;

public interface UploadListener {
    void success(String url);
    void fail(String error_message);
}
