package co.highfive.petrolstation.data.local.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;

public class InvoiceDetailWithItem {
    @Embedded public InvoiceDetailEntity detail;

    @Relation(
            parentColumn = "itemId",
            entityColumn = "id"
    )
    public ItemEntity item;
}
