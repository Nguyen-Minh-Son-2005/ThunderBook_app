package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Adapter.admin.AdminDashboardMenuItem;
import com.example.bookapp.Adapter.admin.AdminDashboardStat;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Order;
import com.example.bookapp.R;
import com.example.bookapp.Repository.AdminBookRepository;
import com.example.bookapp.Repository.AdminOrderRepository;
import com.example.bookapp.Repository.AdminUserRepository;
import com.example.bookapp.Utils.PriceFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.bookapp.Model.OrderItem;
import com.example.bookapp.Utils.Constants;
import com.example.bookapp.Utils.FirebaseCallback;
import com.example.bookapp.Utils.FirebaseUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class AdminDashboardViewModel extends ViewModel {

    public enum Period {WEEK, MONTH, YEAR}

    private final AdminBookRepository bookRepository = new AdminBookRepository();
    private final AdminOrderRepository orderRepository = new AdminOrderRepository();
    private final AdminUserRepository userRepository = new AdminUserRepository();

    public static class ChartResult {
        private final double totalRevenue;
        private final List<com.github.mikephil.charting.data.BarEntry> entries;
        private final List<String> labels;

        public ChartResult(double totalRevenue, List<com.github.mikephil.charting.data.BarEntry> entries, List<String> labels) {
            this.totalRevenue = totalRevenue;
            this.entries = entries;
            this.labels = labels;
        }

        public double getTotalRevenue() { return totalRevenue; }
        public List<com.github.mikephil.charting.data.BarEntry> getEntries() { return entries; }
        public List<String> getLabels() { return labels; }
    }

    private final MediatorLiveData<List<AdminDashboardStat>> statCards = new MediatorLiveData<>();
    private final MediatorLiveData<ChartResult> chartRevenue = new MediatorLiveData<>();
    private Integer bookCount, orderCount, userCount;
    private Double revenueThisWeek;

    private LiveData<List<Order>> currentChartOrdersLiveData;
    private androidx.lifecycle.Observer<List<Order>> currentChartOrdersObserver;

    public LiveData<List<AdminDashboardStat>> getStatCards() {
        return statCards;
    }

    public LiveData<ChartResult> getChartRevenue() {
        return chartRevenue;
    }

    public LiveData<List<Order>> getRecentOrders() {
        return orderRepository.getRecentOrders(5);
    }

    public LiveData<List<Book>> getTopSellingBooks() {
        return bookRepository.getTopSellingBooks(10);
    }

    private boolean isStatsLoaded = false;

    /**
     * Gọi 1 lần ở onCreate() Activity — tải song song 4 nguồn số liệu (đếm sách/đơn/user +
     * doanh thu tuần này), gộp thành danh sách 4 AdminDashboardStat khi ĐỦ CẢ 4 đã về, dùng
     * MediatorLiveData theo dõi nhiều nguồn cùng lúc thay vì lồng callback vào nhau.
     */
    public void loadStats() {
        if (isStatsLoaded) return;
        isStatsLoaded = true;

        Calendar cal = Calendar.getInstance();
        Date toDate = cal.getTime();
        
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.getTime(); // Bắt buộc gọi để recompute week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date fromDate = cal.getTime();

        statCards.addSource(orderRepository.getRevenueInRange(fromDate, toDate), value -> {
            revenueThisWeek = value;
            tryEmitStats();
        });
        statCards.addSource(orderRepository.countAllOrders(), value -> {
            orderCount = value;
            tryEmitStats();
        });
        statCards.addSource(userRepository.countAllUsers(), value -> {
            userCount = value;
            tryEmitStats();
        });
        statCards.addSource(bookRepository.countActiveBooks(), value -> {
            bookCount = value;
            tryEmitStats();
        });
    }

    /** Chỉ emit ra danh sách thẻ khi ĐỦ 4 giá trị đã tải xong, tránh hiện thẻ thiếu số liệu. */
    private void tryEmitStats() {
        if (revenueThisWeek == null || orderCount == null || userCount == null || bookCount == null) {
            return;
        }
        List<AdminDashboardStat> stats = new ArrayList<>();
        stats.add(new AdminDashboardStat(R.drawable.ic_revenue,
                PriceFormatter.formatVND(revenueThisWeek), "Doanh thu tuần", null));
        stats.add(new AdminDashboardStat(R.drawable.ic_orders_stat,
                String.valueOf(orderCount), "Đơn hàng", null));
        stats.add(new AdminDashboardStat(R.drawable.ic_users_stat,
                String.valueOf(userCount), "Người dùng", null));
        stats.add(new AdminDashboardStat(R.drawable.ic_products_stat,
                String.valueOf(bookCount), "Sản phẩm", null));
        statCards.setValue(stats);
    }

    /**
     * Danh sách tĩnh cho lưới 3 cột "Quản lý cửa hàng" — KHÔNG lấy từ Firestore, chỉ là
     * cấu hình điều hướng cố định. targetKey Activity sẽ dùng để switch-case mở đúng màn.
     */
    public List<AdminDashboardMenuItem> getMenuItems() {
        List<AdminDashboardMenuItem> items = new ArrayList<>();
        items.add(new AdminDashboardMenuItem(R.drawable.ic_book, "Quản lý sách", "manage_book"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_category, "Thể loại", "manage_category"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_author, "Tác giả", "manage_author"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_publisher, "Nhà xuất bản", "manage_publisher"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_order, "Đơn hàng", "manage_order"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_user, "Người dùng", "manage_user"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_voucher, "Voucher", "manage_voucher"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_notification, "Thông báo", "manage_notification"));
        items.add(new AdminDashboardMenuItem(R.drawable.ic_statistic, "Thống kê", "statistic"));
        return items;
    }

    public void loadChartRevenue(Period period) {
        Date[] range = calculateDateRange(period);
        chartRevenue.setValue(null);

        if (currentChartOrdersLiveData != null && currentChartOrdersObserver != null) {
            currentChartOrdersLiveData.removeObserver(currentChartOrdersObserver);
        }

        currentChartOrdersLiveData = orderRepository.getOrdersInRange(range[0], range[1]);
        currentChartOrdersObserver = orders -> {
            if (orders == null) return;
            double total = 0;
            List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            Calendar cal = Calendar.getInstance();

            if (period == Period.WEEK) {
                double[] sums = new double[7];
                String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (Order o : orders) {
                    if (o.getCreatedAt() != null) {
                        total += o.getFinalTotal();
                        cal.setTime(o.getCreatedAt().toDate());
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sun, 2=Mon...
                        int index = (dayOfWeek + 5) % 7; // Map Mon->0, Sun->6
                        sums[index] += o.getFinalTotal();
                    }
                }
                for (int i = 0; i < 7; i++) {
                    entries.add(new com.github.mikephil.charting.data.BarEntry(i, (float) sums[i]));
                    labels.add(dayNames[i]);
                }
            } else if (period == Period.MONTH) {
                cal.setTime(range[0]);
                int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                double[] sums = new double[daysInMonth];
                for (Order o : orders) {
                    if (o.getCreatedAt() != null) {
                        total += o.getFinalTotal();
                        cal.setTime(o.getCreatedAt().toDate());
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        sums[day - 1] += o.getFinalTotal();
                    }
                }
                for (int i = 0; i < daysInMonth; i++) {
                    entries.add(new com.github.mikephil.charting.data.BarEntry(i, (float) sums[i]));
                    labels.add(String.valueOf(i + 1));
                }
            } else if (period == Period.YEAR) {
                double[] sums = new double[12];
                for (Order o : orders) {
                    if (o.getCreatedAt() != null) {
                        total += o.getFinalTotal();
                        cal.setTime(o.getCreatedAt().toDate());
                        int month = cal.get(Calendar.MONTH); // 0-11
                        sums[month] += o.getFinalTotal();
                    }
                }
                for (int i = 0; i < 12; i++) {
                    entries.add(new com.github.mikephil.charting.data.BarEntry(i, (float) sums[i]));
                    labels.add("T" + (i + 1));
                }
            }

            chartRevenue.setValue(new ChartResult(total, entries, labels));
        };
        currentChartOrdersLiveData.observeForever(currentChartOrdersObserver);
    }

    private Date[] calculateDateRange(Period period) {
        Calendar cal = Calendar.getInstance();
        Date toDate = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (period) {
            case WEEK:
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.getTime();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case MONTH:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }
        return new Date[]{cal.getTime(), toDate};
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentChartOrdersLiveData != null && currentChartOrdersObserver != null) {
            currentChartOrdersLiveData.removeObserver(currentChartOrdersObserver);
        }
    }

    public void syncHistoricalSoldCount(FirebaseCallback<Void> callback) {
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        
        // 1. Get all DELIVERED orders
        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo(Constants.FIELD_ORDER_STATUS, Constants.ORDER_DELIVERED)
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    
                    // Map to count sold quantity per book
                    Map<String, Integer> bookSales = new HashMap<>();
                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        Order order = orderDoc.toObject(Order.class);
                        if (order.getItems() != null) {
                            for (OrderItem item : order.getItems()) {
                                if (item.getBookId() != null) {
                                    int current = bookSales.getOrDefault(item.getBookId(), 0);
                                    bookSales.put(item.getBookId(), current + item.getQuantity());
                                }
                            }
                        }
                    }

                    // 2. Reset all books' soldCount to 0, then apply new counts
                    db.collection(Constants.COLLECTION_BOOKS).get().addOnSuccessListener(bookSnapshots -> {
                        List<com.google.android.gms.tasks.Task<Void>> tasks = new ArrayList<>();
                        WriteBatch currentBatch = db.batch();
                        int opCount = 0;

                        for (QueryDocumentSnapshot bookDoc : bookSnapshots) {
                            int actualSales = bookSales.getOrDefault(bookDoc.getId(), 0);
                            currentBatch.update(bookDoc.getReference(), Constants.FIELD_SOLD_COUNT, actualSales);
                            opCount++;

                            if (opCount == 500) {
                                tasks.add(currentBatch.commit());
                                currentBatch = db.batch();
                                opCount = 0;
                            }
                        }

                        if (opCount > 0) {
                            tasks.add(currentBatch.commit());
                        }

                        com.google.android.gms.tasks.Tasks.whenAll(tasks)
                                .addOnSuccessListener(unused -> callback.onSuccess(null))
                                .addOnFailureListener(callback::onFailure);
                    }).addOnFailureListener(callback::onFailure);
                    
                }).addOnFailureListener(callback::onFailure);
    }
}
