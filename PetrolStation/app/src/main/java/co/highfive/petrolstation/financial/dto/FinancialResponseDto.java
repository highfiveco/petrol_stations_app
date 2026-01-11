package co.highfive.petrolstation.financial.dto;

import java.util.List;

import co.highfive.petrolstation.models.Account;
import co.highfive.petrolstation.models.Currency;
import co.highfive.petrolstation.models.Setting;

public class FinancialResponseDto {
    public int check_last_reading_sanad;
    public int has_move;
    public int has_load;
    public int has_discount;
    public int view_statement;
    public int view_date;
    public int payment_type_default;
    public int disabled_type_move;
    public int default_type_income;

    public Account account;

    public List<Currency> payment_type;
    public List<Currency> currency;
    public List<Currency> type_loaded;
    public List<Currency> month_load;
    public List<Currency> type_income;
    public List<Currency> bank;

    public Setting setting;
}
