package com.example.qrcardproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_FRIEND = 1;

    private List<FriendListItem> itemList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    public FriendsAdapter(List<FriendListItem> itemList, OnFriendClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (itemList.get(position) instanceof SectionHeader) ? VIEW_TYPE_HEADER : VIEW_TYPE_FRIEND;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_contacts, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts, parent, false);
            return new FriendViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            SectionHeader header = (SectionHeader) itemList.get(position);
            ((HeaderViewHolder) holder).headerText.setText(header.getHeader());
        } else {
            Friend friend = (Friend) itemList.get(position);
            ((FriendViewHolder) holder).nameText.setText(friend.getName());
            holder.itemView.setOnClickListener(v -> listener.onFriendClick(friend));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setData(List<FriendListItem> newList) {
        itemList.clear();
        itemList.addAll(newList);
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.sectionHeader);
        }
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.friendName);
        }
    }
}
