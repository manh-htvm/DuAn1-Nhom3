package fpl.manhph61584.duan1_nhom3_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpl.manhph61584.duan1_nhom3_app.network.ApiClient;
import fpl.manhph61584.duan1_nhom3_app.network.dto.OrderDto;
import fpl.manhph61584.duan1_nhom3_app.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private TextView txtEmptyOrders, tabAll, tabUnpaid, tabPaid;
    private ImageView btnBack;
    private OrderHistoryAdapter adapter;
    private List<OrderDto> allOrders = new ArrayList<>();
    private String currentFilter = "all"; // "all", "unpaid", "paid"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        txtEmptyOrders = findViewById(R.id.txtEmptyOrders);
        tabAll = findViewById(R.id.tabAll);
        tabUnpaid = findViewById(R.id.tabUnpaid);
        tabPaid = findViewById(R.id.tabPaid);
        btnBack = findViewById(R.id.btnBack);

        adapter = new OrderHistoryAdapter(this, new ArrayList<>());
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(adapter);

        setupTabs();
        loadOrders();

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateTabSelection();
            filterOrders();
        });

        tabUnpaid.setOnClickListener(v -> {
            currentFilter = "unpaid";
            updateTabSelection();
            filterOrders();
        });

        tabPaid.setOnClickListener(v -> {
            currentFilter = "paid";
            updateTabSelection();
            filterOrders();
        });
    }

    private void updateTabSelection() {
        tabAll.setTextColor(getResources().getColor(currentFilter.equals("all") ? R.color.primary : android.R.color.darker_gray));
        tabAll.setTypeface(null, currentFilter.equals("all") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        
        tabUnpaid.setTextColor(getResources().getColor(currentFilter.equals("unpaid") ? R.color.primary : android.R.color.darker_gray));
        tabUnpaid.setTypeface(null, currentFilter.equals("unpaid") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        
        tabPaid.setTextColor(getResources().getColor(currentFilter.equals("paid") ? R.color.primary : android.R.color.darker_gray));
        tabPaid.setTypeface(null, currentFilter.equals("paid") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void loadOrders() {
        String token = UserManager.getAuthToken();
        if (token == null) {
            txtEmptyOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            return;
        }

        String authHeader = "Bearer " + token;
        ApiClient.getApiService().getOrders(authHeader, null).enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());
                    filterOrders();
                } else {
                    txtEmptyOrders.setVisibility(View.VISIBLE);
                    recyclerViewOrders.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                txtEmptyOrders.setVisibility(View.VISIBLE);
                recyclerViewOrders.setVisibility(View.GONE);
            }
        });
    }

    private void filterOrders() {
        List<OrderDto> filteredOrders = new ArrayList<>();
        
        for (OrderDto order : allOrders) {
            if (currentFilter.equals("all")) {
                filteredOrders.add(order);
            } else if (currentFilter.equals("unpaid") && "unpaid".equals(order.getPaymentStatus())) {
                filteredOrders.add(order);
            } else if (currentFilter.equals("paid") && "paid".equals(order.getPaymentStatus())) {
                filteredOrders.add(order);
            }
        }

        adapter.updateOrders(filteredOrders);
        
        if (filteredOrders.isEmpty()) {
            txtEmptyOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            txtEmptyOrders.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

