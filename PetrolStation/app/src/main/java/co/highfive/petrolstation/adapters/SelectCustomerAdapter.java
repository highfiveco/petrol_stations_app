package co.highfive.petrolstation.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.listener.SelectIntListener;
import co.highfive.petrolstation.models.Customer;

public class SelectCustomerAdapter extends RecyclerView.Adapter<SelectCustomerAdapter.ViewHolder> {

    private ArrayList<CustomerDto> customers;
    int pos = -1;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    SelectIntListener selectIntListener;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView letter_type;
        private final AppCompatImageView ic_check;
        private final RelativeLayout root_item;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            root_item = (RelativeLayout) view.findViewById(R.id.root_item);
            letter_type = (AppCompatTextView) view.findViewById(R.id.letter_type);
            ic_check = (AppCompatImageView) view.findViewById(R.id.ic_check);
        }

        public AppCompatTextView getCustomerView() {
            return letter_type;
        }

        public AppCompatImageView getIc_check() {
            return ic_check;
        }

        public RelativeLayout getRoot_item() {
            return root_item;
        }
    }


    public SelectCustomerAdapter(ArrayList<CustomerDto> customers, int pos, SelectIntListener selectIntListener ) {
        this.selectIntListener = selectIntListener;
        this.customers = customers;
        this.pos = pos;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.customer_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getRoot_item().setTag(position);
        viewHolder.getRoot_item().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp_pos  = Integer.parseInt(view.getTag().toString());
                pos = temp_pos;
                notifyDataSetChanged();
                selectIntListener.selectInt(temp_pos);
            }
        });
        viewHolder.getCustomerView().setText(customers.get(position).name);

        if(pos == position){
            viewHolder.getIc_check().setVisibility(View.VISIBLE);
        }else{
            viewHolder.getIc_check().setVisibility(View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return customers.size();
    }
}
