package com.example.bookapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp.Adapter.admin.AdminRankItem;
import com.example.bookapp.Model.Book;
import com.example.bookapp.Model.Order;
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

import com.github.mikephil.charting.data.Entry;

/** Dùng cho StatisticActivity — doanh thu theo kỳ, top sách, top khách hàng. */
public class AdminStatisticViewModel extends ViewModel {

    public enum Period {DAY, WEEK, MONTH, YEAR}

    public static class ChartResult {
        private final double totalRevenue;
        private final List<Entry> entries;
        private final List<String> labels;

        public ChartResult(double totalRevenue, List<Entry> entries, List<String> labels) {
            this.totalRevenue = totalRevenue;
            this.entries = entries;
            this.labels = labels;
        }

        public double getTotalRevenue() { return totalRevenue; }
        public List<Entry> getEntries() { return entries; }
        public List<String> getLabels() { return labels; }
    }

    private final AdminOrderRepository orderRepository = new AdminOrderRepository();
    private final AdminBookRepository bookRepository = new AdminBookRepository();
    private final AdminUserRepository userRepository = new AdminUserRepository();

    private final MutableLiveData<Double> revenue = new MutableLiveData<>();
    private final MutableLiveData<Integer> orderCount = new MutableLiveData<>();
    private final MutableLiveData<Double> averageOrderValue = new MutableLiveData<>();
    private final MutableLiveData<ChartResult> chartRevenue = new MutableLiveData<>();
    private final MediatorLiveData<List<AdminRankItem>> topBooks = new MediatorLiveData<>();
    private final MediatorLiveData<List<AdminRankItem>> topCustomers = new MediatorLiveData<>();

    public LiveData<Double> getRevenue() {
        return revenue;
    }

    public LiveData<Integer> getOrderCount() {
        return orderCount;
    }

    public LiveData<Double> getAverageOrderValue() {
        return averageOrderValue;
    }

    public LiveData<ChartResult> getChartRevenue() {
        return chartRevenue;
    }

    public LiveData<List<AdminRankItem>> getTopBooks() {
        return topBooks;
    }

    public LiveData<List<AdminRankItem>> getTopCustomers() {
        return topCustomers;
    }

    /**
     * Gọi khi chọn chip Ngày/Tuần/Tháng/Năm — tính khoảng [fromDate, toDate] rồi lấy TOÀN
     * BỘ đơn hàng trong khoảng đó 1 LẦN, tự tính ra cả 3 số liệu (doanh thu, số đơn, giá
     * trị trung bình/đơn) thay vì gọi Firestore riêng cho từng số — khớp đúng 3 TextView
     * tv_stat_revenue_value/tv_stat_order_value/tv_stat_aov_value trên StatisticActivity.
     */
    private LiveData<List<Order>> currentOrdersLiveData;
    private androidx.lifecycle.Observer<List<Order>> currentOrdersObserver;
    public void loadRevenue(Period period) {
        Date[] range = calculateDateRange(period);
        revenue.setValue(null);
        orderCount.setValue(null);
        averageOrderValue.setValue(null);

        if (currentOrdersLiveData != null && currentOrdersObserver != null) {
            currentOrdersLiveData.removeObserver(currentOrdersObserver);
        }

        currentOrdersLiveData = orderRepository.getOrdersInRange(range[0], range[1]);
        currentOrdersObserver = orders -> {
            if (orders == null) return;
            double total = 0;
            int count = orders.size();
            
            List<Entry> chartEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            Calendar cal = Calendar.getInstance();

            if (period == Period.DAY) {
                double[] sums = new double[24];
                for (Order o : orders) {
                    if (o.getCreatedAt() != null) {
                        total += o.getFinalTotal();
                        cal.setTime(o.getCreatedAt().toDate());
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        sums[hour] += o.getFinalTotal();
                    }
                }
                for (int i = 0; i < 24; i++) {
                    chartEntries.add(new Entry(i, (float) sums[i]));
                    labels.add(i + "h");
                }
            } else if (period == Period.WEEK) {
                double[] sums = new double[7];
                String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (Order o : orders) {
                    if (o.getCreatedAt() != null) {
                        total += o.getFinalTotal();
                        cal.setTime(o.getCreatedAt().toDate());
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sun
                        int index = (dayOfWeek + 5) % 7; 
                        sums[index] += o.getFinalTotal();
                    }
                }
                for (int i = 0; i < 7; i++) {
                    chartEntries.add(new Entry(i, (float) sums[i]));
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
                    chartEntries.add(new Entry(i, (float) sums[i]));
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
                    chartEntries.add(new Entry(i, (float) sums[i]));
                    labels.add("T" + (i + 1));
                }
            }

            revenue.setValue(total);
            orderCount.setValue(count);
            averageOrderValue.setValue(count > 0 ? total / count : 0);
            chartRevenue.setValue(new ChartResult(total, chartEntries, labels));
        };
        currentOrdersLiveData.observeForever(currentOrdersObserver);
    }

    private Date[] calculateDateRange(Period period) {
        Calendar cal = Calendar.getInstance();
        Date toDate = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (period) {
            case DAY:
                // Already set to 00:00:00 above
                break;
            case WEEK:
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.getTime(); // Bắt buộc gọi để recompute week nếu hôm nay là Chủ nhật
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

    private LiveData<List<Book>> currentTopBooksSource;
    /** Top N sách bán chạy — map từ Book sang AdminRankItem để AdminStatRankAdapter dùng chung. */
    public void loadTopBooks(int limit) {
        if (currentTopBooksSource != null) {
            topBooks.removeSource(currentTopBooksSource);
        }
        currentTopBooksSource = bookRepository.getTopSellingBooks(limit);
        topBooks.addSource(currentTopBooksSource, books -> {
            List<AdminRankItem> items = new ArrayList<>();
            if (books != null) {
                for (Book b : books) {
                    items.add(new AdminRankItem(b.getCoverImageUrl(), b.getTitle(),
                            "Đã bán " + b.getSoldCount()));
                }
            }
            topBooks.setValue(items);
        });
    }

    private LiveData<List<Order>> currentTopCustomersSource;
    /**
     * Top N khách hàng mua nhiều đơn nhất — Firestore không hỗ trợ GROUP BY, nên đếm bằng
     * cách gom nhóm client-side trên danh sách đơn hàng đã tải (chấp nhận được ở quy mô
     * bài tập lớn; nếu dữ liệu lớn hơn nên chuyển sang Cloud Function tính sẵn).
     */
    public void loadTopCustomers(int limit) {
        if (currentTopCustomersSource != null) {
            topCustomers.removeSource(currentTopCustomersSource);
        }
        currentTopCustomersSource = orderRepository.observeAllOrders();
        topCustomers.addSource(currentTopCustomersSource, orders -> {
            if (orders == null) return;

            Map<String, Integer> orderCountByUser = new HashMap<>();
            Map<String, String> nameByUser = new HashMap<>();
            for (Order o : orders) {
                String uid = o.getUserId();
                if (uid == null) continue;
                orderCountByUser.put(uid, orderCountByUser.getOrDefault(uid, 0) + 1);
                if (o.getShippingAddress() != null) {
                    nameByUser.put(uid, o.getShippingAddress().getName());
                }
            }

            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(orderCountByUser.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            List<AdminRankItem> items = new ArrayList<>();
            int count = 0;
            for (Map.Entry<String, Integer> entry : sorted) {
                if (count >= limit) break;
                String name = nameByUser.getOrDefault(entry.getKey(), "Khách hàng");
                items.add(new AdminRankItem(null, name, entry.getValue() + " đơn hàng"));
                count++;
            }
            topCustomers.setValue(items);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentOrdersLiveData != null && currentOrdersObserver != null) {
            currentOrdersLiveData.removeObserver(currentOrdersObserver);
        }
    }
}
