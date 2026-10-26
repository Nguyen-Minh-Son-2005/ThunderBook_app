package com.example.bookapp.View.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Adapter.user.CartAdapter;
import com.example.bookapp.Model.CartItem;
import com.example.bookapp.R;
import com.example.bookapp.Utils.FirebaseUtils;
import com.example.bookapp.ViewModel.CartViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private LinearLayout llEmptyCart, llCheckoutBar, llHeader;
    private CheckBox cbSelectAll;
    private TextView tvCartTotal, tvEditCart;
    private Button btnCheckout, btnShopNow;

    private CartAdapter adapter;
    private final List<CartItem> cartItemList = new ArrayList<>();
    private final Map<String, String[]> bookInfoCache = new HashMap<>();

    private CartViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerView();
        setupClicks();

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartItemList.clear();
            if (items != null) cartItemList.addAll(items);
            adapter.notifyDataSetChanged();
            updateEmptyState();
            recalculateTotal();
        });

        viewModel.getBookInfoCache().observe(getViewLifecycleOwner(), cache -> {
            if (cache != null) {
                bookInfoCache.clear();
                bookInfoCache.putAll(cache);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getRemoveSuccessBookId().observe(getViewLifecycleOwner(), bookId -> {
            if (bookId != null) {
                adapter.getSelectedIds().remove(bookId);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                recalculateTotal();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String uid = FirebaseUtils.getCurrentUserId();
        if (uid != null && viewModel != null) {
            viewModel.loadCart(uid);
        }
    }

    private void bindViews(View view) {
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        llEmptyCart = view.findViewById(R.id.ll_empty_cart);
        llCheckoutBar = view.findViewById(R.id.ll_checkout_bar);
        llHeader = view.findViewById(R.id.ll_header);
        cbSelectAll = view.findViewById(R.id.cb_select_all);
        tvCartTotal = view.findViewById(R.id.tv_cart_total);
        tvEditCart = view.findViewById(R.id.tv_edit_cart);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        btnShopNow = view.findViewById(R.id.btn_shop_now);
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartItemList, bookInfoCache, new CartAdapter.OnCartActionListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                String uid = FirebaseUtils.getCurrentUserId();
                if (uid != null) viewModel.updateQuantity(uid, item, newQuantity);
                recalculateTotal();
            }

            @Override
            public void onRemove(CartItem item, int position) {
                String uid = FirebaseUtils.getCurrentUserId();
                if (uid != null) viewModel.removeItem(uid, item);
            }

            @Override
            public void onSelectionChanged(CartItem item, boolean selected) {
                recalculateTotal();
            }
        });
        rvCartItems.setAdapter(adapter);
    }

    private void setupClicks() {
        btnShopNow.setOnClickListener(v -> {
            // Quay lại tab Home trong cùng HomeActivity
            if (getActivity() instanceof HomeActivity) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_container, new HomeFragment())
                        .commit();
            }
        });

        cbSelectAll.setOnClickListener(v -> {
            boolean checked = cbSelectAll.isChecked();
            adapter.getSelectedIds().clear();
            if (checked) {
                for (CartItem item : cartItemList) {
                    adapter.getSelectedIds().add(item.getBookId());
                }
            }
            adapter.notifyDataSetChanged();
            recalculateTotal();
        });

        btnCheckout.setOnClickListener(v -> {
            if (adapter.getSelectedIds().isEmpty()) return;
            Intent intent = new Intent(getContext(), CheckoutActivity.class);
            intent.putStringArrayListExtra(CheckoutActivity.EXTRA_SELECTED_BOOK_IDS,
                    new ArrayList<>(adapter.getSelectedIds()));
            startActivity(intent);
        });
    }

    private void recalculateTotal() {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        double total = 0;
        for (CartItem item : cartItemList) {
            if (adapter.getSelectedIds().contains(item.getBookId())) {
                total += item.getSubTotal();
            }
        }
        tvCartTotal.setText(currencyFormat.format(total) + "đ");
        btnCheckout.setEnabled(!adapter.getSelectedIds().isEmpty());
    }

    private void updateEmptyState() {
        boolean isEmpty = cartItemList.isEmpty();
        llEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        llCheckoutBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        llHeader.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
