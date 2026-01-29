package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "offline_financial_transactions",
        indices = {
                @Index("accountId"),
                @Index("customerId"),
                @Index("syncStatus"),
                @Index("createdAtTs")
        }
)
public class OfflineFinancialTransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public long localId;

    public Integer onlineId;

    public Integer customerId;
    public Integer accountId;

    public int actionType;
    public Integer paymentTypeId;
    public Integer bankId;

    public String chequeNo;
    public String dueDate;
    public String date;

    public Integer currencyId;
    public String amount;
    public String statement;
    public String notes;

    public Integer typeMoveId;

    public String requestJson;

    public int syncStatus;
    public String syncError;

    public long createdAtTs;
    public long updatedAtTs;
}
