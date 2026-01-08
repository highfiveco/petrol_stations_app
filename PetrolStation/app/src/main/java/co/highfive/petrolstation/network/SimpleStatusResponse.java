package co.highfive.petrolstation.network;

import com.google.gson.annotations.SerializedName;

public class SimpleStatusResponse {
    @SerializedName("status") public boolean status;
    @SerializedName("message") public String message;
}
