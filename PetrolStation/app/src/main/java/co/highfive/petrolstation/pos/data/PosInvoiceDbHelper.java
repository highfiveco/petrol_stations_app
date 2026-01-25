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

    // ✅ ارفع النسخة
    private static final int DB_VERSION = 4;

    public static final String TBL = "pos_active_invoices";

    // ✅ columns
    private static final String COL_ID = "id";
    private static final String COL_CUSTOMER_ID = "customer_id";
    private static final String COL_CUSTOMER_NAME = "customer_name";
    private static final String COL_CUSTOMER_MOBILE = "customer_mobile"; // ✅ NEW
    private static final String COL_ITEMS_JSON = "items_details_json";
    private static final String COL_ACCOUNT_ID = "account_id";
    private static final String COL_NOTE = "note";
    private static final String COL_PAYMENTS_JSON = "payments_json";
    private static final String COL_CREATED_AT = "created_at";

    public PosInvoiceDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE IF NOT EXISTS " + TBL + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COL_CUSTOMER_ID + " INTEGER NOT NULL," +
                        COL_CUSTOMER_NAME + " TEXT," +
                        COL_CUSTOMER_MOBILE + " TEXT," +               // ✅ NEW
                        COL_ITEMS_JSON + " TEXT NOT NULL," +
                        COL_ACCOUNT_ID + " INTEGER DEFAULT 0," +
                        COL_NOTE + " TEXT," +
                        COL_PAYMENTS_JSON + " TEXT," +
                        COL_CREATED_AT + " INTEGER NOT NULL" +
                        ")";
        db.execSQL(sql);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pos_customer_id ON " + TBL + "(" + COL_CUSTOMER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pos_created_at ON " + TBL + "(" + COL_CREATED_AT + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // upgrades to v3
        if (oldVersion < 3) {
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_ACCOUNT_ID + " INTEGER DEFAULT 0"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_NOTE + " TEXT"); } catch (Exception ignore) {}
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_PAYMENTS_JSON + " TEXT"); } catch (Exception ignore) {}
        }

        // ✅ upgrade to v4: add customer_mobile
        if (oldVersion < 4) {
            try { db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_CUSTOMER_MOBILE + " TEXT"); } catch (Exception ignore) {}
        }
    }

    /* ================= INSERT / UPDATE ================= */

    // ✅ NEW signature includes customerMobile
    public long insertInvoice(int customerId, String customerName, String customerMobile, int accountId, String itemsJson) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_CUSTOMER_ID, customerId);
        cv.put(COL_CUSTOMER_NAME, customerName);
        cv.put(COL_CUSTOMER_MOBILE, customerMobile); // ✅ NEW
        cv.put(COL_ACCOUNT_ID, accountId);
        cv.put(COL_ITEMS_JSON, itemsJson);
        cv.put(COL_CREATED_AT, System.currentTimeMillis());

        return db.insert(TBL, null, cv);
    }

    // ✅ NEW signature includes customerMobile
    public boolean updateInvoice(long invoiceId, int customerId, String customerName, String customerMobile, int accountId, String itemsJson) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_CUSTOMER_ID, customerId);
        cv.put(COL_CUSTOMER_NAME, customerName);
        cv.put(COL_CUSTOMER_MOBILE, customerMobile); // ✅ NEW
        cv.put(COL_ACCOUNT_ID, accountId);
        cv.put(COL_ITEMS_JSON, itemsJson);

        int rows = db.update(TBL, cv, COL_ID + "=?", new String[]{String.valueOf(invoiceId)});
        return rows > 0;
    }

    /* ================= READ ================= */

    public List<PosActiveInvoice> listInvoices() {
        SQLiteDatabase db = getReadableDatabase();
        List<PosActiveInvoice> out = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT " +
                        COL_ID + "," +
                        COL_ACCOUNT_ID + "," +
                        COL_CUSTOMER_ID + "," +
                        COL_CUSTOMER_NAME + "," +
                        COL_CUSTOMER_MOBILE + "," +     // ✅ NEW
                        COL_ITEMS_JSON + "," +
                        COL_NOTE + "," +
                        COL_PAYMENTS_JSON + "," +
                        COL_CREATED_AT +
                        " FROM " + TBL + " ORDER BY " + COL_ID + " DESC",
                null
        );

        try {
            while (c.moveToNext()) {
                PosActiveInvoice inv = new PosActiveInvoice();
                inv.id = c.getLong(0);
                inv.accountId = c.getInt(1);
                inv.customerId = c.getInt(2);
                inv.customerName = c.getString(3);
                inv.customerMobile = c.getString(4);  // ✅ NEW
                inv.itemsJson = c.getString(5);
                inv.note = c.getString(6);
                inv.paymentsJson = c.getString(7);
                inv.createdAt = c.getLong(8);
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
                "SELECT " +
                        COL_ID + "," +
                        COL_CUSTOMER_ID + "," +
                        COL_ACCOUNT_ID + "," +
                        COL_CUSTOMER_NAME + "," +
                        COL_CUSTOMER_MOBILE + "," +    // ✅ NEW
                        COL_ITEMS_JSON + "," +
                        COL_NOTE + "," +
                        COL_PAYMENTS_JSON + "," +
                        COL_CREATED_AT +
                        " FROM " + TBL + " WHERE " + COL_ID + "=? LIMIT 1",
                new String[]{String.valueOf(invoiceId)}
        );

        try {
            if (c.moveToFirst()) {
                PosActiveInvoice inv = new PosActiveInvoice();
                inv.id = c.getLong(0);
                inv.customerId = c.getInt(1);
                inv.accountId = c.getInt(2);
                inv.customerName = c.getString(3);
                inv.customerMobile = c.getString(4); // ✅ NEW
                inv.itemsJson = c.getString(5);
                inv.note = c.getString(6);
                inv.paymentsJson = c.getString(7);
                inv.createdAt = c.getLong(8);
                return inv;
            }
            return null;
        } finally {
            c.close();
        }
    }

    /* ================= PARTIAL UPDATES ================= */

    public boolean updateInvoiceItemsJson(long invoiceId, String itemsJson) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_ITEMS_JSON, itemsJson);

            int rows = db.update(TBL, cv, COL_ID + "=?", new String[]{String.valueOf(invoiceId)});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateInvoiceCheckout(long invoiceId, String note, String paymentsJson) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTE, note);
        cv.put(COL_PAYMENTS_JSON, paymentsJson);

        int rows = db.update(TBL, cv, COL_ID + "=?", new String[]{String.valueOf(invoiceId)});
        return rows > 0;
    }

    // ✅ optional helper: update only customer fields
    public boolean updateInvoiceCustomer(long invoiceId, int customerId, String customerName, String customerMobile, int accountId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_CUSTOMER_ID, customerId);
            cv.put(COL_CUSTOMER_NAME, customerName);
            cv.put(COL_CUSTOMER_MOBILE, customerMobile);
            cv.put(COL_ACCOUNT_ID, accountId);

            int rows = db.update(TBL, cv, COL_ID + "=?", new String[]{String.valueOf(invoiceId)});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteInvoice(long invoiceId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            int rows = db.delete(TBL, COL_ID + "=?", new String[]{String.valueOf(invoiceId)});
            return rows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateInvoiceAccountId(long invoiceId, int accountId) {
        SQLiteDatabase db = getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("account_id", accountId);
        db.update("pos_invoices", cv, "id=?", new String[]{String.valueOf(invoiceId)});
    }
}
