package co.highfive.petrolstation.notifications.dto;

import java.util.List;

import co.highfive.petrolstation.models.Notification;
import co.highfive.petrolstation.models.Setting;

public class NotificationsResponseDto {
    private List<Notification> data;
    private Setting setting;
    private Pagination pagination;

    public List<Notification> getData() {
        return data;
    }

    public void setData(List<Notification> data) {
        this.data = data;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
