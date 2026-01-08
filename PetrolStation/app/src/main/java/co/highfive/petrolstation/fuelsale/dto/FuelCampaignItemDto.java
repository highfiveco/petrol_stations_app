package co.highfive.petrolstation.fuelsale.dto;

import com.google.gson.annotations.SerializedName;

public class FuelCampaignItemDto {

    @SerializedName("id") public int id;
    @SerializedName("campaign_id") public Integer campaign_id;
    @SerializedName("item_id") public Integer item_id;

    // item object كامل
    @SerializedName("item") public Object item;
}
