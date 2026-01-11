package co.highfive.petrolstation.models;

import java.io.Serializable;

public class FundPaymentSummary implements Serializable {
    public String payment;
    public String net_balance;

    // Optional if you need them later
    public String total_debit;
    public String total_credit;
    public int is_print;
}
