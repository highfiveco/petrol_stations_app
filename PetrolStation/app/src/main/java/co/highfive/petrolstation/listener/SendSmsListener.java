package co.highfive.petrolstation.listener;

public interface SendSmsListener {
    void onSend(String message);
    void onPrefillRequested(); // للـ current_reading
}