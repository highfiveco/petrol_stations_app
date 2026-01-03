package co.highfive.petrolstation.models;

public class Notification {
   private String id;
   private String type_notification;
   private String user_id;
   private String customer_id;
   private String customer_name;
   private String customer_mobile;
   private String customer_account_id;
   private String created_at;
   private String title;
   private String data_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType_notification() {
        return type_notification;
    }

    public void setType_notification(String type_notification) {
        this.type_notification = type_notification;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_mobile() {
        return customer_mobile;
    }

    public void setCustomer_mobile(String customer_mobile) {
        this.customer_mobile = customer_mobile;
    }

    public String getCustomer_account_id() {
        return customer_account_id;
    }

    public void setCustomer_account_id(String customer_account_id) {
        this.customer_account_id = customer_account_id;
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }
}
