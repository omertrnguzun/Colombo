package com.riccardobusetti.colombo.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.data.CardData;
import com.riccardobusetti.colombo.database.DBAdapter;
import com.riccardobusetti.colombo.holder.MyHolder;
import com.riccardobusetti.colombo.util.AppStatus;
import com.riccardobusetti.colombo.util.ItemClickListener;
import com.riccardobusetti.colombo.util.ItemLongClickListener;
import com.riccardobusetti.colombo.util.RecentSuggestionsProvider;
import com.riccardobusetti.colombo.util.StaticUtils;
import com.riccardobusetti.colombo.view.CustomWebChromeClient;
import com.riccardobusetti.colombo.view.ObservableWebView;

import java.util.ArrayList;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_FILE = 100;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private static final int STORAGE_PERMISSION_CODE = 5678;
    private static final int SEARCH_GOOGLE = 0, SEARCH_YAHOO = 1, SEARCH_DUCKDUCKGO = 2, SEARCH_BING = 3;

    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> uploadMessagePreLollipop;

    private boolean isIncognito;
    private boolean desktop = true;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private SharedPreferences prefs;

    private RecyclerView rv;
    private MyAdapter adapter;
    private ArrayList<CardData> cardDatas = new ArrayList<>();

    private AppBarLayout appbar;
    private CoordinatorLayout coordinatorLayout;
    private SearchView searchView;
    private SearchRecentSuggestions suggestions;
    private ObservableWebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private TextView title, appTitle;
    private FrameLayout titleFrame;
    private String urlIntent = null;
    private CardView cardSearch;
    private BottomSheetLayout bottomSheet;
    private LinearLayout share, rename, delete;
    private LayoutInflater inflater;
    private View view;

    private static void setOverflowButtonColor(final Toolbar toolbar, final int color) {
        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
            toolbar.setOverflowIcon(drawable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Hardware acceleration */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        /** Initialization of prefs */
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        /** UI stuff */
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        urlIntent = getIntent().getDataString();
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title = (TextView) findViewById(R.id.toolbar_title);
        appTitle = (TextView) findViewById(R.id.app_title);
        titleFrame = (FrameLayout) findViewById(R.id.big_title);
        cardSearch = (CardView) findViewById(R.id.card_search);
        View search = findViewById(R.id.search);
        setOverflowButtonColor(toolbar, Color.parseColor("#696969"));

        /** BottomSheet initialization */
        bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        inflater = MainActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.bookmark_options_dialog, null);
        share = (LinearLayout) view.findViewById(R.id.share_bookmark);
        rename = (LinearLayout) view.findViewById(R.id.rename_bookmark);
        delete = (LinearLayout) view.findViewById(R.id.delete_bookmark);

        /** Checking internet connection */
        if (AppStatus.getInstance(this).isOnline()) {

        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        /** Webview first stuff */
        webView = (ObservableWebView) findViewById(R.id.webview);
        webView.setVisibility(View.GONE);

        /** Location */
        handleLocation();

        /** Recyclerviewer stuff */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        rv = (RecyclerView) findViewById(R.id.recyclerViewer);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(gridLayoutManager);
        adapter = new MyAdapter(this, cardDatas);
        rv.setAdapter(adapter);
        retrieve();

        /** Load WebView url */
        webView.loadUrl(getHomepage());
        if (urlIntent != null) {
            webView.loadUrl(urlIntent);
            if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                webView.setVisibility(View.VISIBLE);
                titleFrame.setVisibility(View.GONE);
            }
        }

        /** Add toolbar clicklistener */
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
            }
        });

        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Add Bookmark?")
                        .content("Give to your bookmark a name!")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Bookmark name", webView.getTitle(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                save(input.toString(), webView.getUrl());
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });

        /** Set searchbar animation */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (search != null && search.getVisibility() == View.VISIBLE) {
                search.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_fab_in));
            }
        }

        /** Initiate UI colors */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (prefs.getBoolean("light_icons", true)) {
                if (coordinatorLayout != null) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                appTitle.setTextColor(Color.parseColor("#233B3F"));
            } else {
                setTheme(R.style.WhiteIconsTheme);
                appTitle.setTextColor(Color.parseColor("#FAFAFA"));
            }
        } else {
            setTheme(R.style.WhiteIconsTheme);
            appTitle.setTextColor(Color.parseColor("#FAFAFA"));
        }

        /** Settings Button on Toolbar */
        ImageView settings = (ImageView) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        /** Set Webview params */
        //webView.setNavigationViews(findViewById(R.id.previous), findViewById(R.id.next));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(prefs.getBoolean("javascript", true));
        webSettings.setGeolocationEnabled(prefs.getBoolean("location_services", true));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setSaveFormData(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(prefs.getBoolean("zooming", false));
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.supportZoom();
        webView.requestFocus();
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
                final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);

                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Download " + filename1 + "?", Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.parseColor("#1DE9B6"));
                snackbar.setAction("DOWNLOAD", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            } else {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                                String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);

                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dm.enqueue(request);

                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");

                                Toast.makeText(MainActivity.this, "Downloading: " + filename, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                            String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);

                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            dm.enqueue(request);

                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");

                            Toast.makeText(MainActivity.this, "Downloading: " + filename, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                snackbar.show();
            }
        });
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(urlIntent);
            }
        });
        CustomWebChromeClient webChromeClient = new CustomWebChromeClient(this) {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                if (prefs.getBoolean("dynamic_colors", true)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (prefs.getBoolean("light_icons", true)) {
                            Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    setColor(palette.getLightMutedColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                                }
                            });
                        } else {
                            Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                                }
                            });
                        }
                    } else {
                        Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                            }
                        });
                    }
                }
            }
        };

        webView.setWebChromeClient(webChromeClient);

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                webView.setCanScrollVertically((appBarLayout.getHeight() - appBarLayout.getBottom()) != 0);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.swipeRefresh));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessagePreLollipop == null)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            uploadMessagePreLollipop.onReceiveValue(result);
            uploadMessagePreLollipop = null;
        } else
            Snackbar.make(coordinatorLayout, R.string.msg_upload_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else if (searchView.getVisibility() == View.VISIBLE) {
            searchView.setVisibility(View.GONE);
            searchView.setIconified(false);
        } else if (webView.canGoBack() && searchView.getVisibility() == View.VISIBLE) {
            searchView.setVisibility(View.GONE);
            searchView.setIconified(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint("");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        suggestions = new SearchRecentSuggestions(MainActivity.this, RecentSuggestionsProvider.AUTHORITY, RecentSuggestionsProvider.MODE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (webView.getVisibility() == View.GONE) {
                    webView.setVisibility(View.VISIBLE);
                }

                if (titleFrame.getVisibility() == View.VISIBLE) {
                    titleFrame.setVisibility(View.GONE);
                }

                if (query.startsWith("www") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".com") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".gov") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".net") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".org") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".ly") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".gl") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);

                } else
                    webView.loadUrl(getSearchPrefix() + query);

                if (!isIncognito) suggestions.saveRecentQuery(query, null);

                searchView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                String query = cursor.getString(2);

                searchView.setQuery(query, false);

                if (webView.getVisibility() == View.GONE) {
                    webView.setVisibility(View.VISIBLE);
                }

                if (titleFrame.getVisibility() == View.VISIBLE) {
                    titleFrame.setVisibility(View.GONE);
                }

                if (URLUtil.isValidUrl(query))
                    webView.loadUrl(query);
                else
                    webView.loadUrl(getSearchPrefix() + query);

                searchView.setIconified(false);
                searchView.setVisibility(View.GONE);

                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.VISIBLE);
                    titleFrame.setVisibility(View.GONE);
                } else if (searchView.getVisibility() == View.GONE) {
                    searchView.setVisibility(View.VISIBLE);
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setIconified(false);
                searchView.setVisibility(View.GONE);
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (prefs.getBoolean("suggestions", true))
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        searchView.setVisibility(View.GONE);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                webView.clearHistory();
                webView.loadUrl(getHomepage());
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.VISIBLE);
                    titleFrame.setVisibility(View.GONE);
                }
                break;
            case R.id.action_share:
                String shareBody = webView.getUrl();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Website Link");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share link"));
                break;
            case R.id.action_refresh:
                webView.reload();
                break;
            case R.id.action_bookmark:
                if (webView.getVisibility() == View.VISIBLE && titleFrame.getVisibility() == View.GONE) {
                    webView.setVisibility(View.GONE);
                    titleFrame.setVisibility(View.VISIBLE);
                } else if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.VISIBLE);
                    titleFrame.setVisibility(View.GONE);
                }
                break;
            case R.id.action_search_words:
                webView.showFindDialog(null, true);
                break;
            case R.id.action_dekstop:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                if (desktop) {
                    webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.41 Safari/537.36");
                    webView.reload();
                    desktop = false;
                } else {
                    webView.getSettings().setUserAgentString("");
                    webView.reload();
                    desktop = true;
                }
                break;
            case R.id.action_incognito:
                isIncognito = !isIncognito;
                item.setChecked(isIncognito);

                WebSettings webSettings = webView.getSettings();
                CookieManager.getInstance().setAcceptCookie(!isIncognito);
                webSettings.setAppCacheEnabled(!isIncognito);
                webView.clearHistory();
                webView.clearCache(isIncognito);
                webView.clearFormData();
                webView.getSettings().setSavePassword(!isIncognito);
                webView.getSettings().setSaveFormData(!isIncognito);
                webView.isPrivateBrowsingEnabled();

                if (!isIncognito) {
                    //When exit from incognito
                    appTitle.setText(R.string.app_name);
                    cardSearch.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
                    title.setTextColor(Color.parseColor("#696969"));
                    setOverflowButtonColor(toolbar, Color.parseColor("#696969"));
                } else {
                    //When enter in incognito
                    appTitle.setText(R.string.app_name_incognito);
                    cardSearch.setCardBackgroundColor(Color.parseColor("#233B3F"));
                    title.setTextColor(Color.parseColor("#FAFAFA"));
                    setOverflowButtonColor(toolbar, Color.parseColor("#FAFAFA"));
                }
                break;
            case R.id.action_add:
                createShortCut();
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else if (locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);

        setPrefs();

        if (AppStatus.getInstance(this).isOnline()) {

        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setPrefs();
    }

    private void update(int id,String newName)
    {
        DBAdapter db=new DBAdapter(this);
        db.openDB();
        long result=db.UPDATE(id,newName);
        if(result>0)
        {
            Snackbar.make(coordinatorLayout,"Bookmark updated successfully!",Snackbar.LENGTH_SHORT).show();

        }else
        {
            Snackbar.make(coordinatorLayout,"Unable to update the bookmark :_(",Snackbar.LENGTH_SHORT).show();
        }
        db.closeDB();
    }

    /**
     * Delete data from DB
     */
    private void delete(int id) {
        DBAdapter db = new DBAdapter(this);
        db.openDB();

        long result = db.Delete(id);
        if (result > 0) {
            Snackbar.make(coordinatorLayout, "Bookmark deleted", Snackbar.LENGTH_SHORT).show();
            retrieve();
        } else {
            Snackbar.make(coordinatorLayout, "Unable to Delete", Snackbar.LENGTH_SHORT).show();
        }

        db.closeDB();
    }

    /**
     * Get data from DB
     */
    private void retrieve() {

        DBAdapter db = new DBAdapter(this);
        db.openDB();

        cardDatas.clear();

        //Prendere dati
        Cursor c = db.getAllData();

        //Guardare nei dati e aggiungere ad ArrayList
        while (c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String code = c.getString(2);

            CardData cardData = new CardData(id, name, code);

            //Aggiungere ad arraylist
            cardDatas.add(cardData);
        }

        //Controllo se ArrayList non è vuota
        if (!(cardDatas.size() < 1)) {
            rv.setAdapter(adapter);
        }

        db.closeDB();

    }

    /**
     * Save data in DB
     */
    private void save(String name, String code) {

        DBAdapter db = new DBAdapter(this);

        //Aprire DataBase
        db.openDB();

        //Dichiarare cambiamenti
        long result = db.add(name, code);

        if (result > 0) {

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Bookmark added successfully!", Snackbar.LENGTH_SHORT);
            snackbar.show();

        } else {

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Impossible to save Bookmark :_(", Snackbar.LENGTH_SHORT);
            snackbar.show();

        }

        db.closeDB();

        retrieve();
    }

    /**
     * Handle GPS
     */
    private void handleLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            }
        } else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
        }
    }

    /**
     * Method to get homepage from prefs
     */
    private String getHomepage() {
        String homepage = prefs.getString("homepage", "");
        if (homepage.length() > 0) {
            return homepage;
        } else {
            switch (Integer.parseInt(prefs.getString("search_engine", "0"))) {
                case SEARCH_GOOGLE:
                    return "https://google.com";
                case SEARCH_YAHOO:
                    return "https://www.yahoo.com/";
                case SEARCH_DUCKDUCKGO:
                    return "https://duckduckgo.com/";
                case SEARCH_BING:
                    return "https://www.bing.com/";
                default:
                    return "https://www.google.com/";
            }
        }
    }

    /**
     * Method to get prefix for search engine
     */
    private String getSearchPrefix() {
        switch (Integer.parseInt(prefs.getString("search_engine", "0"))) {
            case SEARCH_GOOGLE:
                return "https://www.google.com/search?q=";
            case SEARCH_YAHOO:
                return "https://search.yahoo.com/search?p=";
            case SEARCH_DUCKDUCKGO:
                return "https://duckduckgo.com/?q=";
            case SEARCH_BING:
                return "https://www.bing.com/search?q=";
            default:
                return "https://www.google.com/search?q=";
        }
    }

    /**
     * Set color class to change dinamically color of UI
     */
    private void setColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), StaticUtils.darkColor(color));
            colorAnimation.setDuration(150);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor((int) animator.getAnimatedValue());
                        if (prefs.getBoolean("navbar_tint", false))
                            getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
                    }
                }
            });
            colorAnimation.start();

            setTaskDescription(new ActivityManager.TaskDescription(webView.getTitle(), webView.getFavicon(), color));
        }

        int colorFrom = ContextCompat.getColor(this, R.color.colorPrimary);
        Drawable backgroundFrom = appbar.getBackground();
        if (backgroundFrom instanceof ColorDrawable)
            colorFrom = ((ColorDrawable) backgroundFrom).getColor();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(150);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                appbar.setBackgroundColor((int) animator.getAnimatedValue());
                swipeRefreshLayout.setColorSchemeColors((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, isIncognito ? R.color.swipeRefreshIncognito : R.color.swipeRefresh));
    }

    /**
     * Method to set stuff when returning from settings
     */
    private void setPrefs() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(prefs.getBoolean("javascript", true));
        webSettings.setGeolocationEnabled(prefs.getBoolean("location_services", true));
        webSettings.setSupportZoom(prefs.getBoolean("zooming", false));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (prefs.getBoolean("light_icons", true)) {
                if (coordinatorLayout != null) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                appTitle.setTextColor(Color.parseColor("#233B3F"));
            } else {
                setTheme(R.style.WhiteIconsTheme);
                appTitle.setTextColor(Color.parseColor("#FAFAFA"));
            }
        } else {
            setTheme(R.style.WhiteIconsTheme);
            appTitle.setTextColor(Color.parseColor("#FAFAFA"));
        }
    }

    /**
     * Method to create shortcuts to home
     */
    private void createShortCut() {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Intent data = new Intent(getApplicationContext(), MainActivity.class);
        data.setData(Uri.parse(webView.getUrl()));
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, webView.getTitle());
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, data);
        sendBroadcast(shortcutintent);
        Toast.makeText(this, "Shortcut added to your home!", Toast.LENGTH_SHORT).show();
    }

    public class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (Uri.parse(url).getHost().equals(url)) {
                webView.loadUrl(url);
                return true;
            } else if (urlIntent != null) {
                webView.loadUrl(urlIntent);
            }
            if (url.startsWith("market://") || url.startsWith("https://m.youtube.com")
                    || url.startsWith("https://play.google.com") || url.startsWith("magnet:")
                    || url.startsWith("mailto:") || url.startsWith("intent://")
                    || url.startsWith("https://mail.google.com") || url.startsWith("https://plus.google.com")) {

                new BottomDialog.Builder(MainActivity.this)
                        .setTitle("You are leaving Colombo!")
                        .setContent("Are you sure to open this link in the specific app?")
                        .setPositiveText("OPEN")
                        .setNegativeText("CONTINUE IN COLOMBO")
                        .onNegative(new BottomDialog.ButtonCallback() {
                            @Override
                            public void onClick(@NonNull BottomDialog bottomDialog) {
                                bottomDialog.dismiss();
                                webView.loadUrl(url);
                            }
                        })
                        .setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_splash))
                        .setCancelable(true)
                        .onPositive(new BottomDialog.ButtonCallback() {
                            @Override
                            public void onClick(BottomDialog dialog) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            }
                        }).show();
                return true;
            }
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap facIcon) {
            swipeRefreshLayout.setRefreshing(true);

            if (!isIncognito && suggestions != null) suggestions.saveRecentQuery(url, null);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);

            title.setText(webView.getTitle());
        }
    }

    /**
     * Adapter recyclerviewer
     */
    public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        Context c;
        ArrayList<CardData> cardData;
        private int lastPosition = -1;

        public MyAdapter(Context c, ArrayList<CardData> cardData) {
            this.c = c;
            this.cardData = cardData;
        }

        //Inzializzazione ViewHolder
        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Creazione View Object
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);

            //Creazione Holder
            MyHolder holder = new MyHolder(v);

            return holder;
        }

        //Inizialiazzione Bind
        @Override
        public void onBindViewHolder(final MyHolder holder, final int position) {

            holder.name.setText(cardData.get(position).getName());

            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    webView.loadUrl(cardData.get(pos).getCode());
                    if (webView.getVisibility() == View.GONE) {
                        webView.setVisibility(View.VISIBLE);
                        titleFrame.setVisibility(View.GONE);
                    }
                }
            });

            holder.setItemLongClickListener(new ItemLongClickListener() {
                @Override
                public void onItemLongClick(View v, final int pos) {
                    /*share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String shareBody = cardData.get(position).getCode();
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, cardData.get(position).getName());
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                            startActivity(Intent.createChooser(sharingIntent, "Share bookmark"));
                            bottomSheet.dismissSheet();
                        }
                    });

                    rename.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title("Rename Bookmark")
                                    .content("Give to this bookmark a new name!")
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input("Bookmark name", cardData.get(position).getName(), new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                            update(cardData.get(position).getId(), input.toString());
                                        }
                                    }).show();
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            delete(cardData.get(position).getId());
                            bottomSheet.dismissSheet();
                        }
                    });*/
                    bottomSheet.showWithSheetView(LayoutInflater.from(MainActivity.this).inflate(R.layout.bookmark_options_dialog, bottomSheet, false));
                }
            });

            setAnimation(holder.itemView, position);

        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setDuration(new Random().nextInt(501));//to make duration random number between [0,501)
                viewToAnimate.startAnimation(anim);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return cardData.size();
        }

    }
}
