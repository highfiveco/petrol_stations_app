package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "campaigns")
public class CampaignEntity {
    @PrimaryKey public int id;
    public String name;
}
