package co.highfive.petrolstation.financial.dto;

import com.google.gson.annotations.SerializedName;

import co.highfive.petrolstation.models.Reading;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.Transactions;

public class FinancialAddMoveResponseDto {

    @SerializedName("setting")
    public Setting setting;

    @SerializedName("move")
    public Transactions move;

    @SerializedName("reading")
    public Reading reading;
}
