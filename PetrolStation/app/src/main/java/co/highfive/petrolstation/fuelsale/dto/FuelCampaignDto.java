package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FuelCampaignDto {

    @SerializedName("id") public int id;
    @SerializedName("name") public String name;

    @SerializedName("start_date") public String start_date;
    @SerializedName("end_date") public String end_date;

    @SerializedName("reward_type") public Integer reward_type;
    @SerializedName("reward_value") public Double reward_value;

    @SerializedName("points_per_unit") public String points_per_unit;
    @SerializedName("notes") public String notes;

    @SerializedName("items") public List<FuelCampaignItemDto> items;
}
