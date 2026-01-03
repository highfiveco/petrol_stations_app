package co.highfive.petrolstation.data.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CollectionRepository {
//    private final CollectionDao collectionDao;
//    private final ExecutorService executorService;
//
//    public CollectionRepository(Application application) {
//        AppDatabase database = AppDatabase.getInstance(application);
//        this.collectionDao = database.collectionDao();
//        this.executorService = Executors.newSingleThreadExecutor();
//    }
//
//    // Basic operations
//    public void insert(Collection collection) {
//        executorService.execute(() -> collectionDao.insert(collection));
//    }
//
//    public void update(Collection collection) {
//        executorService.execute(() -> collectionDao.update(collection));
//    }
//
//    public void delete(Collection collection) {
//        executorService.execute(() -> collectionDao.deleteById(collection.getId()));
//    }
//
//    // Bulk operations
//    public void insertAll(List<Collection> collections) {
//        executorService.execute(() -> collectionDao.insertAll(collections));
//    }
//
//    public void updateAll(List<Collection> collections) {
//        executorService.execute(() -> collectionDao.updateAll(collections));
//    }
//
//    public void deleteAll() {
//        executorService.execute(() -> collectionDao.deleteAll());
//    }
//
//    // Query operations
//    public List<Collection> getAllCollections() {
//        return collectionDao.getAll();
//    }
//
//    public LiveData<Collection> getCollectionById(int id) {
//        return collectionDao.getByIdLive(id);
//    }
//
//    public LiveData<List<Collection>> getCollectionsByAccount(int accountId) {
//        return collectionDao.getByAccountId(accountId);
//    }
//
//    public LiveData<List<Collection>> searchCollections(String searchTerm) {
//        return collectionDao.searchCollections(searchTerm);
//    }
//
//    public LiveData<List<Collection>> getCollectionsByMonth(String month) {
//        return collectionDao.getByMonth(month);
//    }
//
//    // Advanced operations
//    public void upsert(Collection collection) {
//        executorService.execute(() -> collectionDao.upsert(collection));
//    }
//
//    public void upsertAll(List<Collection> collections) {
//        executorService.execute(() -> collectionDao.upsertAll(collections));
//    }
//
//    public LiveData<Integer> getCollectionsCount() {
//        return collectionDao.count();
//    }
//
//    public Collection getCustomerByAccountId(String accountId) {
//        return collectionDao.getCustomerByAccountId(accountId);
//    }
//
//
//    public List<site.ebill.generator.data.local.entities.Collection> getFilteredCollections(CollectionFilter filter) {
//        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM collections WHERE 1=1 ");
//        List<Object> args = new ArrayList<>();
//
//        // تطبيق معايير البحث
//        if (filter.getName() != null && !filter.getName().isEmpty()) {
//            queryBuilder.append("AND (name LIKE ? OR mobile LIKE ?) ");
//            args.add("%" + filter.getName() + "%");
//            args.add("%" + filter.getName() + "%");
//        }
//
//        if (filter.getFirst_area() != null) {
//            queryBuilder.append("AND first_area = ? ");
//            args.add(filter.getFirst_area());
//        }
//
//        if (filter.getSecond_area() != null) {
//            queryBuilder.append("AND second_area = ? ");
//            args.add(filter.getSecond_area());
//        }
//
//        if (filter.getStatus() != null) {
//            queryBuilder.append("AND status = ? ");
//            args.add(filter.getStatus());
//        }
//
//        if (filter.getCompany_id() != 0) {
//            queryBuilder.append("AND company_id = ? ");
//            args.add(filter.getCompany_id());
//        }
//
////        queryBuilder.append("AND isCollected = ? ");
////        args.add(filter.isCollected());
//
//        if (filter.getBalance() != null) {
//            queryBuilder.append("AND balance >= ? ");
//            args.add(filter.getBalance());
//        }
//
//        if (filter.getOrder() != null) {
//            queryBuilder.append("ORDER BY ");
//            switch (filter.getOrder()) {
//                case "1": queryBuilder.append("name ASC"); break;
//                case "2": queryBuilder.append("name DESC"); break;
//                default: queryBuilder.append("customer_order ASC");
//            }
//        } else {
//            queryBuilder.append("ORDER BY ");
//            queryBuilder.append("customer_order ASC");
//        }
//
//        if ( filter.getPageNumber() > 0 && filter.getPageSize() > 0) {
//            int offset = (filter.getPageNumber() - 1) * filter.getPageSize();
//            queryBuilder.append(" LIMIT ? OFFSET ? ");
//            args.add(filter.getPageSize());
//            args.add(offset);
//        }
//
//        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
//                queryBuilder.toString(),
//                args.toArray()
//        );
//
//        return collectionDao.getFilteredCollections(query);
//    }


}