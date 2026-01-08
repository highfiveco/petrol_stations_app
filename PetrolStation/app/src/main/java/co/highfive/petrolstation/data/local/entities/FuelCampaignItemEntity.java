package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
        tableName = "fuel_campaign_items",
        indices = { @Index("campaignId"), @Index("itemId") }
)
public class FuelCampaignItemEntity {
    @PrimaryKey public int id;

    public Integer campaignId;
    public Integer itemId;

    // snapshot للـ item object داخل campaign
    public String itemJson;
}
