package com.example.qrcardproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        void onFavoriteClick(Friend friend);

        void onFavoriteClick(Friend friend, boolean isNowFavorite);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_header, parent, false);
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
            FriendViewHolder viewHolder = (FriendViewHolder) holder;

            if (viewHolder.nameText != null) {
                viewHolder.nameText.setText(friend.getName());
            }

            // 즐겨찾기 아이콘 표시
            if (friend.isFavorite()) {
                viewHolder.favoriteIcon.setImageResource(R.drawable.ic_star_filled);
            } else {
                viewHolder.favoriteIcon.setImageResource(R.drawable.ic_star_border);
            }

            // 즐겨찾기 아이콘 클릭 리스너
            viewHolder.favoriteIcon.setOnClickListener(v -> {
                friend.setFavorite(!friend.isFavorite()); // 상태 변경
                notifyItemChanged(position); // UI 갱신
                listener.onFavoriteClick(friend); // 외부에 알림
            });

            // 친구 클릭 시 상세 보기
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
        ImageView favoriteButton;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.sectionHeader);
            favoriteButton = itemView.findViewById(R.id.btnFavorite);
        }
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageView favoriteIcon; // 즐겨찾기 버튼

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.friendName);
            favoriteIcon = itemView.findViewById(R.id.btnFavorite); // 여기!
        }
    }


}
