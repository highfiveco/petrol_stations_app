package co.highfive.petrolstation.models;

public class ReplyFile {
    private String id;
    private String reply_id;
    private String maintenance_id;
    private String user_id;
    private String company_id;
    private String file;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
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

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
