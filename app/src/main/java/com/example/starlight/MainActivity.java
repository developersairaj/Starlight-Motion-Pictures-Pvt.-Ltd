package com.example.starlight;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME      = "tag_home";
    private static final String TAG_ANIME     = "tag_anime";
    private static final String TAG_COMMUNITY = "tag_community";
    private static final String TAG_CINEMA    = "tag_cinema";
    private static final String TAG_PROFILE   = "tag_profile";

    private BottomNavigationView bottomNav;
    private String activeTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState != null) {
            // Restore which tab was active
            activeTag = savedInstanceState.getString("activeTag", TAG_HOME);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)      showFragment(TAG_HOME);
            else if (id == R.id.nav_anime)     showFragment(TAG_ANIME);
            else if (id == R.id.nav_community) showFragment(TAG_COMMUNITY);
            else if (id == R.id.nav_cinema)    showFragment(TAG_CINEMA);
            else if (id == R.id.nav_profile)   showFragment(TAG_PROFILE);
            return true;
        });

        if (savedInstanceState == null) {
            // Very first launch — just show home
            showFragment(TAG_HOME);
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("activeTag", activeTag);
    }

    private void showFragment(String tag) {
        activeTag = tag;
        FragmentManager fm = getSupportFragmentManager();

        // Check if this fragment already exists
        Fragment target = fm.findFragmentByTag(tag);

        FragmentTransaction ft = fm.beginTransaction();

        // Hide all existing fragments
        for (Fragment f : fm.getFragments()) {
            if (f != null && f.isAdded()) {
                ft.hide(f);
            }
        }

        if (target == null) {
            // Create and add for the first time
            target = createFragment(tag);
            ft.add(R.id.fragmentContainer, target, tag);
        } else {
            // Already exists — just show it
            ft.show(target);
        }

        ft.commitAllowingStateLoss();
    }

    private Fragment createFragment(String tag) {
        switch (tag) {
            case TAG_ANIME:     return new AnimeFragment();
            case TAG_COMMUNITY: return new CommunityFragment();
            case TAG_CINEMA:    return new CinemaFragment();
            case TAG_PROFILE:   return new ProfileFragment();
            default:            return new MainFragment();
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomNav.getSelectedItemId() != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}