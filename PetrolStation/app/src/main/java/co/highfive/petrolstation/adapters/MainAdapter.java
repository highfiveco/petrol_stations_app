package co.highfive.petrolstation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.activities.*;
import co.highfive.petrolstation.databinding.MainItemViewLayoutBinding;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.MainItemView;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<MainItemView> list;
    private Context context;
    private BaseActivity baseActivity;

    public MainAdapter(List<MainItemView> list, Context context) {
        this.list = list;
        this.context = context;
        this.baseActivity = (BaseActivity) context;
    }

    public void updateData(List<MainItemView> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MainItemViewLayoutBinding binding = MainItemViewLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MainItemView item = list.get(position);
        holder.binding.icon.setImageResource(item.getIcon());
        holder.binding.text.setText(item.getName());
        holder.binding.customersLayout.setBackgroundResource(item.getBg_color());

        // ✅ تنفيذ الأكشن بناءً على الـ ID مثل الكود القديم
        holder.binding.rootItem.setOnClickListener(v -> {
            switch (item.getId()) {
                case 1: // customers
                    baseActivity.moveToActivity(baseActivity, CustomersActivity.class, null, false);
                    break;
                case 2: // finance
                    baseActivity.moveToActivity(baseActivity, FinanceActivity.class, null, false);
                    break;
                case 3: // financial funds
                    baseActivity.moveToActivity(baseActivity, FinancialFundsActivity.class, null, false);
                    break;
                case 4: // monthly readings
//                    baseActivity.moveToActivity(baseActivity, MonthlyReadingsActivity.class, null, false);
                    break;
                case 5: // disconnected readings
//                    baseActivity.moveToActivity(baseActivity, ExpelledReadingsActivity.class, null, false);
                    break;
                case 6: // notifications
                    baseActivity.moveToActivity(baseActivity, NotificationsActivity.class, null, false);
                    break;
                case 7: // pos
                    baseActivity.moveToActivity(baseActivity, PosActivity.class, null, false);
                    break;
                case 8: // fule_sale
                    baseActivity.moveToActivity(baseActivity, FuelSaleActivity.class, null, false);
                    break;
                case 9: // invoices

                    baseActivity.moveToActivity(baseActivity, CustomerInvoicesActivity.class, null, false);
                    break;
                case 10: // about
                    baseActivity.moveToActivity(baseActivity, AboutActivity.class, null, false);
                    break;
                case 11: // maintenances
//                    baseActivity.moveToActivity(baseActivity, MaintenancesActivity.class, null, false);
                    break;
                case 12: // collection
//                    baseActivity.moveToActivity(baseActivity, CollectionActivity.class, null, false);
                    break;
                case 13: // refresh data
                    baseActivity.toast("Refreshing data...");
                    break;
                case 14: // sync data
                    baseActivity.moveToActivity(baseActivity, SyncOfflineDataActivity.class, null, false);
                    break;
                default:
                    baseActivity.toast("Unknown action");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MainItemViewLayoutBinding binding;
        ViewHolder(MainItemViewLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
