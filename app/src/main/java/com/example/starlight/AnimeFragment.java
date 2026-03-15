package com.example.starlight;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.starlight.adapter.AnimeAdapter;
import com.example.starlight.model.AnimeItem;
import com.example.starlight.utils.ImageSearchHelper;
import com.example.starlight.viewmodel.AnimeViewModel;

import java.util.List;

public class AnimeFragment extends Fragment {

    private static final int PICK_IMAGE         = 102;
    private static final int REQUEST_PERMISSION  = 202;

    private View               rootView;
    private AnimeViewModel     vm;
    private AnimeAdapter       adapter;

    // ── Views — matching fragment_anime.xml IDs exactly ───────────────────────
    private RecyclerView       rvAnime;
    private SwipeRefreshLayout swipeRefreshAnime;
    private LinearLayout       layoutAnimeEmpty;
    private EditText           etAnimeSearch;
    private ImageButton        btnAnimeClear;
    private ImageButton        btnAnimeImageSearch;
    private Button             btnAnimeTab1, btnAnimeTab2,
            btnAnimeTab3, btnAnimeTab4;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int     activeSubTab = AnimeViewModel.SUB_TOP_ANIME;
    private boolean isMangaTab   = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_anime, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
        setupAdapter();
        setupViewModel();
        setupTabs();
        setupSearch();
        setupImageSearch();
        if (swipeRefreshAnime != null)
            swipeRefreshAnime.setOnRefreshListener(() -> vm.loadAllAnimeData());
        vm.loadAllAnimeData();
    }

    private void bindViews() {
        rvAnime             = rootView.findViewById(R.id.rvAnime);
        swipeRefreshAnime   = rootView.findViewById(R.id.swipeRefreshAnime);
        layoutAnimeEmpty    = rootView.findViewById(R.id.layoutAnimeEmpty);
        etAnimeSearch       = rootView.findViewById(R.id.etAnimeSearch);
        btnAnimeClear       = rootView.findViewById(R.id.btnAnimeClear);
        btnAnimeImageSearch = rootView.findViewById(R.id.btnAnimeImageSearch);
        btnAnimeTab1        = rootView.findViewById(R.id.btnAnimeTab1);
        btnAnimeTab2        = rootView.findViewById(R.id.btnAnimeTab2);
        btnAnimeTab3        = rootView.findViewById(R.id.btnAnimeTab3);
        btnAnimeTab4        = rootView.findViewById(R.id.btnAnimeTab4);
    }

    // ── Image Search ──────────────────────────────────────────────────────────

    private void setupImageSearch() {
        if (btnAnimeImageSearch == null) return;
        btnAnimeImageSearch.setOnClickListener(v -> openGalleryWithPermission());
    }

    private void openGalleryWithPermission() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), perm)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(new String[]{perm}, REQUEST_PERMISSION);
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(getContext(),
                    "Storage permission needed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            processImage(data.getData());
        }
    }

    private void processImage(Uri uri) {
        if (swipeRefreshAnime != null) swipeRefreshAnime.setRefreshing(true);
        showToast("Analyzing image...");

        new Thread(() -> {
            try {
                Bitmap bmp = MediaStore.Images.Media
                        .getBitmap(requireActivity().getContentResolver(), uri);
                String detected = ImageSearchHelper.detectFromImage(bmp);

                handler.post(() -> {
                    if (swipeRefreshAnime != null) swipeRefreshAnime.setRefreshing(false);
                    if (detected != null && !detected.isEmpty()) {
                        if (etAnimeSearch != null) etAnimeSearch.setText(detected);
                        if (btnAnimeClear != null)
                            btnAnimeClear.setVisibility(View.VISIBLE);
                        if (isMangaTab) vm.searchManga(detected);
                        else            vm.searchAnime(detected);
                        showToast("Found: " + detected);
                    } else {
                        String err = ImageSearchHelper.lastError;
                        if (err != null && !err.isEmpty()) {
                            showToast("API Error: " + err);
                        } else {
                            showToast("Could not identify the media in this image.");
                        }
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (swipeRefreshAnime != null) swipeRefreshAnime.setRefreshing(false);
                    showToast("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showToast(String msg) {
        if (getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private void setupAdapter() {
        adapter = new AnimeAdapter(requireContext());
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), AnimeDetailActivity.class);
            intent.putExtra("anime_id",    item.getMalId());
            intent.putExtra("anime_title", item.getDisplayTitle());
            intent.putExtra("is_manga",    isMangaTab);
            startActivity(intent);
        });
        if (rvAnime != null) {
            rvAnime.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            rvAnime.setAdapter(adapter);
        }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private void setupTabs() {
        if (btnAnimeTab1 != null) btnAnimeTab1.setOnClickListener(v ->
                switchTab(AnimeViewModel.SUB_TOP_ANIME, false));
        if (btnAnimeTab2 != null) btnAnimeTab2.setOnClickListener(v ->
                switchTab(AnimeViewModel.SUB_AIRING, false));
        if (btnAnimeTab3 != null) btnAnimeTab3.setOnClickListener(v ->
                switchTab(AnimeViewModel.SUB_UPCOMING, false));
        if (btnAnimeTab4 != null) btnAnimeTab4.setOnClickListener(v ->
                switchTab(AnimeViewModel.SUB_TOP_MANGA, true));
        highlightTab(activeSubTab);
    }

    private void switchTab(int tab, boolean manga) {
        activeSubTab = tab;
        isMangaTab   = manga;
        highlightTab(tab);
        if (etAnimeSearch != null) etAnimeSearch.setText("");
        showListForTab(tab);
    }

    private void highlightTab(int idx) {
        Button[] tabs = {btnAnimeTab1, btnAnimeTab2, btnAnimeTab3, btnAnimeTab4};
        for (int i = 0; i < tabs.length; i++)
            if (tabs[i] != null) tabs[i].setSelected(i == idx);
    }

    private void showListForTab(int tab) {
        List<AnimeItem> current;
        switch (tab) {
            case AnimeViewModel.SUB_AIRING:
                current = vm.getAiringList().getValue(); break;
            case AnimeViewModel.SUB_UPCOMING:
                current = vm.getUpcomingList().getValue(); break;
            case AnimeViewModel.SUB_TOP_MANGA:
                current = vm.getTopMangaList().getValue(); break;
            default:
                current = vm.getTopAnimeList().getValue(); break;
        }
        if (current != null && !current.isEmpty()) updateList(current);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        if (etAnimeSearch == null) return;
        etAnimeSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (btnAnimeClear != null)
                    btnAnimeClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                String q = s.toString().trim();
                if (q.length() >= 2) {
                    if (isMangaTab) vm.searchManga(q);
                    else            vm.searchAnime(q);
                } else if (q.isEmpty()) {
                    showListForTab(activeSubTab);
                }
            }
        });
        if (btnAnimeClear != null) {
            btnAnimeClear.setOnClickListener(v -> {
                etAnimeSearch.setText("");
                showListForTab(activeSubTab);
            });
        }
    }

    // ── ViewModel ─────────────────────────────────────────────────────────────

    private void setupViewModel() {
        vm = new ViewModelProvider(this).get(AnimeViewModel.class);

        vm.getTopAnimeList().observe(getViewLifecycleOwner(), items -> {
            if (swipeRefreshAnime != null) swipeRefreshAnime.setRefreshing(false);
            if (activeSubTab == AnimeViewModel.SUB_TOP_ANIME && !isMangaTab)
                updateList(items);
        });
        vm.getAiringList().observe(getViewLifecycleOwner(), items -> {
            if (activeSubTab == AnimeViewModel.SUB_AIRING && !isMangaTab)
                updateList(items);
        });
        vm.getUpcomingList().observe(getViewLifecycleOwner(), items -> {
            if (activeSubTab == AnimeViewModel.SUB_UPCOMING && !isMangaTab)
                updateList(items);
        });
        vm.getTopMangaList().observe(getViewLifecycleOwner(), items -> {
            if (activeSubTab == AnimeViewModel.SUB_TOP_MANGA && isMangaTab)
                updateList(items);
        });
        vm.getSearchAnimeList().observe(getViewLifecycleOwner(), items -> {
            if (!isMangaTab) updateList(items);
        });
        vm.getSearchMangaList().observe(getViewLifecycleOwner(), items -> {
            if (isMangaTab) updateList(items);
        });
        vm.getErrorMsg().observe(getViewLifecycleOwner(), err -> {
            if (err == null || err.isEmpty()) return;
            if (swipeRefreshAnime != null) swipeRefreshAnime.setRefreshing(false);
            Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
        vm.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (swipeRefreshAnime != null && loading != null)
                swipeRefreshAnime.setRefreshing(loading);
        });
    }

    private void updateList(List<AnimeItem> items) {
        if (items != null && !items.isEmpty()) {
            adapter.submitList(items);
            if (layoutAnimeEmpty != null) layoutAnimeEmpty.setVisibility(View.GONE);
        } else {
            if (layoutAnimeEmpty != null) layoutAnimeEmpty.setVisibility(View.VISIBLE);
        }
    }
}