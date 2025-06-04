package com.example.qrcardproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_FRIEND = 1;

    private List<FriendListItem> itemList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
        void onFavoriteToggled(Friend friend);
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
            FriendViewHolder friendHolder = (FriendViewHolder) holder;

            friendHolder.nameText.setText(friend.getName());

            // 즐겨찾기 아이콘 상태 설정
            if (friend.isFavorite()) {
                friendHolder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); // 즐겨찾기된 상태
            } else {
                friendHolder.favoriteIcon.setImageResource(R.drawable.ic_star_border); // 즐겨찾기 아님
            }

            // ✅ 알림 dot 표시 여부 설정
            if (friend.shouldShowAlert()) {
                friendHolder.alertDot.setVisibility(View.VISIBLE);
            } else {
                friendHolder.alertDot.setVisibility(View.GONE);
            }

            // 클릭 이벤트 처리
            holder.itemView.setOnClickListener(v -> listener.onFriendClick(friend));

            friendHolder.favoriteIcon.setOnClickListener(v -> {
                friend.setFavorite(!friend.isFavorite());
                notifyItemChanged(position);
                listener.onFavoriteToggled(friend);
            });
        }
    }



    @Override
    public int getItemCount() {
        return itemList.size();
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
        ImageView favoriteIcon;
        View alertDot;

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tvName);
            favoriteIcon = itemView.findViewById(R.id.btnFavorite);
            alertDot = itemView.findViewById(R.id.ivAlertDot);
        }
    }
    public void setData(List<FriendListItem> newList) {
        itemList.clear();
        itemList.addAll(newList);  // 섹션 헤더 포함된 리스트 그대로 사용
        notifyDataSetChanged();
    }



}
