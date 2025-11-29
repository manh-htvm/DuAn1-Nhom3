package fpl.manhph61584.duan1_nhom3_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private Context context;
    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onEdit(Voucher voucher);
        void onDelete(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
    }

    public void setOnVoucherClickListener(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    public void updateVouchers(List<Voucher> vouchers) {
        this.voucherList = vouchers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView txtVoucherCode, txtVoucherStatus, txtVoucherName, txtVoucherDescription;
        TextView txtDiscount, txtUsageInfo, txtDateRange;
        Button btnEdit, btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVoucherCode = itemView.findViewById(R.id.txtVoucherCode);
            txtVoucherStatus = itemView.findViewById(R.id.txtVoucherStatus);
            txtVoucherName = itemView.findViewById(R.id.txtVoucherName);
            txtVoucherDescription = itemView.findViewById(R.id.txtVoucherDescription);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            txtUsageInfo = itemView.findViewById(R.id.txtUsageInfo);
            txtDateRange = itemView.findViewById(R.id.txtDateRange);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Voucher voucher) {
            // Code
            txtVoucherCode.setText(voucher.getCode() != null ? voucher.getCode() : "");

            // Status
            boolean isActive = voucher.isActive();
            txtVoucherStatus.setText(isActive ? "Đang hoạt động" : "Đã tắt");
            txtVoucherStatus.setBackgroundColor(isActive ? 0xFF4CAF50 : 0xFF757575);

            // Name
            txtVoucherName.setText(voucher.getName() != null ? voucher.getName() : "");

            // Description
            txtVoucherDescription.setText(voucher.getDescription() != null ? voucher.getDescription() : "");

            // Discount
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String discountType = voucher.getDiscountType() != null ? voucher.getDiscountType() : "amount";
            String discountText;
            if ("percent".equals(discountType)) {
                discountText = voucher.getDiscount() + "%";
                if (voucher.getMaxDiscount() > 0) {
                    discountText += " (tối đa " + formatter.format(voucher.getMaxDiscount()) + "₫)";
                }
            } else {
                discountText = formatter.format(voucher.getDiscount()) + "₫";
            }
            txtDiscount.setText(discountText);

            // Usage info
            int used = voucher.getUsedCount();
            int limit = voucher.getUsageLimit();
            txtUsageInfo.setText("Đã dùng: " + used + "/" + limit);

            // Date range
            String dateRange = "";
            if (voucher.getStartDate() != null) {
                dateRange += formatDate(voucher.getStartDate());
            }
            dateRange += " - ";
            if (voucher.getEndDate() != null) {
                dateRange += formatDate(voucher.getEndDate());
            }
            txtDateRange.setText(dateRange);

            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(voucher);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(voucher);
                }
            });
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateStr;
            }
        }
    }
}

