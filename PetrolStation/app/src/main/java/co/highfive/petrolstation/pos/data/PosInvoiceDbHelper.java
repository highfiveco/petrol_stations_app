package co.highfive.petrolstation.pos.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.pos.dto.PosActiveInvoice;

public class PosInvoiceDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pos_local.db";
    private static final int DB_VERSION =  3;

    public static final String TBL = "pos_active_invoices";

    public PosInvoiceDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE IF NOT EXISTS " + TBL + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customer_id INTEGER NOT NULL," +
                        "customer_name TEXT," +
                        "items_details_json TEXT NOT NULL," +
                        "account_id INTEGER DEFAULT 0," +
                        "note TEXT," +
                        "payments_json TEXT," +
                        "created_at INTEGER NOT NULL" +
                        ")";
        db.execSQL(sql);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pos_customer_id ON " + TBL + "(customer_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pos_created_at ON " + TBL + "(created_at)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 3) {
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN account_id INTEGER DEFAULT 0"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN note TEXT"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN payments_json TEXT"); } catch (Exception ignore) {}
        }



    }
    public long insertInvoice(int customerId, String customerName, int accountId, String itemsJson) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("customer_id", customerId);
        cv.put("customer_name", customerName);
        cv.put("account_id", accountId);
        cv.put("items_details_json", itemsJson);
        cv.put("created_at", System.currentTimeMillis());

        return db.insert("pos_active_invoices", null, cv);
    }

    public List<PosActiveInvoice> listInvoices() {
        SQLiteDatabase db = getReadableDatabase();
        List<PosActiveInvoice> out = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT id,account_id, customer_id, customer_name, items_details_json, note, payments_json, created_at FROM " + TBL + " ORDER BY id DESC",
                null
        );

        try {
            while (c.moveToNext()) {
                PosActiveInvoice inv = new PosActiveInvoice();
                inv.id = c.getLong(0);
                inv.accountId = c.getInt(1);
                inv.customerId = c.getInt(2);
                inv.customerName = c.getString(3);
                inv.itemsJson = c.getString(4);
                inv.note = c.getString(5);
                inv.paymentsJson = c.getString(6);
                inv.createdAt = c.getLong(7);
                out.add(inv);
            }
        } finally {
            c.close();
        }

        return out;
    }

    public PosActiveInvoice getInvoice(long invoiceId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, customer_id,account_id, customer_name, items_details_json, note, payments_json, created_at " +
                        "FROM " + TBL + " WHERE id=? LIMIT 1",
                new String[]{String.valueOf(invoiceId)}
        );

        try {
            if (c.moveToFirst()) {
                PosActiveInvoice inv = new PosActiveInvoice();
                inv.id = c.getLong(0);
                inv.customerId = c.getInt(1);
                inv.accountId = c.getInt(2);
                inv.customerName = c.getString(3);
                inv.itemsJson = c.getString(4);
                inv.note = c.getString(5);
                inv.paymentsJson = c.getString(6);
                inv.createdAt = c.getLong(7);
                return inv;
            }
            return null;
        } finally {
            c.close();
        }
    }


    public boolean updateInvoice(long invoiceId, int customerId, String customerName, int accountId, String itemsJson) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("customer_id", customerId);
        cv.put("customer_name", customerName);
        cv.put("account_id", accountId);
        cv.put("items_details_json", itemsJson);

        int rows = db.update("pos_active_invoices", cv, "id=?", new String[]{String.valueOf(invoiceId)});
        return rows > 0;
    }

    public boolean updateInvoiceItemsJson(long invoiceId, String itemsJson) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("items_details_json", itemsJson);

            int rows = db.update(TBL, cv, "id = ?", new String[]{String.valueOf(invoiceId)});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateInvoiceCheckout(long invoiceId, String note, String paymentsJson) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("note", note);
        cv.put("payments_json", paymentsJson);
        int rows = db.update(TBL, cv, "id=?", new String[]{String.valueOf(invoiceId)});
        return rows > 0;
    }
    public boolean deleteInvoice(long invoiceId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.delete(TBL, "id = ?", new String[]{String.valueOf(invoiceId)});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }


}
