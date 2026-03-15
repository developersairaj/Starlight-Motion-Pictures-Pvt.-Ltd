package com.example.starlight.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.starlight.R;
import com.example.starlight.model.CommunityMessage;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private final Context context;
    private final String currentUserEmail;
    private List<CommunityMessage> messages = new ArrayList<>();

    // Gold colors per letter for visual variety
    private final int[] avatarColors = {
            0xFFFFB700, 0xFF00F5FF, 0xFFAA80FF,
            0xFF00FFB0, 0xFFFF2D78, 0xFFF5A623
    };

    public MessageAdapter(Context ctx, String currentUserEmail) {
        this.context = ctx;
        this.currentUserEmail = currentUserEmail;
    }

    public void setMessages(List<CommunityMessage> list) {
        // Reverse so newest is at bottom
        messages = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--)
            messages.add(list.get(i));
        notifyDataSetChanged();
    }

    public void addMessage(CommunityMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CommunityMessage msg = messages.get(pos);
        h.tvAvatar.setText(msg.getAvatarLetter());
        h.tvName.setText(msg.getUserName());
        h.tvTime.setText(msg.getFormattedTime());
        h.tvText.setText(msg.getMessage());

        // Color avatar by first letter
        int colorIdx = Math.abs(msg.getUserName().hashCode()) % avatarColors.length;
        h.tvAvatar.setTextColor(avatarColors[colorIdx]);

        // Highlight own messages
        boolean isOwn = msg.getUserEmail() != null
                && msg.getUserEmail().equals(currentUserEmail);
        h.tvName.setTextColor(isOwn ? 0xFFFFB700 : 0xFF00F5FF);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvTime, tvText;
        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvMsgAvatar);
            tvName   = v.findViewById(R.id.tvMsgName);
            tvTime   = v.findViewById(R.id.tvMsgTime);
            tvText   = v.findViewById(R.id.tvMsgText);
        }
    }
}