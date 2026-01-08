package co.highfive.petrolstation.network;


public class ApiError {
    public final int httpCode;
    public final String message;
    public final String raw;

    public ApiError(int httpCode, String message, String raw) {
        this.httpCode = httpCode;
        this.message = message;
        this.raw = raw;
    }
}
