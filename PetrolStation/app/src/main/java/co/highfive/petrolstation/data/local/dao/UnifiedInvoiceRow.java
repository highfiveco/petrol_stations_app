package co.highfive.petrolstation.data.local.dao;

public class UnifiedInvoiceRow {

    // "ONLINE" or "OFFLINE"
    public String source;

    public Integer onlineId;
    public Long localId;

    public int customerId;
    public long sortTs;

    public String statement;
    public Double total;
    public String invoiceNo;

    // for OFFLINE: 0/1/2
    public Integer syncStatus;
}
