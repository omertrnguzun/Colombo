package com.riccardobusetti.colombo.activities;

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
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.data.CardData;
import com.riccardobusetti.colombo.database.DBAdapter;
import com.riccardobusetti.colombo.holder.MyHolder;
import com.riccardobusetti.colombo.util.AdBlocker;
import com.riccardobusetti.colombo.util.AppStatus;
import com.riccardobusetti.colombo.util.ItemClickListener;
import com.riccardobusetti.colombo.util.ItemLongClickListener;
import com.riccardobusetti.colombo.util.StaticUtils;
import com.riccardobusetti.colombo.view.CustomWebChromeClient;
import com.riccardobusetti.colombo.view.ObservableWebView;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private boolean immersive = true;

    /**
     * Strings Vars
     */
    private String urlIntent = null;

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
    private SearchView searchView;
    private ObservableWebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private TextView title, appTitle;
    private FrameLayout titleFrame, webviewContainer, frameNoBookmarks, frameError;
    private CardView cardSearch;
    private BottomSheetLayout bottomSheet;
    private View search;
    private ImageView settings;
    private GridLayoutManager gridLayoutManager;
    private View backround_bookmark_text;
    private TextView bookmark_text, no_bookmark_text;
    private ImageView back, forward, bookmark;
    private Menu menu;

    /**
     * Detect if running on tablet screen
     */
    private static Boolean isTablet(Context context) {
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
                && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setUpElements();

        setUpBookmarksStructure();

        detectArraySize();

        setUpUiAnimations();

        setUpClickListeners();

        setUpLightIcons();

        handleUrlLoading();

        if (prefs.getBoolean("adblock", true)) {
            AdBlocker.init(this);
        }

        handleLocation();

        firstTimeSnackBar();

        /** Set Webview params */
        //webView.setNavigationViews(findViewById(R.id.previous), findViewById(R.id.next));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        WebSettings webSettings = webView.getSettings();

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
        if (AppStatus.getInstance(this).isOnline()) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        /** Cookie Settings */
        if (prefs.getBoolean("cookies", true)) {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        /** Database support */
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(this.getFilesDir().getPath() + getPackageName() + "/databases/");
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

        webView.setWebViewClient(new WebClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
                final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);

                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Download " + filename1 + "?", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(Color.parseColor("#1DE9B6"));
                snackbar.setAction("DOWNLOAD", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Build.VERSION.SDK_INT >= M) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            } else {
                                try {
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
                                } catch (Exception exc) {
                                    Toast.makeText(MainActivity.this, exc.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            try {
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
                callback.invoke(origin, true, false);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                if (prefs.getBoolean("dynamic_colors", true)) {
                    if (Build.VERSION.SDK_INT >= M) {
                        if (prefs.getBoolean("light_icons", true)) {
                            Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    setColor(palette.getLightMutedColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        setTaskDescription(new ActivityManager.TaskDescription(webView.getTitle(), webView.getFavicon(), palette.getLightMutedColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))));
                                    }
                                }
                            });
                        } else {
                            Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        setTaskDescription(new ActivityManager.TaskDescription(webView.getTitle(), webView.getFavicon(), palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))));
                                    }
                                }
                            });
                        }
                    } else {
                        Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                setColor(palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    setTaskDescription(new ActivityManager.TaskDescription(webView.getTitle(), webView.getFavicon(), palette.getVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))));
                                }
                            }
                        });
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setTaskDescription(new ActivityManager.TaskDescription(webView.getTitle(), webView.getFavicon(), Color.parseColor("#80DEEA")));
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

        if (prefs.getBoolean("swipe_to_refresh", true)) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                }
            });
        }
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
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() && searchView.getVisibility() == View.VISIBLE) {
            searchView.setIconified(false);
            searchView.setVisibility(View.GONE);
            searchView.clearFocus();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else if (searchView.getVisibility() == View.VISIBLE) {
            searchView.setIconified(false);
            searchView.setVisibility(View.GONE);
            searchView.clearFocus();
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
        menu.findItem(R.id.action_incognito).setIcon(R.drawable.menu_incognito);
        menu.findItem(R.id.action_add).setIcon(R.drawable.menu_add);
        menu.findItem(R.id.action_share).setIcon(R.drawable.menu_share);
        menu.findItem(R.id.action_settings).setIcon(R.drawable.menu_settings);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            menu.findItem(R.id.action_new).setVisible(false);
        }

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setBackground(new ColorDrawable(Color.TRANSPARENT));
        searchView.setQueryHint("");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconified(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.VISIBLE);
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
                if (query.endsWith(".mil") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".edu") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".int") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".ly") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".de") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".uk") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".it") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".jp") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".ru") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                }
                if (query.endsWith(".gl") || URLUtil.isValidUrl(query)) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);

                } else
                    webView.loadUrl(getSearchPrefix() + query);

                searchView.setIconified(false);
                searchView.setVisibility(View.GONE);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });


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
                    webView.setVisibility(View.VISIBLE);
                    titleFrame.setVisibility(View.GONE);
                }
                break;
            case R.id.action_refresh:
                webView.reload();
                break;
            case R.id.action_share:
                String shareBody = webView.getUrl();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share link"));
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
                    title.setTextColor(Color.parseColor("#B2B2B2"));
                    setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                    rv.setBackgroundColor(Color.parseColor("#E0F7FA"));
                    webviewContainer.setBackgroundColor(Color.parseColor("#E0F7FA"));
                    bookmark_text.setTextColor(Color.parseColor("#233B3F"));
                    backround_bookmark_text.setBackgroundColor(Color.parseColor("#B2EBF2"));
                    no_bookmark_text.setTextColor(Color.parseColor("#233B3F"));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        menu.findItem(R.id.action_menu).setIcon(getResources().getDrawable(R.drawable.ic_menu));
                        toolbar.setNavigationIcon(R.drawable.ic_search_toolbar);
                    }

                    setUpLightIcons();
                } else {
                    //When enter in incognito
                    appTitle.setText(R.string.app_name_incognito);
                    cardSearch.setCardBackgroundColor(Color.parseColor("#455A64"));
                    title.setTextColor(Color.parseColor("#FAFAFA"));
                    rv.setBackgroundColor(Color.parseColor("#263238"));
                    webviewContainer.setBackgroundColor(Color.parseColor("#263238"));
                    bookmark_text.setTextColor(Color.parseColor("#FAFAFA"));
                    backround_bookmark_text.setBackgroundColor(Color.parseColor("#37474F"));
                    no_bookmark_text.setTextColor(Color.parseColor("#FAFAFA"));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        menu.findItem(R.id.action_menu).setIcon(getResources().getDrawable(R.drawable.ic_menu_white));
                        toolbar.setNavigationIcon(R.drawable.ic_search_toolbar_white);
                    }

                    if (prefs.getBoolean("light_icons", true)) {
                    } else {
                        setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryIncognito));
                        setTheme(R.style.AppThemeNoActionBar);
                        appTitle.setTextColor(Color.parseColor("#FAFAFA"));
                        Drawable drawable_light = getResources().getDrawable(R.drawable.ic_settings_title_white);
                        settings.setImageDrawable(drawable_light);
                        if (isTablet(this)) {
                            Drawable drawable_back_white = getResources().getDrawable(R.drawable.ic_back_title_white);
                            back.setImageDrawable(drawable_back_white);

                            Drawable drawable_forward_white = getResources().getDrawable(R.drawable.ic_forward_title_white);
                            forward.setImageDrawable(drawable_forward_white);

                            Drawable drawable_bookmark_white = getResources().getDrawable(R.drawable.ic_bookmark_title_white);
                            bookmark.setImageDrawable(drawable_bookmark_white);
                        }
                    }
                }
                break;
            case R.id.action_add:
                createShortCut();
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        if (preferences.getBoolean("font", true)) {
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        urlIntent = getIntent().getDataString();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.toolbar_title); // SearchBar Title
        appTitle = (TextView) findViewById(R.id.app_title); // Big Colombo TextView
        titleFrame = (FrameLayout) findViewById(R.id.big_title); // FrameLayout with Big Colombo TextView

        cardSearch = (CardView) findViewById(R.id.card_search); // CardView with SearchView
        search = findViewById(R.id.search); // FrameLayout of cardSearch

        webviewContainer = (FrameLayout) findViewById(R.id.webviewContainer);

        backround_bookmark_text = findViewById(R.id.backround_bookmarks);
        bookmark_text = (TextView) findViewById(R.id.text_bookmark);
        bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        settings = (ImageView) findViewById(R.id.settings);

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

        frameNoBookmarks = (FrameLayout) findViewById(R.id.frameNoBookmarks);
        frameNoBookmarks.setVisibility(View.GONE);
        frameError = (FrameLayout) findViewById(R.id.frameError);
        no_bookmark_text = (TextView) findViewById(R.id.text_no_bookmarks);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            appTitle.setTextColor(Color.parseColor("#FAFAFA"));
            Drawable drawable_light = getResources().getDrawable(R.drawable.ic_settings_title_white);
            settings.setImageDrawable(drawable_light);
            if (isTablet(this)) {
                Drawable drawable_back_white = getResources().getDrawable(R.drawable.ic_back_title_white);
                back.setImageDrawable(drawable_back_white);

                Drawable drawable_forward_white = getResources().getDrawable(R.drawable.ic_forward_title_white);
                forward.setImageDrawable(drawable_forward_white);

                Drawable drawable_bookmark_white = getResources().getDrawable(R.drawable.ic_bookmark_title_white);
                bookmark.setImageDrawable(drawable_bookmark_white);
            }
        }
    }

    /**
     * Load the url based on actions
     */
    private void handleUrlLoading() {
        if (urlIntent != null) {
            webView.loadUrl(urlIntent);
            webView.setVisibility(View.VISIBLE);
            titleFrame.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_toolbar));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        } else {
            webView.loadUrl(getHomepage());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_search_toolbar));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                            webView.setVisibility(View.VISIBLE);
                            titleFrame.setVisibility(View.GONE);
                            searchView.setIconified(false);
                            searchView.setVisibility(View.VISIBLE);
                        } else {
                            searchView.setIconified(false);
                            searchView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
        }
    }

    /**
     * Handle Bookmark elements
     */
    private void setUpBookmarksStructure() {
        gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
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
            frameNoBookmarks.setVisibility(View.VISIBLE);
        } else {
            frameNoBookmarks.setVisibility(View.GONE);
        }
    }

    /**
     * SetUp the elements animated
     */
    private void setUpUiAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (settings != null && settings.getVisibility() == View.VISIBLE) {
                settings.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotation));
            }

            if (search != null && search.getVisibility() == View.VISIBLE) {
                search.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_fab_in));
            }

            if (appTitle != null && appTitle.getVisibility() == View.VISIBLE) {
                appTitle.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
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
                if (webView.getVisibility() == View.GONE && titleFrame.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.VISIBLE);
                    titleFrame.setVisibility(View.GONE);
                    searchView.setIconified(false);
                    searchView.setVisibility(View.VISIBLE);
                } else {
                    searchView.setIconified(false);
                    searchView.setVisibility(View.VISIBLE);
                }
            }
        });

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
                                save(input.toString(), webView.getUrl());
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
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
                                    save(input.toString(), webView.getUrl());
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
            // TODO Fare settings della cache
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /**
     * SetUp white theme
     */
    private void setUpLightIcons() {
        if (Build.VERSION.SDK_INT >= M) {
            if (prefs.getBoolean("light_icons", true)) {
                coordinatorLayout.setSystemUiVisibility(coordinatorLayout.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                appTitle.setTextColor(Color.parseColor("#233B3F"));
                Drawable drawable_black = getResources().getDrawable(R.drawable.ic_settings_title);
                settings.setImageDrawable(drawable_black);
                if (isTablet(this)) {
                    Drawable drawable_back_black = getResources().getDrawable(R.drawable.ic_back_title);
                    back.setImageDrawable(drawable_back_black);

                    Drawable drawable_forward_black = getResources().getDrawable(R.drawable.ic_forward_title);
                    forward.setImageDrawable(drawable_forward_black);

                    Drawable drawable_bookmark_black = getResources().getDrawable(R.drawable.ic_bookmark_title);
                    bookmark.setImageDrawable(drawable_bookmark_black);
                }
            } else {
                setTheme(R.style.AppThemeNoActionBar);
                appTitle.setTextColor(Color.parseColor("#FAFAFA"));
                Drawable drawable_light = getResources().getDrawable(R.drawable.ic_settings_title_white);
                settings.setImageDrawable(drawable_light);
                if (isTablet(this)) {
                    Drawable drawable_back_white = getResources().getDrawable(R.drawable.ic_back_title_white);
                    back.setImageDrawable(drawable_back_white);

                    Drawable drawable_forward_white = getResources().getDrawable(R.drawable.ic_forward_title_white);
                    forward.setImageDrawable(drawable_forward_white);

                    Drawable drawable_bookmark_white = getResources().getDrawable(R.drawable.ic_bookmark_title_white);
                    bookmark.setImageDrawable(drawable_bookmark_white);
                }
            }
        }
    }

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
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.tutorial, Snackbar.LENGTH_INDEFINITE);
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
        setUpLightIcons();
        if (prefs.getBoolean("adblock", true)) {
            AdBlocker.init(this);
        }
        webView.getSettings().setJavaScriptEnabled(prefs.getBoolean("javascript", true));
        webView.getSettings().setGeolocationEnabled(prefs.getBoolean("location_services", true));
        webView.getSettings().setBuiltInZoomControls(prefs.getBoolean("zooming", true));
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
        if (prefs.getBoolean("light_icons", true)) {
        } else {
            color = isIncognito ? ContextCompat.getColor(this, R.color.colorPrimaryIncognito) : color;
        }

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
                swipeRefreshLayout.setColorSchemeColors((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();


        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, isIncognito ? R.color.swipeRefreshIncognito : R.color.swipeRefresh));
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
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_home_shortcut);
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

        long result = db.UPDATE(id, newName);
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

        long result = db.Delete(id);
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

            CardData cardData = new CardData(id, name, code);

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
    private void save(String name, String code) {

        DBAdapter db = new DBAdapter(this);
        db.openDB();

        long result = db.add(name, code);

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
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (url.startsWith("market://") || url.startsWith("https://m.youtube.com")
                    || url.startsWith("https://play.google.com") || url.startsWith("magnet:")
                    || url.startsWith("mailto:") || url.startsWith("intent://")
                    || url.startsWith("https://mail.google.com") || url.startsWith("https://plus.google.com")
                    || url.startsWith("https://www.google.com/maps")) {
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
            }

            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
                                                            DownloadManager downloadManager = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
                                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
                                                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFilenameFromURL(imageUrl));
                                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                            downloadManager.enqueue(request);
                                                            Toast.makeText(MainActivity.this, "Downloading: " + getFilenameFromURL(imageUrl), Toast.LENGTH_SHORT).show();
                                                        } catch (Exception ex) {
                                                            Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } else {
                                                    try {
                                                        DownloadManager downloadManager = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
                                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
                                                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                                                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFilenameFromURL(imageUrl));
                                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                        downloadManager.enqueue(request);
                                                        Toast.makeText(MainActivity.this, "Downloading: " + getFilenameFromURL(imageUrl), Toast.LENGTH_SHORT).show();
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
                    } else if (webView.getHitTestResult().getType() == WebView.HitTestResult.ANCHOR_TYPE
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
                                                                save(input.toString(), webView.getHitTestResult().getExtra());
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                break;
                                            case R.id.action_copy_link:
                                                try {
                                                    copyToClipBoard(webView.getUrl());
                                                    Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                                } catch (Exception exc) {
                                                    Toast.makeText(MainActivity.this, exc.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                                break;
                                            case R.id.action_share_link:
                                                String shareBody = webView.getHitTestResult().getExtra();
                                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                                sharingIntent.setType("text/plain");
                                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Website Link");
                                                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                                                startActivity(Intent.createChooser(sharingIntent, "Share link"));
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
                    } else if (webView.getHitTestResult().getType() == WebView.HitTestResult.PHONE_TYPE) {
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
            if (prefs.getBoolean("circle_progress", true)) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                swipeRefreshLayout.setEnabled(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            checkInternet();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (prefs.getBoolean("circle_progress", true)) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(false);
            }

            if (prefs.getBoolean("title_search", true)) {
                title.setText(webView.getTitle());
            } else {
                searchView.setQuery(webView.getUrl(), false);
                title.setText(webView.getUrl());
            }

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
                    MenuSheetView menuSheetView =
                            new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, cardData.get(pos).getName(), new MenuSheetView.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.action_open_new:
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                                intent.setData(Uri.parse(cardData.get(pos).getCode()));
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(c, R.string.tabs_error, Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case R.id.action_share_bookmark:
                                            String shareBody = cardData.get(position).getCode();
                                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                            sharingIntent.setType("text/plain");
                                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, cardData.get(position).getName());
                                            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                                            startActivity(Intent.createChooser(sharingIntent, "Share bookmark"));
                                            bottomSheet.dismissSheet();
                                            break;
                                        case R.id.action_rename_bookmark:
                                            new MaterialDialog.Builder(MainActivity.this)
                                                    .title(R.string.rename_bookmark_title)
                                                    .content(R.string.rename_bookmark_content)
                                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                                    .input("Bookmark name", cardData.get(position).getName(), new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                                            update(cardData.get(position).getId(), input.toString());
                                                            dialog.dismiss();
                                                        }
                                                    }).show();
                                            break;
                                        case R.id.action_delete_bookmark:
                                            new MaterialDialog.Builder(MainActivity.this)
                                                    .title(R.string.delete_bookmark_title)
                                                    .content(R.string.delete_bookmark_content + cardData.get(pos).getName() + "?")
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
