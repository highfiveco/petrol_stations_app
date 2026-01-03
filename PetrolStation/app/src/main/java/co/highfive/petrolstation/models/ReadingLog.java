package co.highfive.petrolstation.models;

public class ReadingLog {
    private String name;
    private String curr_old_value;
    private String curr_new_value;
    private String old_price;
    private String new_price;
    private String last_old_value;
    private String last_new_value;
    private String create_date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurr_old_value() {
        return curr_old_value;
    }

    public void setCurr_old_value(String curr_old_value) {
        this.curr_old_value = curr_old_value;
    }

    public String getCurr_new_value() {
        return curr_new_value;
    }

    public void setCurr_new_value(String curr_new_value) {
        this.curr_new_value = curr_new_value;
    }

    public String getOld_price() {
        return old_price;
    }

    public void setOld_price(String old_price) {
        this.old_price = old_price;
    }

    public String getNew_price() {
        return new_price;
    }

    public void setNew_price(String new_price) {
        this.new_price = new_price;
    }

    public String getLast_old_value() {
        return last_old_value;
    }

    public void setLast_old_value(String last_old_value) {
        this.last_old_value = last_old_value;
    }

    public String getLast_new_value() {
        return last_new_value;
    }

    public void setLast_new_value(String last_new_value) {
        this.last_new_value = last_new_value;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }
}
