package co.highfive.petrolstation.utils;

public class CustomerFilter {
    private String searchQuery;
    private String region1;
    private String region2;
    private String drumNumber;
    private String balance;
    private String order;
    private boolean isCollected;

    private int pageNumber;
    private int pageSize;
    private int company_id;


    public CustomerFilter(int company_id, String searchQuery, String region1, String region2,
                          String drumNumber, String balance, String order,
                          boolean isCollected, int pageNumber, int pageSize) {
        this.company_id = company_id;
        this.searchQuery = searchQuery;
        this.region1 = region1;
        this.region2 = region2;
        this.drumNumber = drumNumber;
        this.balance = balance;
        this.order = order;
        this.isCollected = isCollected;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    // Getters لكل الخصائص
    public String getSearchQuery() { return searchQuery; }
    public String getRegion1() { return region1; }
    public String getRegion2() { return region2; }
    public String getDrumNumber() { return drumNumber; }
    public String getBalance() { return balance; }
    public String getOrder() { return order; }
    public boolean isCollected() { return isCollected; }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }
}