package co.highfive.petrolstation.settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import co.highfive.petrolstation.data.local.dao.CompanySettingCacheDao;
import co.highfive.petrolstation.data.local.entities.CompanySettingCacheEntity;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

public class CompanySettingRepository {

    private final CompanySettingCacheDao dao;
    private final Gson gson;

    public CompanySettingRepository(CompanySettingCacheDao dao, Gson gson) {
        this.dao = dao;
        this.gson = gson;
    }

    /** خزّن JSON كما وصل من السيرفر (BaseResponse<CompanySettingData>) */
    public void saveRawResponseJson(String rawJson) {
        CompanySettingCacheEntity entity = new CompanySettingCacheEntity();
        entity.id = 1;
        entity.json = rawJson;
        entity.updatedAt = System.currentTimeMillis();
        dao.upsert(entity);
    }

    /** يرجع JSON الخام من Room */
    public String getCachedRaw() {
        CompanySettingCacheEntity entity = dao.get();
        return entity != null ? entity.json : null;
    }

    /** يرجع CompanySettingData بعد parsing من JSON المخزن */
    public CompanySettingData getCachedData() {
        CompanySettingCacheEntity entity = dao.get();
        if (entity == null || entity.json == null || entity.json.trim().isEmpty()) return null;

        Type type = new TypeToken<BaseResponse<CompanySettingData>>() {}.getType();
        BaseResponse<CompanySettingData> base = gson.fromJson(entity.json, type);
        return base != null ? base.data : null;
    }

    public long getCachedUpdatedAt() {
        CompanySettingCacheEntity entity = dao.get();
        return entity != null ? entity.updatedAt : 0L;
    }

    public void clearCache() {
        dao.clear();
    }
}
