package com.example.starlight;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starlight.adapter.MessageAdapter;
import com.example.starlight.model.CommunityMessage;
import com.example.starlight.utils.CommunityHelper;
import com.example.starlight.utils.SessionManager;

import java.util.Collections;
import java.util.List;

public class CommunityFragment extends Fragment {

    // ─── Views ───────────────────────────────────────────────────────────────
    private RecyclerView rvMessages;
    private EditText     etMessage;
    private View         btnSend;
    private TextView     tvRoomName;
    private TextView     tvMemberCount;
    private TextView     tvInputAvatar;

    // ─── State ───────────────────────────────────────────────────────────────
    private MessageAdapter adapter;
    private SessionManager session;
    private String         currentRoom = "movies";

    private final Handler  refreshHandler  = new Handler(Looper.getMainLooper());
    private       Runnable refreshRunnable;

    // ─── Room data ───────────────────────────────────────────────────────────
    private static final String[] ROOM_KEYS = {
            "movies", "series", "drama", "kdrama", "cdrama", "anime", "cartoons"
    };
    private static final String[] ROOM_NAMES = {
            "\uD83C\uDFAC  Movies Room",
            "\uD83D\uDCFA  Series Room",
            "\uD83C\uDFAD  Drama Room",
            "\uD83C\uDDF0\uD83C\uDDF7  K-Drama Room",
            "\uD83C\uDDE8\uD83C\uDDF3  C-Drama Room",
            "\u26E9  Anime Room",
            "\uD83C\uDFA8  Cartoons Room"
    };

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());

        rvMessages    = view.findViewById(R.id.rvMessages);
        etMessage     = view.findViewById(R.id.etMessage);
        btnSend       = view.findViewById(R.id.btnSend);
        tvRoomName    = view.findViewById(R.id.tvRoomName);
        tvMemberCount = view.findViewById(R.id.tvMemberCount);
        tvInputAvatar = view.findViewById(R.id.tvInputAvatar);

        // Avatar initial letter
        String userName = session.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvInputAvatar.setText(String.valueOf(userName.charAt(0)).toUpperCase());
        } else {
            tvInputAvatar.setText("?");
        }

        // RecyclerView — newest messages stack at bottom
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        adapter = new MessageAdapter(requireContext(), session.getUserEmail());
        rvMessages.setAdapter(adapter);

        // Room tabs
        setupRoomButtons(view);

        // Send
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Load default room
        switchRoom(0);

        // Auto-refresh every 8 seconds
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    loadMessages();
                    refreshHandler.postDelayed(this, 8000);
                }
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 8000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    // ─── Room buttons ────────────────────────────────────────────────────────

    private void setupRoomButtons(View view) {
        int[] btnIds = {
                R.id.btnRoomMovies, R.id.btnRoomSeries,  R.id.btnRoomDrama,
                R.id.btnRoomKdrama, R.id.btnRoomCdrama,  R.id.btnRoomAnime,
                R.id.btnRoomCartoons
        };
        for (int i = 0; i < btnIds.length; i++) {
            final int idx = i;
            Button btn = view.findViewById(btnIds[i]);
            if (btn != null) btn.setOnClickListener(v -> switchRoom(idx));
        }
    }

    private void switchRoom(int idx) {
        currentRoom = ROOM_KEYS[idx];
        if (tvRoomName != null) tvRoomName.setText(ROOM_NAMES[idx]);
        adapter.setMessages(Collections.emptyList());
        loadMessages();
    }

    // ─── Load ────────────────────────────────────────────────────────────────

    private void loadMessages() {
        CommunityHelper.getMessages(currentRoom,
                new CommunityHelper.MessagesCallback() {
                    @Override
                    public void onSuccess(List<CommunityMessage> messages) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapter.setMessages(messages);
                            if (tvMemberCount != null)
                                tvMemberCount.setText(messages.size() + " messages");
                            if (!messages.isEmpty())
                                rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Silently ignore background refresh errors
                    }
                });
    }

    // ─── Send ────────────────────────────────────────────────────────────────

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(),
                    "Please login to chat", Toast.LENGTH_SHORT).show();
            return;
        }

        String name  = session.getUserName();
        String email = session.getUserEmail();
        if (name == null || name.isEmpty()) name = "Anonymous";

        CommunityMessage msg = new CommunityMessage(currentRoom, name, email, text);

        btnSend.setEnabled(false);
        etMessage.setText("");

        // Optimistic local add
        adapter.addMessage(msg);
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);

        final CommunityMessage outgoing = msg;
        CommunityHelper.postMessage(outgoing, new CommunityHelper.PostCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    loadMessages(); // reload to get server timestamp
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Failed to send. Check connection.",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}