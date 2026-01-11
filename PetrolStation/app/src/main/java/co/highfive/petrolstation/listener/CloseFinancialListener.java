package co.highfive.petrolstation.listener;

import java.util.HashMap;

import co.highfive.petrolstation.models.Fund;

public interface CloseFinancialListener {
    void closeMulti(Fund fund, HashMap<String, String> params);
}
