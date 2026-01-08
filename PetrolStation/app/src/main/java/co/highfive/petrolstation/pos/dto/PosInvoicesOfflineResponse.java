package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;

public class PosInvoicesOfflineResponse {

    @SerializedName("status")
    public boolean status;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public PosInvoicesOfflineDataDto data;
}
