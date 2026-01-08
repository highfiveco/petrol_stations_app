package co.highfive.petrolstation.listener;

public interface CustomerListener {

    // Apply filter (name + balance)
    void onApplyFilter(String name, String balance);

    // Clear filter
    void onClearFilter();
}
