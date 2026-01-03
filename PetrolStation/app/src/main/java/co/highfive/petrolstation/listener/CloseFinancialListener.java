package co.highfive.petrolstation.listener;

import co.highfive.petrolstation.models.Fund;

public interface CloseFinancialListener {
    void close(Fund fund, String recevied_money, String box_status_id, String notes);
}
