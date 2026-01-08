package co.highfive.petrolstation.pos.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.CustomersSettingDto;

public class PosItemsData {


    @SerializedName("items")
    public List<PosItemDto> items;
}
