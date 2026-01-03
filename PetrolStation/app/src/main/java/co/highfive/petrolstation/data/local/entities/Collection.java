package co.highfive.petrolstation.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "collections")
public class Collection {
    @PrimaryKey
    @NonNull
    private int id;
    private int account_id;
    private int company_id;
    private double balance;
    private double accumulated_account;
    private String name;
    private String mobile;
    private Double diff;
    private Double total_price;
    private Double curnt_value;
    private Double last_value;
    private double cumulative;
    private double remain_monthly;
    private String month;
    private String pdf_url;
    private String excel_url;
    private String tablon_no;
    private String customer_order;
    private String status;

    public Collection(){

    }
    // Constructor
    public Collection(int id, int account_id, int companyId, double balance, double accumulated_account,
                      String name, String mobile, Double diff, Double total_price,
                      Double curnt_value, Double last_value, double cumulative,
                      double remain_monthly, String month, String pdf_url, String excel_url,
                      String tablon_no, String customer_order, String status) {
        this.id = id;
        this.account_id = account_id;
        company_id = companyId;
        this.balance = balance;
        this.accumulated_account = accumulated_account;
        this.name = name;
        this.mobile = mobile;
        this.diff = diff;
        this.total_price = total_price;
        this.curnt_value = curnt_value;
        this.last_value = last_value;
        this.cumulative = cumulative;
        this.remain_monthly = remain_monthly;
        this.month = month;
        this.pdf_url = pdf_url;
        this.excel_url = excel_url;
        this.tablon_no = tablon_no;
        this.customer_order = customer_order;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccount_id() { return account_id; }
    public void setAccount_id(int account_id) { this.account_id = account_id; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getAccumulated_account() { return accumulated_account; }
    public void setAccumulated_account(double accumulated_account) { this.accumulated_account = accumulated_account; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Double getDiff() { return diff; }
    public void setDiff(Double diff) { this.diff = diff; }

    public Double getTotal_price() { return total_price; }
    public void setTotal_price(Double total_price) { this.total_price = total_price; }

    public Double getCurnt_value() { return curnt_value; }
    public void setCurnt_value(Double curnt_value) { this.curnt_value = curnt_value; }

    public Double getLast_value() { return last_value; }
    public void setLast_value(Double last_value) { this.last_value = last_value; }

    public double getCumulative() { return cumulative; }
    public void setCumulative(double cumulative) { this.cumulative = cumulative; }

    public double getRemain_monthly() { return remain_monthly; }
    public void setRemain_monthly(double remain_monthly) { this.remain_monthly = remain_monthly; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getPdf_url() { return pdf_url; }
    public void setPdf_url(String pdf_url) { this.pdf_url = pdf_url; }

    public String getExcel_url() { return excel_url; }
    public void setExcel_url(String excel_url) { this.excel_url = excel_url; }

    public String getTablon_no() {
        return tablon_no;
    }

    public void setTablon_no(String tablon_no) {
        this.tablon_no = tablon_no;
    }

    public String getCustomer_order() {
        return customer_order;
    }

    public void setCustomer_order(String customer_order) {
        this.customer_order = customer_order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }
}