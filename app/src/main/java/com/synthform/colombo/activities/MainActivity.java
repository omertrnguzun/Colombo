package com.synthform.colombo.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.synthform.colombo.R;
import com.synthform.colombo.data.CardData;
import com.synthform.colombo.database.DBAdapter;
import com.synthform.colombo.database.DBAdapterHistory;
import com.synthform.colombo.holder.MyHolder;
import com.synthform.colombo.search.AnimationUtil;
import com.synthform.colombo.search.MaterialSearchView;
import com.synthform.colombo.util.AdBlocker;
import com.synthform.colombo.util.AppStatus;
import com.synthform.colombo.util.ItemClickListener;
import com.synthform.colombo.util.ItemLongClickListener;
import com.synthform.colombo.util.StaticUtils;
import com.synthform.colombo.view.CustomGestureDetector;
import com.synthform.colombo.view.CustomWebChromeClient;
import com.synthform.colombo.view.ExpandAnimationUtil;
import com.synthform.colombo.view.ObservableWebView;
import com.synthform.colombo.view.SwipeGestureDetector;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity {

    /**
     * Integers
     */
    private static final int REQUEST_SELECT_FILE = 100;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private static final int STORAGE_PERMISSION_CODE = 5678;
    private static final int SEARCH_GOOGLE = 0, SEARCH_YAHOO = 1, SEARCH_DUCKDUCKGO = 2, SEARCH_BING = 3;
    private static final int OPEN_ALWAYS = 0, OPEN_APP = 1, OPEN_LINK = 2;
    private static final String GOOGLE = "https://www.google.com/";
    private static final String YAHOO = "https://www.yahoo.com/";
    private static final String DUCKDUCKGO = "https://www.duckduckgo.com/";
    private static final String BING = "https://www.bing.com/";
    private static final double FRACTION = 0.2;

    /**
     * Array Vars
     */
    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> uploadMessagePreLollipop;
    private ArrayList<CardData> cardDatas = new ArrayList<>();

    /**
     * Boolean Values
     */
    private boolean isIncognito;
    private boolean desktop = true;

    /**
     * Intent vars
     */
    private String urlIntent = null;
    private String shortcutNew;
    private String shortcutIncognito;
    private String shortcutIncognitoComing;

    /**
     * Other Elements
     */
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SharedPreferences prefs;
    private RecyclerView rv;
    private MyAdapter adapter;

    /**
     * UI Elements
     */
    private AppBarLayout appbar;
    private CoordinatorLayout coordinatorLayout;
    private ObservableWebView webView;
    private Toolbar toolbar;
    private TextView title, appTitle;
    private FrameLayout webviewContainer;
    private RelativeLayout titleFrame, progressBarFrame;
    private CardView cardSearch;
    private BottomSheetLayout bottomSheet;
    private View search;
    private ImageView settings;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RelativeLayout backround_bookmark_text;
    private TextView bookmark_text, no_bookmark_text, no_bookmark_text_2;
    private ImageView back, forward, bookmark;
    private Menu menu;
    private ProgressBar progressBar;
    private GridLayoutManager gridLayoutManager;
    private MaterialSearchView materialSearchView;
    private ArrayList<String> lines;
    private WebSettings webSettings;
    private View whiteSearch;
    private RelativeLayout containerNoBookmarks;
    private GestureDetector gestureDetector;
    private boolean zoomed = false, firstRun = true;

    /**
     * Detect if running on tablet screen
     */
    private static Boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
                && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Convert RGB color to hex
     *
     * @param swatch
     * @return
     */
    public static String convertColorToHexadeimal(Palette.Swatch swatch) {
        String hex = Integer.toHexString(swatch.getRgb() & 0xffffff);
        if (hex.length() < 6) {
            if (hex.length() == 5)
                hex = "0" + hex;
            if (hex.length() == 4)
                hex = "00" + hex;
            if (hex.length() == 3)
                hex = "000" + hex;
        }
        hex = "#" + hex;
        return hex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize getting intents and sharedpreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        urlIntent = getIntent().getDataString();
        shortcutNew = getIntent().getStringExtra("shortcut_new");
        shortcutIncognito = getIntent().getStringExtra("shortcut_incognito");
        shortcutIncognitoComing = getIntent().getStringExtra("shortcut_incognito_coming");

        setUpElements();

        if (shortcutIncognitoComing != null && shortcutIncognitoComing.equals("yes")) {
            isIncognito = true;
            applyColors(true);
        } else {
            isIncognito = false;
            applyColors(false);
        }

        setUpSearchView();

        setUpBookmarksStructure();

        detectArraySize();

        setUpUiAnimations();

        setUpClickListeners();

        handleUrlLoading();

        firstTimeSnackBar();

        /** Set Webview params */
        if (prefs.getBoolean("hw_acceleration", true)) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        webSettings = webView.getSettings();

        /** Editable settings */
        webSettings.setJavaScriptEnabled(prefs.getBoolean("javascript", true));
        webSettings.setGeolocationEnabled(prefs.getBoolean("location_services", true));
        webSettings.setBuiltInZoomControls(prefs.getBoolean("zooming", true));

        /** Zoom */
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

        /** Cache Settings */
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        /** Cookie Settings */
        if (prefs.getBoolean("cookies", true)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptCookie(true);
            } else {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            }
            CookieSyncManager.createInstance(MainActivity.this);
            CookieSyncManager.getInstance().startSync();
        }

        /** Database support */
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);

        /** File settings */
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        /** UI Mode */
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        /** Other settings */
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        if (prefs.getBoolean("plugins", true)) {
            webSettings.setPluginState(WebSettings.PluginState.ON);
        }
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);

        webView.setWebViewClient(new WebClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
                final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Download " + filename1 + "?", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("DOWNLOAD", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Build.VERSION.SDK_INT >= M) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            } else {
                                try {
                                    downloadFile(url, contentDisposition, mimeType);
                                } catch (Exception exc) {
                                    Toast.makeText(MainActivity.this, exc.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            try {
                                downloadFile(url, contentDisposition, mimeType);
                            } catch (Exception exc) {
                                Toast.makeText(MainActivity.this, exc.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                snackbar.show();
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
                if (Build.VERSION.SDK_INT >= M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                    } else {
                        callback.invoke(origin, true, false);
                    }
                } else {
                    callback.invoke(origin, true, false);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                if (prefs.getBoolean("dynamic_colors", true)) {
                    Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            if (palette.getVibrantSwatch() != null) {
                                setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)), false);
                            } else {
                                setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)), true);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                setTaskDescription(new ActivityManager.TaskDescription("Colombo | " + webView.getTitle(), webView.getFavicon(), palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))));
                            }
                        }
                    });
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setTaskDescription(new ActivityManager.TaskDescription("Colombo | " + webView.getTitle(), webView.getFavicon(), Color.parseColor("#307DFB")));
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
        } else {
            Snackbar.make(coordinatorLayout, R.string.msg_upload_failed, Snackbar.LENGTH_SHORT).show();
        }

        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    materialSearchView.setQuery(searchWrd, false);
                }
            }
            return;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() && materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else if (materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
        } else if (bottomSheet.isSheetShowing()) {
            bottomSheet.dismissSheet();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        this.menu = menu;

        menu.findItem(R.id.action_new).setIcon(R.drawable.menu_new);
        menu.findItem(R.id.action_home).setIcon(R.drawable.menu_home);
        menu.findItem(R.id.action_refresh).setIcon(R.drawable.menu_refresh);
        menu.findItem(R.id.action_search_words).setIcon(R.drawable.menu_search_words);
        menu.findItem(R.id.action_dekstop).setIcon(R.drawable.menu_dekstop);
        menu.findItem(R.id.action_bookmark).setIcon(R.drawable.menu_bookmark);
        menu.findItem(R.id.action_private).setIcon(R.drawable.menu_private);
        menu.findItem(R.id.action_add).setIcon(R.drawable.menu_add);
        menu.findItem(R.id.action_copy).setIcon(R.drawable.menu_copy);
        menu.findItem(R.id.action_share).setIcon(R.drawable.menu_share);
        menu.findItem(R.id.action_history).setIcon(R.drawable.menu_history);
        menu.findItem(R.id.action_settings).setIcon(R.drawable.menu_settings);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            menu.findItem(R.id.action_new).setVisible(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            menu.findItem(R.id.action_menu).setIcon(getResources().getDrawable(R.drawable.ic_menu));
        }

        if (isIncognito) {
            menu.findItem(R.id.action_private).setChecked(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
                break;
            case R.id.action_home:
                webView.loadUrl(getHomepage());
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    AnimationUtil.fadeInView(webView, 200);
                    ExpandAnimationUtil.collapse(titleFrame);
                }
                break;
            case R.id.action_refresh:
                webView.reload();
                break;
            case R.id.action_share:
                String shareBody = webView.getUrl();
                share(webView.getTitle(), shareBody, "Share " + webView.getTitle());
                break;
            case R.id.action_history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;
            case R.id.action_bookmark:
                if (webView.getVisibility() == View.VISIBLE && titleFrame.getVisibility() == View.GONE) {
                    AnimationUtil.fadeOutView(webView, 200);
                    ExpandAnimationUtil.expand(titleFrame);
                } else if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    AnimationUtil.fadeInView(webView, 200);
                    ExpandAnimationUtil.collapse(titleFrame);
                }
                break;
            case R.id.action_search_words:
                webView.showFindDialog(null, true);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    AnimationUtil.fadeInView(webView, 200);
                    ExpandAnimationUtil.collapse(titleFrame);
                }
                break;
            case R.id.action_copy:
                copyToClipBoard(webView.getUrl());
                Toast.makeText(this, "Current link copied to clipboard!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_private:
                isIncognito = !isIncognito;
                item.setChecked(isIncognito);
                adapter.notifyDataSetChanged();

                if (!isIncognito) {
                    //Exiting incognito
                    CookieManager.getInstance().setAcceptCookie(true);
                    webSettings.setAppCacheEnabled(true);
                    webView.getSettings().setSavePassword(true);
                    webView.getSettings().setSaveFormData(true);
                    webSettings.setDatabaseEnabled(true);
                    webSettings.setDomStorageEnabled(true);
                    applyColors(false);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().setAcceptCookie(true);
                    } else {
                        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
                    }
                    CookieSyncManager.createInstance(MainActivity.this);
                    CookieSyncManager.getInstance().startSync();
                } else {
                    //Entering incognito
                    webView.isPrivateBrowsingEnabled();
                    webView.getSettings().setSavePassword(false);
                    webView.getSettings().setSaveFormData(false);
                    applyColors(true);
                    webSettings.setDatabaseEnabled(false);
                    webSettings.setDomStorageEnabled(false);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().setAcceptCookie(false);
                    } else {
                        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
                    }
                    CookieSyncManager.createInstance(MainActivity.this);
                    CookieSyncManager.getInstance().startSync();
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
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            webView.loadUrl(intent.getDataString());
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean("active", true);
        ed.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();

        adapter.notifyDataSetChanged();

        if (webView.getUrl() == null) {
            webView.loadUrl(getHomepage());
        }

        setUpPrefs();
        checkInternet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    /**
     * SetUp the UI elements importing them
     */
    private void setUpElements() {
        if (shortcutIncognitoComing != null && shortcutIncognitoComing.equals("yes")) {
            isIncognito = true;
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_search_toolbar));
        }

        title = (TextView) findViewById(R.id.toolbar_title); // SearchBar Title
        appTitle = (TextView) findViewById(R.id.app_title); // Big Colombo TextView
        titleFrame = (RelativeLayout) findViewById(R.id.big_title); // FrameLayout with Big Colombo TextView

        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setInterpolator(new FastOutSlowInInterpolator());
        progressBar.setProgress(0);

        progressBarFrame = (RelativeLayout) findViewById(R.id.progress_container);
        progressBarFrame.setVisibility(View.GONE);
        ExpandAnimationUtil.expand(progressBarFrame);

        cardSearch = (CardView) findViewById(R.id.card_search); // CardView with SearchView
        search = findViewById(R.id.search); // FrameLayout of cardSearch

        whiteSearch = findViewById(R.id.white_search);
        whiteSearch.setVisibility(View.GONE);

        materialSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        materialSearchView.setAnimationDuration(400);
        materialSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));

        webviewContainer = (FrameLayout) findViewById(R.id.webviewContainer);

        backround_bookmark_text = (RelativeLayout) findViewById(R.id.backround_bookmarks);
        bookmark_text = (TextView) findViewById(R.id.text_bookmark);
        bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        settings = (ImageView) findViewById(R.id.settings);

        rv = (RecyclerView) findViewById(R.id.recyclerViewer);

        containerNoBookmarks = (RelativeLayout) findViewById(R.id.containerNoBookmarks);
        containerNoBookmarks.setVisibility(View.GONE);
        no_bookmark_text = (TextView) findViewById(R.id.text_no_bookmarks);
        no_bookmark_text_2 = (TextView) findViewById(R.id.text_2_no_bookmarks);

        webView = (ObservableWebView) findViewById(R.id.webview);
        if (urlIntent == null) {
            if (prefs.getBoolean("home_bookmarks", true)) {
                webView.setVisibility(View.GONE);
            } else {
                webView.setVisibility(View.VISIBLE);
                titleFrame.setVisibility(View.GONE);
            }
        }

        if (isTablet(this)) {
            back = (ImageView) findViewById(R.id.back);
            forward = (ImageView) findViewById(R.id.forward);
            bookmark = (ImageView) findViewById(R.id.bookmark);
        }
    }

    private void setUpSearchView() {
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    AnimationUtil.fadeInView(webView, 200);
                    ExpandAnimationUtil.collapse(titleFrame);
                }
                if (query.startsWith("www") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".com") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".gov") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".net") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".org") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".mil") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".edu") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".int") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".ly") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".de") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".uk") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".it") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".jp") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".ru") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.endsWith(".gl") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else if (query.contains(".") && !query.contains(" ") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else {
                    webView.loadUrl(getSearchPrefix() + query);
                }

                materialSearchView.closeSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                AnimationUtil.fadeOutView(whiteSearch, 400);
                webView.setNestedScrollingEnabled(true);
                toolbar.setVisibility(View.VISIBLE);
                cardSearch.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Load the url based on actions
     */
    private void handleUrlLoading() {
        if (urlIntent != null && shortcutNew == null && shortcutIncognito == null) {
            webView.loadUrl(urlIntent);
            webView.setVisibility(View.VISIBLE);
            titleFrame.setVisibility(View.GONE);
        } else if (shortcutNew != null && shortcutNew.equals("yes")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setData(Uri.parse(getHomepage()));
            startActivity(intent);
        } else if (shortcutIncognito != null && shortcutIncognito.equals("yes")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setData(Uri.parse(getHomepage()));
            intent.putExtra("shortcut_incognito_coming", "yes");
            startActivity(intent);
        } else {
            webView.loadUrl(getHomepage());
        }
    }

    /**
     * Handle GPS
     */
    private void handleLocation() {
        if (Build.VERSION.SDK_INT >= M) {
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
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
            }
        }
    }

    /**
     * Apply UI colors
     *
     * @param incognito is used to determinate if you want incognito colors or not
     */
    private void applyColors(boolean incognito) {
        // TODO : ContextCompat.getColor(this, R.color.color...) usare questo
        if (incognito) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDarkIncoginto));
            }
            progressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar_light));
            appbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryIncognito));
            // App title big
            appTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            appTitle.setText(R.string.app_name_private);
            // Search card backround
            cardSearch.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorTextLight));
            // Search card text
            title.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
            // Recyclerviewer backround color
            rv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundIncognito));
            // Webview container (Framelayout)
            webviewContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundIncognito));
            // Bookmark text color
            bookmark_text.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            // Backround of bookmark
            backround_bookmark_text.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBookmarksBarIncognito));
            no_bookmark_text.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            no_bookmark_text_2.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
            progressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar_dark));
            progressBar.getProgressDrawable().setAlpha(178);
            appbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            // App title big
            appTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            appTitle.setText(R.string.app_name);
            // Search card backround
            cardSearch.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorTextLight));
            // Search card text
            title.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
            // Recyclerviewer backround color
            rv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTextLight));
            // Webview container (Framelayout)
            webviewContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTextLight));
            // Bookmark text color
            bookmark_text.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
            // Backround of bookmark
            backround_bookmark_text.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBookmarksBar));
            no_bookmark_text.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
            no_bookmark_text_2.setTextColor(ContextCompat.getColor(this, R.color.colorBlack50));
        }
    }

    /**
     * Handle Bookmark elements
     */
    private void setUpBookmarksStructure() {
        gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
        rv = (RecyclerView) findViewById(R.id.recyclerViewer);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(gridLayoutManager);
        adapter = new MyAdapter(this, cardDatas);
        rv.setAdapter(adapter);
        retrieve();
    }

    /**
     * Scans if array is empty or not
     */
    private void detectArraySize() {
        if (cardDatas.isEmpty()) {
            containerNoBookmarks.setVisibility(View.VISIBLE);
        } else {
            containerNoBookmarks.setVisibility(View.GONE);
        }
    }

    /**
     * SetUp the elements animated
     */
    private void setUpUiAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (search != null && search.getVisibility() == View.VISIBLE) {
                search.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_fab_in));
            }
        }
    }

    /**
     * SetUp clicklisteners
     */
    private void setUpClickListeners() {
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialSearchView.showSearch(true);
                AnimationUtil.fadeInView(whiteSearch, 400);
                webView.setNestedScrollingEnabled(false);
                toolbar.setVisibility(View.GONE);
                cardSearch.setVisibility(View.GONE);
                title.setVisibility(View.GONE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (webView.getVisibility() == View.VISIBLE && titleFrame.getVisibility() == View.GONE) {
                        AnimationUtil.fadeOutView(webView, 200);
                        ExpandAnimationUtil.expand(titleFrame);
                    } else if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                        AnimationUtil.fadeInView(webView, 200);
                        ExpandAnimationUtil.collapse(titleFrame);
                    }
                }
            });
        }

        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.add_bookmark_title)
                        .content(R.string.add_bookmark_content)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Bookmark name", webView.getTitle(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Palette palette = Palette.from(webView.getFavicon()).generate();
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null && webView.getFavicon() != null) {
                                        save(input.toString(), webView.getUrl(), convertColorToHexadeimal(swatch));
                                    } else {
                                        save(input.toString(), webView.getUrl(), "#307DFB");
                                    }
                                } else {
                                    save(input.toString(), webView.getUrl(), "#307DFB");
                                }
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        if (isTablet(this)) {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        Snackbar.make(coordinatorLayout, "No history", Snackbar.LENGTH_SHORT);
                    }
                }
            });

            forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (webView.canGoForward()) {
                        webView.goForward();
                    } else {
                        Snackbar.make(coordinatorLayout, "No history", Snackbar.LENGTH_SHORT);
                    }
                }
            });

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.add_bookmark_title)
                            .content(R.string.add_bookmark_content)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("Bookmark name", webView.getTitle(), new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Palette palette = Palette.from(webView.getFavicon()).generate();
                                        Palette.Swatch swatch = palette.getVibrantSwatch();
                                        if (swatch != null && webView.getFavicon() != null) {
                                            save(input.toString(), webView.getUrl(), convertColorToHexadeimal(swatch));
                                        } else {
                                            save(input.toString(), webView.getUrl(), "#307DFB");
                                        }
                                    } else {
                                        save(input.toString(), webView.getUrl(), "#307DFB");
                                    }
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
        }
    }

    /**
     * Check internet class
     */
    private void checkInternet() {
        if (AppStatus.getInstance(this).isOnline()) {
            title.setTextColor(ContextCompat.getColor(this, R.color.colorBlack70));
            title.setText(R.string.search_query);
        } else {
            title.setTextColor(ContextCompat.getColor(this, R.color.colorRedError));
            title.setText("You are offline");
        }
    }

    /**
     * Copy to clipboard text
     *
     * @param text
     */
    private void copyToClipBoard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * SetUp first time snackbar for tutorial
     */
    private void firstTimeSnackBar() {
        if (prefs.getBoolean("first_time", true)) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.tutorial, Snackbar.LENGTH_LONG);
            snackbar.setAction("YES", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MainIntroActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
            snackbar.show();
            prefs.edit().putBoolean("first_time", false).apply();
        }
    }

    /**
     * Setup the preferences
     */
    private void setUpPrefs() {
        if (prefs.getBoolean("adblock", true)) {
            AdBlocker.init(this);
        }
        webView.getScale();
        webView.setGestureDetector(new GestureDetector(new CustomGestureDetector(webView, this)));
        webView.getSettings().setJavaScriptEnabled(prefs.getBoolean("javascript", true));
        webView.getSettings().setGeolocationEnabled(prefs.getBoolean("location_services", true));
        webView.getSettings().setBuiltInZoomControls(prefs.getBoolean("zooming", true));
        if (prefs.getBoolean("plugins", true)) {
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
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
                    return GOOGLE;
                case SEARCH_YAHOO:
                    return YAHOO;
                case SEARCH_DUCKDUCKGO:
                    return DUCKDUCKGO;
                case SEARCH_BING:
                    return BING;
                default:
                    return GOOGLE;
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
    private void setColor(int color, boolean noFavicon) {
        color = isIncognito ? ContextCompat.getColor(this, R.color.colorPrimaryIncognito) : color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), noFavicon ? !isIncognito ? ContextCompat.getColor(this, R.color.colorPrimaryDark) : ContextCompat.getColor(this, R.color.colorPrimaryDarkIncoginto) : StaticUtils.darkColor(color));
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
        }

        int colorFrom = ContextCompat.getColor(this, !isIncognito ? R.color.colorPrimaryIncognito : R.color.colorPrimary);
        Drawable backgroundFrom = appbar.getBackground();
        if (backgroundFrom instanceof ColorDrawable)
            colorFrom = ((ColorDrawable) backgroundFrom).getColor();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(150);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                appbar.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    /**
     * Method to detect if color is dark or light
     *
     * @param color
     * @return
     */
    public boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;

        if (darkness < 0.5) {
            return false; // It's a light color
        } else {
            return true; // It's a dark color
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
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_home_shortcut);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, data);
        sendBroadcast(shortcutintent);
        Toast.makeText(this, "Shortcut added to your home!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update database
     */
    private void update(int id, String newName) {

        DBAdapter db = new DBAdapter(this);
        db.openDB();

        long result = db.update(id, newName);
        if (result > 0) {
            Snackbar.make(coordinatorLayout, "Bookmark updated successfully!", Snackbar.LENGTH_SHORT).show();
            retrieve();
        } else {
            Snackbar.make(coordinatorLayout, "Unable to update the bookmark :_(", Snackbar.LENGTH_SHORT).show();
        }

        db.closeDB();
    }

    /**
     * Delete data from DB
     */
    private void delete(int id) {
        DBAdapter db = new DBAdapter(this);
        db.openDB();
        long result = db.delete(id);
        if (result > 0) {
            Snackbar.make(coordinatorLayout, "Bookmark deleted", Snackbar.LENGTH_SHORT).show();
            retrieve();
        } else {
            Snackbar.make(coordinatorLayout, "Unable to Delete", Snackbar.LENGTH_SHORT).show();
        }
        retrieve();
        db.closeDB();
    }

    /**
     * Get data from DB
     */
    private void retrieve() {
        DBAdapter db = new DBAdapter(this);
        db.openDB();
        cardDatas.clear();
        Cursor c = db.getAllData();
        while (c.moveToNext()) {
            int id = c.getInt(0);
            String name = c.getString(1);
            String code = c.getString(2);
            String hex = c.getString(3);

            CardData cardData = new CardData(id, name, code, hex);

            cardDatas.add(cardData);
        }
        if (!(cardDatas.size() < 1)) {
            rv.setAdapter(adapter);
        }
        db.closeDB();
        detectArraySize();
    }

    /**
     * Save data in DB
     */
    private void save(String name, String code, String hex) {
        DBAdapter db = new DBAdapter(this);
        db.openDB();
        long result = db.add(name, code, hex);
        if (result > 0) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Bookmark added successfully!", Snackbar.LENGTH_SHORT);
            snackbar.show();
            retrieve();
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Impossible to save Bookmark :_(", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        db.closeDB();
    }

    /**
     * Save data in DB
     */
    private void saveHistory(String title, String link) {
        DBAdapterHistory db = new DBAdapterHistory(this);
        db.openDB();
        long result = db.add(title, link);
        db.closeDB();
    }

    /**
     * Another method to get image file name
     */
    protected String getFilenameFromURL(URL url) {
        return getFilenameFromURL(url.getFile());
    }

    /**
     * Get file name method from Jack
     */
    protected String getFilenameFromURL(String url) {
        String[] p = url.split("/");
        String s = p[p.length - 1];
        if (s.indexOf("?") > -1) {
            return s.substring(0, s.indexOf("?"));
        }
        return s;
    }

    /**
     * Download image on a given URL
     *
     * @param imageUrl
     */
    private void downloadImage(String imageUrl) {
        DownloadManager downloadManager = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFilenameFromURL(imageUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
        Toast.makeText(MainActivity.this, "Downloading: " + getFilenameFromURL(imageUrl), Toast.LENGTH_SHORT).show();
    }

    /**
     * Download file
     *
     * @param url
     * @param contentDisposition
     * @param mimeType
     */
    private void downloadFile(String url, String contentDisposition, String mimeType) {
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

    /**
     * Share link or text
     */
    private void share(String subject, String text, String title) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, title));
    }

    /**
     * SetUp WebClient
     */
    public class WebClient extends WebViewClient {
        private Map<String, Boolean> loadedUrls = new HashMap<>();

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (prefs.getBoolean("adblock", true)) {
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            float initScale = 0;
            if (firstRun) {
                initScale = newScale;
                firstRun=false;
            } else {
                if (newScale > oldScale) {
                    //Toast.makeText(MainActivity.this, "Zoomed true", Toast.LENGTH_SHORT).show();
                } else {
                    if (newScale < initScale) {
                        //Toast.makeText(MainActivity.this, "Zoomed false", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            super.onScaleChanged(view, oldScale, newScale);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (url.startsWith("market:") || url.startsWith("https://m.youtube.com")
                    || url.startsWith("https://play.google.com") || url.startsWith("magnet:")
                    || url.startsWith("mailto:") || url.startsWith("intent:")
                    || url.startsWith("https://mail.google.com") || url.startsWith("https://plus.google.com")
                    || url.startsWith("geo:") || url.startsWith("google.streetview:")) {
                switch (Integer.parseInt(prefs.getString("show_open_dialog", "0"))) {
                    case OPEN_ALWAYS:
                        MenuSheetView menuSheetView =
                                new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, url, new MenuSheetView.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.action_open:
                                                try {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(url));
                                                    startActivity(intent);
                                                } catch (Exception exc) {
                                                    Toast.makeText(MainActivity.this, R.string.link_error, Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            case R.id.action_continue:
                                                webView.loadUrl(url);
                                                break;
                                        }
                                        bottomSheet.setUseHardwareLayerWhileAnimating(true);
                                        if (bottomSheet.isSheetShowing()) {
                                            bottomSheet.dismissSheet();
                                        }
                                        return true;
                                    }
                                });
                        menuSheetView.inflateMenu(R.menu.menu_intent_leave_colombo);
                        bottomSheet.showWithSheetView(menuSheetView);
                        return true;
                    case OPEN_APP:
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        } catch (Exception exc) {
                            Toast.makeText(MainActivity.this, R.string.link_error, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case OPEN_LINK:
                        webView.loadUrl(url);
                        return true;
                    default:
                        MenuSheetView menuSheetView2 =
                                new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, url, new MenuSheetView.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.action_open:
                                                try {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(url));
                                                    startActivity(intent);
                                                } catch (Exception exc) {
                                                    Toast.makeText(MainActivity.this, R.string.link_error, Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            case R.id.action_continue:
                                                webView.loadUrl(url);
                                                break;

                                        }
                                        bottomSheet.setUseHardwareLayerWhileAnimating(true);
                                        if (bottomSheet.isSheetShowing()) {
                                            bottomSheet.dismissSheet();
                                        }
                                        return true;
                                    }
                                });
                        menuSheetView2.inflateMenu(R.menu.menu_intent_leave_colombo);
                        bottomSheet.showWithSheetView(menuSheetView2);
                        return true;
                }
            } else if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
            }

            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Click on image
                    if (webView.getHitTestResult().getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                            || webView.getHitTestResult().getType() == WebView.HitTestResult.IMAGE_ANCHOR_TYPE
                            || webView.getHitTestResult().getType() == WebView.HitTestResult.IMAGE_TYPE) {
                        MenuSheetView menuSheetView1 =
                                new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, getFilenameFromURL(webView.getHitTestResult().getExtra()), new MenuSheetView.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.action_save_image:
                                                WebView.HitTestResult hr = webView.getHitTestResult();
                                                int type = hr.getType();
                                                String imageUrl = hr.getExtra();
                                                File file = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), getFilenameFromURL(imageUrl));
                                                if (Build.VERSION.SDK_INT >= M) {
                                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                                    } else {
                                                        try {
                                                            downloadImage(imageUrl);
                                                        } catch (Exception ex) {
                                                            Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } else {
                                                    try {
                                                        downloadImage(imageUrl);
                                                    } catch (Exception ex) {
                                                        Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                break;
                                        }
                                        bottomSheet.setUseHardwareLayerWhileAnimating(true);
                                        if (bottomSheet.isSheetShowing()) {
                                            bottomSheet.dismissSheet();
                                        }
                                        return true;
                                    }
                                });
                        menuSheetView1.inflateMenu(R.menu.menu_save_image);
                        bottomSheet.showWithSheetView(menuSheetView1);
                        return true;
                    } else if (webView.getHitTestResult().getType() == WebView.HitTestResult.ANCHOR_TYPE // Link click
                            || webView.getHitTestResult().getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                        MenuSheetView menuSheetView =
                                new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, webView.getHitTestResult().getExtra(), new MenuSheetView.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.action_open_new_link:
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    try {
                                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                                        intent.setData(Uri.parse(webView.getHitTestResult().getExtra()));
                                                        startActivity(intent);
                                                    } catch (Exception ex) {
                                                        Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    break;
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Tabs aren't avaliable for Android KitKat or <", Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            case R.id.action_open_save_bookmark:
                                                new MaterialDialog.Builder(MainActivity.this)
                                                        .title("Add Bookmark?")
                                                        .content("Give to your bookmark a name!")
                                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                                        .input("Bookmark name", "", new MaterialDialog.InputCallback() {
                                                            @Override
                                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                                    Palette palette = Palette.from(webView.getFavicon()).generate();
                                                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                                                    if (swatch != null && webView.getFavicon() != null) {
                                                                        save(input.toString(), webView.getHitTestResult().getExtra(), convertColorToHexadeimal(swatch));
                                                                    } else {
                                                                        save(input.toString(), webView.getHitTestResult().getExtra(), "#307DFB");
                                                                    }
                                                                } else {
                                                                    save(input.toString(), webView.getUrl(), "#307DFB");
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                break;
                                            case R.id.action_copy_link:
                                                try {
                                                    copyToClipBoard(webView.getHitTestResult().getExtra());
                                                    Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                                } catch (Exception exc) {
                                                    Toast.makeText(MainActivity.this, exc.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            case R.id.action_share_link:
                                                String shareBody = webView.getHitTestResult().getExtra();
                                                share("Website link", shareBody, "Share " + webView.getHitTestResult().getExtra());
                                                break;
                                        }
                                        bottomSheet.setUseHardwareLayerWhileAnimating(true);
                                        if (bottomSheet.isSheetShowing()) {
                                            bottomSheet.dismissSheet();
                                        }
                                        return true;
                                    }
                                });
                        menuSheetView.inflateMenu(R.menu.menu_open_link);
                        bottomSheet.showWithSheetView(menuSheetView);
                        return true;
                    } else if (webView.getHitTestResult().getType() == WebView.HitTestResult.PHONE_TYPE) { // Phone number click
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(webView.getHitTestResult().getExtra()));
                        startActivity(intent);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap facIcon) {
            super.onPageStarted(view, url, facIcon);
            checkInternet();
            progressBar.setProgress(0);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setProgress(0);
            if (AppStatus.getInstance(MainActivity.this).isOnline()) {
                if (prefs.getBoolean("title_search", true)) {
                    title.setText(webView.getTitle());
                } else {
                    title.setText(webView.getUrl());
                    materialSearchView.setSearchText(webView.getUrl());
                }
                if (!isIncognito && webView.getTitle() != null && webView.getUrl() != null) {
                    saveHistory(webView.getTitle(), webView.getUrl());
                }
            }
        }
    }

    /**
     * Adapter recyclerviewer
     */
    public class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        Context c;
        ArrayList<CardData> cardData;

        public MyAdapter(Context c, ArrayList<CardData> cardData) {
            this.c = c;
            this.cardData = cardData;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);
            MyHolder holder = new MyHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyHolder holder, final int position) {
            String color = cardData.get(position).getHex();

            holder.bookmarkContainer.setBackgroundColor(Color.parseColor(color));
            if (isIncognito) {
                holder.bookmarkContainer.getBackground().setAlpha(230);
                holder.name.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorTextLight));
            } else {
                holder.bookmarkContainer.getBackground().setAlpha(178);
                if (isColorDark(Integer.decode(color))) {
                    holder.name.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorTextLight));
                } else {
                    holder.name.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorBlack70));
                }
            }

            holder.name.setText(cardData.get(position).getName());

            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    if (webView.getVisibility() == View.GONE) {
                        AnimationUtil.fadeInView(webView, 200);
                        ExpandAnimationUtil.collapse(titleFrame);
                    }
                    if (!webView.getUrl().equals(cardData.get(pos).getCode())) {
                        webView.loadUrl(cardData.get(pos).getCode());
                    }
                }
            });

            holder.setItemLongClickListener(new ItemLongClickListener() {
                @Override
                public void onItemLongClick(View v, final int pos) {
                    MenuSheetView menuSheetView =
                            new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, cardData.get(pos).getName(), new MenuSheetView.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_open_new: // Open in a new tab
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                                intent.setData(Uri.parse(cardData.get(pos).getCode()));
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(c, R.string.tabs_error, Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case R.id.action_share_bookmark: // Share the bookmark
                                            String shareBody = cardData.get(position).getCode();
                                            share(cardData.get(position).getName(), shareBody, "Share " + cardData.get(position).getName());
                                            bottomSheet.dismissSheet();
                                            break;
                                        case R.id.action_rename_bookmark: // Rename the bookmark
                                            new MaterialDialog.Builder(MainActivity.this)
                                                    .title(R.string.rename_bookmark_title)
                                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                                    .input("Bookmark name", cardData.get(position).getName(), new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                                            update(cardData.get(position).getId(), input.toString());
                                                            dialog.dismiss();
                                                        }
                                                    }).show();
                                            break;
                                        case R.id.action_delete_bookmark: // Delete the bookmark
                                            new MaterialDialog.Builder(MainActivity.this)
                                                    .title(R.string.delete_bookmark_title)
                                                    .content(cardData.get(pos).getName())
                                                    .positiveText(R.string.delete_bookmark_positive)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            delete(cardData.get(position).getId());
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .negativeText(R.string.delete_bookmark_negative)
                                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .show();
                                            break;
                                    }
                                    bottomSheet.setUseHardwareLayerWhileAnimating(true);
                                    if (bottomSheet.isSheetShowing()) {
                                        bottomSheet.dismissSheet();
                                    }
                                    return true;
                                }
                            });
                    menuSheetView.inflateMenu(R.menu.menu_bookmarks);
                    bottomSheet.showWithSheetView(menuSheetView);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cardData.size();
        }
    }
}
