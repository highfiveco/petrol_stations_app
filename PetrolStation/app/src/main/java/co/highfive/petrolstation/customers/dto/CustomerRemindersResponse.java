package co.highfive.petrolstation.customers.dto;

import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Setting;
import co.highfive.petrolstation.models.Reminder; // عدّل الباكدج حسب مشروعك
import co.highfive.petrolstation.network.PaginatedResponse;

public class CustomerRemindersResponse {
    public Account customer;                 // json: data.customer
    public PaginatedResponse<Reminder> reminders; // json: data.reminders
    public Setting setting;                  // json: data.setting
}
