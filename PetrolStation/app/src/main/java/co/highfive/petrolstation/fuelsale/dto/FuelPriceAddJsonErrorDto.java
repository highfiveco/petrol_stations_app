package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FuelPriceAddJsonErrorDto {

    @SerializedName("index")
    public Integer index;

    @SerializedName("errors")
    public List<String> errors;
}
