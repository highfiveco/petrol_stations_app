package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class Maintenance {
    private String id;
    private String title;
    private String details;
    private String status;
    private String type;
    private String user_id;
    private String user_name;
    private String type_maintenances;
    private String status_maintenances;
    private String created_at;
    private String customer_name;
    private String customer_id;
    private ArrayList<Constant> users;
    private ArrayList<MaintenanceFile> files;
    private ArrayList<Reply> reply;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getType_maintenances() {
        return type_maintenances;
    }

    public void setType_maintenances(String type_maintenances) {
        this.type_maintenances = type_maintenances;
    }

    public String getStatus_maintenances() {
        return status_maintenances;
    }

    public void setStatus_maintenances(String status_maintenances) {
        this.status_maintenances = status_maintenances;
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

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public ArrayList<MaintenanceFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<MaintenanceFile> files) {
        this.files = files;
    }

    public ArrayList<Reply> getReply() {
        return reply;
    }

    public void setReply(ArrayList<Reply> reply) {
        this.reply = reply;
    }

    public ArrayList<Constant> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Constant> users) {
        this.users = users;
    }
}
