package co.highfive.petrolstation.models;

public class CustomerPermssion {
    private int readings;
    private int sms;
    private int view_log;
    private int view_financial_move;
    private int update_customers;
    private int add_customers;
    private int view_reminders;
    private int add_reminders;
    private int delete_reminders;
    private int add_maintenances;
    private int update_maintenances;
    private int update_mobile;

    public int getReadings() {
        return readings;
    }

    public void setReadings(int readings) {
        this.readings = readings;
    }

    public int getSms() {
        return sms;
    }

    public void setSms(int sms) {
        this.sms = sms;
    }

    public int getView_log() {
        return view_log;
    }

    public void setView_log(int view_log) {
        this.view_log = view_log;
    }

    public int getView_financial_move() {
        return view_financial_move;
    }

    public void setView_financial_move(int view_financial_move) {
        this.view_financial_move = view_financial_move;
    }

    public int getUpdate_customers() {
        return update_customers;
    }

    public void setUpdate_customers(int update_customers) {
        this.update_customers = update_customers;
    }

    public int getAdd_customers() {
        return add_customers;
    }

    public void setAdd_customers(int add_customers) {
        this.add_customers = add_customers;
    }

    public int getView_reminders() {
        return view_reminders;
    }

    public void setView_reminders(int view_reminders) {
        this.view_reminders = view_reminders;
    }

    public int getAdd_reminders() {
        return add_reminders;
    }

    public void setAdd_reminders(int add_reminders) {
        this.add_reminders = add_reminders;
    }

    public int getDelete_reminders() {
        return delete_reminders;
    }

    public void setDelete_reminders(int delete_reminders) {
        this.delete_reminders = delete_reminders;
    }

    public int getAdd_maintenances() {
        return add_maintenances;
    }

    public void setAdd_maintenances(int add_maintenances) {
        this.add_maintenances = add_maintenances;
    }

    public int getUpdate_maintenances() {
        return update_maintenances;
    }

    public void setUpdate_maintenances(int update_maintenances) {
        this.update_maintenances = update_maintenances;
    }

    public int getUpdate_mobile() {
        return update_mobile;
    }

    public void setUpdate_mobile(int update_mobile) {
        this.update_mobile = update_mobile;
    }
}
