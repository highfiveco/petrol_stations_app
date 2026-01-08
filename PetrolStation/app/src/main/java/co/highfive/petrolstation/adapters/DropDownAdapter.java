package co.highfive.petrolstation.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.models.Currency;

public class DropDownAdapter extends RecyclerView.Adapter<DropDownAdapter.MyViewHolder> {
    private List<Currency> statusList;
    OnItemClickListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            title =  view.findViewById(android.R.id.text1);
        }
    }

    public DropDownAdapter(List<Currency> statusList) {
        this.statusList = statusList;
    }

    public DropDownAdapter(List<Currency> statusList, OnItemClickListener listener) {
        this.statusList = statusList;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_spinner_dropdown_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Currency status = statusList.get(position);
        holder.title.setText(status.getName());
        holder.title.setOnClickListener(v -> listener.onItemClick(v, position));
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}