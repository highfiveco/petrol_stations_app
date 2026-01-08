package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PosAddJsonErrorDto {

    @SerializedName("index")
    public int index;

    @SerializedName("errors")
    public List<String> errors;
}
