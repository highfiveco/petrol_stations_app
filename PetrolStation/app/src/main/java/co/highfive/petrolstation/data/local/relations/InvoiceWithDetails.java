package co.highfive.petrolstation.data.local.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import co.highfive.petrolstation.data.local.entities.InvoiceEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;

public class InvoiceWithDetails {
    @Embedded public InvoiceEntity invoice;

    @Relation(
            entity = InvoiceDetailEntity.class,
            parentColumn = "id",
            entityColumn = "invoiceId"
    )
    public List<InvoiceDetailWithItem> details;
}
