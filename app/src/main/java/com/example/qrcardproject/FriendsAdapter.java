package com.example.qrcardproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    public FriendsAdapter(List<Friend> friendsList, OnFriendClickListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.nameTextView.setText(friend.getName());

        // 즐겨찾기 아이콘 상태 설정
        if (friend.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); // 즐겨찾기 O
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_border); // 즐겨찾기 X
        }

        // 즐겨찾기 아이콘 클릭 시 토글
        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newStatus = !friend.isFavorite();
            friend.setFavorite(newStatus);
            notifyItemChanged(position);

            // Firestore 업데이트
            FirebaseFirestore.getInstance()
                    .collection("friends")
                    .document(friend.getId())
                    .update("favorite", newStatus);
        });

        // 전체 항목 클릭 리스너
        holder.itemView.setOnClickListener(v -> listener.onFriendClick(friend));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView favoriteIcon;

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvName);
            favoriteIcon = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
