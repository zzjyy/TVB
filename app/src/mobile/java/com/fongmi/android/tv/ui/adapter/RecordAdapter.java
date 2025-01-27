package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.AdapterCollectRecordBinding;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

//jim add
import com.google.gson.Gson;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final List<String> mItems;

    public RecordAdapter(OnClickListener listener) {
        this.mListener = listener;
        this.mItems = getItems();
        this.mListener.onDataChanged(mItems.size());
    }

    public interface OnClickListener {

        void onItemClick(String text);

        void onDataChanged(int size);
    }

    private List<String> getItems() {
        if (Setting.getKeyword().isEmpty()) return new ArrayList<>();
        return App.gson().fromJson(Setting.getKeyword(), new TypeToken<List<String>>() {}.getType());
    }

    private void checkToAdd(String item) {
        int index = mItems.indexOf(item);
        if (index == -1) {
            mItems.add(0, item);
            notifyItemInserted(0);
        } else {
            mItems.remove(index);
            mItems.add(0, item);
            notifyItemRangeChanged(0, mItems.size());
        }
        if (mItems.size() > 8) {
            mItems.remove(8);
            notifyItemRemoved(8);
        }
    }

    public void add(String item) {
        checkToAdd(item);
        mListener.onDataChanged(getItemCount());
        Setting.putKeyword(App.gson().toJson(mItems));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterCollectRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = mItems.get(position);
        holder.binding.text.setText(text);
        holder.binding.text.setOnClickListener(v -> mListener.onItemClick(text));
    }

    // jim add
    private Gson mGson = new Gson();

    public void deleteAllItems() {
        mItems.clear();
        notifyDataSetChanged();
        mListener.onDataChanged(0);
        Setting.putKeyword(mGson.toJson(mItems));
    }
    //end if

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private final AdapterCollectRecordBinding binding;

        ViewHolder(@NonNull AdapterCollectRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            mItems.remove(getLayoutPosition());
            notifyItemRemoved(getLayoutPosition());
            mListener.onDataChanged(getItemCount());
            Setting.putKeyword(App.gson().toJson(mItems));
            return true;
        }
    }
}
