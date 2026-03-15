package com.example.starlight;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
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

import com.example.starlight.adapter.MediaAdapter;
import com.example.starlight.model.MediaItem;
import com.example.starlight.utils.Constants;
import com.example.starlight.utils.ImageSearchHelper;
import com.example.starlight.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final int PICK_IMAGE        = 101;
    private static final int REQUEST_PERMISSION = 201;

    private RecyclerView       rvMedia;
    private SwipeRefreshLayout swipeRefresh;
    private EditText           etSearch;
    private ImageButton        btnClearSearch;
    private ImageButton        btnImageSearch;
    private View               layoutEmpty;

    private Button btnTab1, btnTab2, btnTab3, btnTab4,
            btnTab5, btnTab6, btnTab7;

    private MainViewModel   viewModel;
    private MediaAdapter    adapter;
    private int             selectedTab = MainViewModel.TAB_TRENDING;
    private List<MediaItem> allItems    = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupImageSearch();
        setupSwipeRefresh();
        setupViewModel();
        viewModel.loadTab(MainViewModel.TAB_TRENDING);
    }

    private void findViews(View view) {
        rvMedia        = view.findViewById(R.id.rvMedia);
        swipeRefresh   = view.findViewById(R.id.swipeRefresh);
        etSearch       = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnImageSearch = view.findViewById(R.id.btnImageSearch);
        layoutEmpty    = view.findViewById(R.id.layoutEmpty);
        btnTab1 = view.findViewById(R.id.btnTab1);
        btnTab2 = view.findViewById(R.id.btnTab2);
        btnTab3 = view.findViewById(R.id.btnTab3);
        btnTab4 = view.findViewById(R.id.btnTab4);
        btnTab5 = view.findViewById(R.id.btnTab5);
        btnTab6 = view.findViewById(R.id.btnTab6);
        btnTab7 = view.findViewById(R.id.btnTab7);
    }

    // ── Image Search ──────────────────────────────────────────────────────────

    private void setupImageSearch() {
        if (btnImageSearch == null) return;
        btnImageSearch.setOnClickListener(v -> openGalleryWithPermission());
    }

    private void openGalleryWithPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(new String[]{permission}, REQUEST_PERMISSION);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
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
                    "Storage permission needed to pick images",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE
                && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            processImage(data.getData());
        }
    }

    private void processImage(Uri uri) {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        showToast("Analyzing image...");

        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(requireActivity().getContentResolver(), uri);

                String detected = ImageSearchHelper.detectFromImage(bitmap);

                handler.post(() -> {
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                    if (detected != null && !detected.isEmpty()) {
                        // Success — fill search bar and search
                        etSearch.setText(detected);
                        if (btnClearSearch != null)
                            btnClearSearch.setVisibility(View.VISIBLE);
                        viewModel.search(detected);
                        showToast("Found: " + detected);
                    } else {
                        // Show the actual API error so it's debuggable
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
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    showToast("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showToast(String msg) {
        if (getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new MediaAdapter(requireContext());
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra(Constants.EXTRA_MEDIA_ID,   item.getId());
            intent.putExtra(Constants.EXTRA_MEDIA_TYPE,
                    item.getResolvedType() != null
                            ? item.getResolvedType() : item.getMediaType());
            intent.putExtra(Constants.EXTRA_MEDIA_NAME, item.getDisplayTitle());
            startActivity(intent);
        });
        GridLayoutManager lm = new GridLayoutManager(requireContext(), 2);
        rvMedia.setLayoutManager(lm);
        rvMedia.setAdapter(adapter);
        rvMedia.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View itemView,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                outRect.set(5, 5, 5, 5);
            }
        });
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private void setupTabs() {
        if (btnTab1 != null) btnTab1.setOnClickListener(v -> switchTab(MainViewModel.TAB_TRENDING));
        if (btnTab2 != null) btnTab2.setOnClickListener(v -> switchTab(MainViewModel.TAB_MOVIES));
        if (btnTab3 != null) btnTab3.setOnClickListener(v -> switchTab(MainViewModel.TAB_SERIES));
        if (btnTab4 != null) btnTab4.setOnClickListener(v -> switchTab(MainViewModel.TAB_DRAMA));
        if (btnTab5 != null) btnTab5.setOnClickListener(v -> switchTab(MainViewModel.TAB_KDRAMA));
        if (btnTab6 != null) btnTab6.setOnClickListener(v -> switchTab(MainViewModel.TAB_CDRAMA));
        if (btnTab7 != null) btnTab7.setOnClickListener(v -> switchTab(MainViewModel.TAB_CARTOONS));
        highlightTab(MainViewModel.TAB_TRENDING);
    }

    private void switchTab(int idx) {
        if (idx == selectedTab) return;
        selectedTab = idx;
        highlightTab(idx);
        if (etSearch != null) etSearch.setText("");
        viewModel.loadTab(idx);
    }

    private void highlightTab(int idx) {
        Button[] tabs = {btnTab1, btnTab2, btnTab3, btnTab4,
                btnTab5, btnTab6, btnTab7};
        for (int i = 0; i < tabs.length; i++)
            if (tabs[i] != null) tabs[i].setSelected(i == idx);
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (btnClearSearch != null)
                    btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    adapter.submitList(allItems);
                    showEmpty(allItems.isEmpty());
                } else {
                    viewModel.search(q);
                }
            }
        });
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etSearch.setText("");
                adapter.submitList(allItems);
                showEmpty(allItems.isEmpty());
            });
        }
    }

    // ── Swipe Refresh ────────────────────────────────────────────────────────

    private void setupSwipeRefresh() {
        if (swipeRefresh == null) return;
        swipeRefresh.setColorSchemeColors(0xFFFFB700, 0xFF00F5FF, 0xFFAA80FF);
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF0D0C1E);
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadTab(selectedTab));
    }

    // ── ViewModel ────────────────────────────────────────────────────────────

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getMediaList().observe(getViewLifecycleOwner(), items -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (items != null && !items.isEmpty()) {
                allItems = items;
                adapter.submitList(items);
                showEmpty(false);
            } else {
                String q = etSearch != null ? etSearch.getText().toString().trim() : "";
                if (q.isEmpty()) showEmpty(true);
            }
        });
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            showEmpty(true);
        });
    }

    private void showEmpty(boolean show) {
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}