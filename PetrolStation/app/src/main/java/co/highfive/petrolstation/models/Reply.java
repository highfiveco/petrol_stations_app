package co.highfive.petrolstation.models;

import java.util.ArrayList;

public class Reply {
    private String id;
    private String maintenance_id;
    private String user_id;
    private String company_id;
    private String reply;
    private String created_at;
    private User user;
    private ArrayList<ReplyFile> files_reply;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaintenance_id() {
        return maintenance_id;
    }

    public void setMaintenance_id(String maintenance_id) {
        this.maintenance_id = maintenance_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<ReplyFile> getFiles_reply() {
        return files_reply;
    }

    public void setFiles_reply(ArrayList<ReplyFile> files_reply) {
        this.files_reply = files_reply;
    }
}
