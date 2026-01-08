package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_campaigns")
public class FuelCampaignEntity {
    @PrimaryKey public int id;

    public String name;
    public String startDate;
    public String endDate;

    public Integer rewardType;
    public Double rewardValue;

    public String pointsPerUnit;
    public String notes;
}
