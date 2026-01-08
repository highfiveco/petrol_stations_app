package co.highfive.petrolstation.data.local.dao;

import androidx.room.*;
import java.util.List;
import co.highfive.petrolstation.data.local.entities.FuelCampaignItemEntity;

@Dao
public interface FuelCampaignItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<FuelCampaignItemEntity> list);

    @Query("SELECT * FROM fuel_campaign_items WHERE campaignId = :campaignId")
    List<FuelCampaignItemEntity> getByCampaign(int campaignId);

    @Query("DELETE FROM fuel_campaign_items")
    void clear();
}
