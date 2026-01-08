package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers_settings.dto.LookupDto;
import co.highfive.petrolstation.data.local.entities.*;
import co.highfive.petrolstation.fuelsale.dto.*;

public class FuelPriceSettingsMappers {

    public static List<FuelPaymentTypeEntity> toPaymentTypes(List<LookupDto> dtos) {
        List<FuelPaymentTypeEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (LookupDto d : dtos) {
            if (d == null) continue;
            FuelPaymentTypeEntity e = new FuelPaymentTypeEntity();
            e.id = d.id;
            e.value2 = d.value2;
            e.name = d.name;
            e.enName = d.en_name;
            out.add(e);
        }
        return out;
    }

    public static List<FuelPumpEntity> toPumps(List<PumpDto> dtos) {
        List<FuelPumpEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (PumpDto p : dtos) {
            if (p == null) continue;
            FuelPumpEntity e = new FuelPumpEntity();
            e.id = p.id;
            e.name = p.name;
            e.icon = p.icon;
            out.add(e);
        }
        return out;
    }

    public static List<FuelSaleItemEntity> toFuelItems(List<FuelItemDto> dtos) {
        List<FuelSaleItemEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (FuelItemDto i : dtos) {
            if (i == null) continue;
            FuelSaleItemEntity e = new FuelSaleItemEntity();
            e.id = i.id;
            e.name = i.name;
            e.negativeCheck = i.negative_check;
            e.price = i.price;
            e.barcode = i.barcode;
            e.icon = i.icon;
            out.add(e);
        }
        return out;
    }

    public static List<FuelCampaignEntity> toCampaigns(List<FuelCampaignDto> dtos) {
        List<FuelCampaignEntity> out = new ArrayList<>();
        if (dtos == null) return out;
        for (FuelCampaignDto c : dtos) {
            if (c == null) continue;
            FuelCampaignEntity e = new FuelCampaignEntity();
            e.id = c.id;
            e.name = c.name;
            e.startDate = c.start_date;
            e.endDate = c.end_date;
            e.rewardType = c.reward_type;
            e.rewardValue = c.reward_value;
            e.pointsPerUnit = c.points_per_unit;
            e.notes = c.notes;
            out.add(e);
        }
        return out;
    }

    public static List<FuelCampaignItemEntity> toCampaignItems(List<FuelCampaignDto> campaigns, Gson gson) {
        List<FuelCampaignItemEntity> out = new ArrayList<>();
        if (campaigns == null) return out;

        for (FuelCampaignDto c : campaigns) {
            if (c == null || c.items == null) continue;

            for (FuelCampaignItemDto it : c.items) {
                if (it == null) continue;
                FuelCampaignItemEntity e = new FuelCampaignItemEntity();
                e.id = it.id;
                e.campaignId = it.campaign_id != null ? it.campaign_id : c.id;
                e.itemId = it.item_id;
                e.itemJson = it.item != null ? gson.toJson(it.item) : null;
                out.add(e);
            }
        }
        return out;
    }
}
