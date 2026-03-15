package com.example.starlight;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.starlight.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        String name  = session.getUserName();
        String email = session.getUserEmail();

        // Avatar letter
        TextView tvAvatar = view.findViewById(R.id.tvAvatarLetter);
        if (name != null && !name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Profile details
        ((TextView) view.findViewById(R.id.tvProfileName)).setText(name);
        ((TextView) view.findViewById(R.id.tvProfileEmail)).setText(email);
        ((TextView) view.findViewById(R.id.tvInfoName)).setText(name);
        ((TextView) view.findViewById(R.id.tvInfoEmail)).setText(email);

        // Member since today's date
        String date = new SimpleDateFormat("MMM yyyy", Locale.getDefault())
                .format(new Date());
        ((TextView) view.findViewById(R.id.tvMemberSince)).setText("Since " + date);

        // Logout
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(requireContext(), SignupActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}