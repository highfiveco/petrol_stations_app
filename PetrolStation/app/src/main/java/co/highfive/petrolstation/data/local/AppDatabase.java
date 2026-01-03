package co.highfive.petrolstation.data.local;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {/*Customer.class, Collection.class, WeeklyReading.class, MonthlyReading.class
        , MoveTransaction.class, LoadTransaction.class, DiscountTransaction.class,
        UpdateReading.class, CustomerPhone.class, CustomerReading.class*/}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
//    public abstract CustomerDao customerDao();
//    public abstract CollectionDao collectionDao();
//    public abstract WeeklyReadingDao weeklyReadingDao();
//    public abstract MonthlyReadingDao monthlyReadingDao();
//    public abstract MoveTransactionDao moveTransactionDao();
//    public abstract LoadTransactionDao loadTransactionDao();
//    public abstract DiscountTransactionDao discountTransactionDao();
//    public abstract UpdateReadingDao updateReadingDao();
//    public abstract CustomerPhoneDao customerPhoneDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "generator_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}