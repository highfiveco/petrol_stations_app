package co.highfive.petrolstation.models;

public class SelectedCloseFundItem {
    public String fundId;
    public String fundName;
    public String amount;

    public SelectedCloseFundItem(String fundId, String fundName) {
        this.fundId = fundId;
        this.fundName = fundName;
        this.amount = "";
    }
}
