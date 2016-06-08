package riccardobusetti.globee.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amqtech.permissions.helper.objects.Permission;
import com.amqtech.permissions.helper.objects.Permissions;
import com.amqtech.permissions.helper.objects.PermissionsActivity;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import riccardobusetti.globee.R;
import riccardobusetti.globee.util.StaticUtils;
import riccardobusetti.globee.view.ObservableWebView;

public class MainActivity extends PlaceholderUiActivity {

    private static final int REQUEST_SELECT_FILE = 100;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;

    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> uploadMessagePreLollipop;

    private ObservableWebView webView;
    private FloatingSearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CardView cardView;
    private View static_backround;
    private Button refresh_no_connection;
    private FloatingActionButton fab;

    private boolean isIncognito;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private AccountHeader accountHeader;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Import all primary UI elements*/
        cardView = (CardView) findViewById(R.id.card);
        webView = (ObservableWebView) findViewById(R.id.webview);
        static_backround = findViewById(R.id.static_background);
        refresh_no_connection = (Button) findViewById(R.id.button);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        /** Fab that set as visible on INTENT */
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }

        /** ClickListerner for button in refresh */
        refresh_no_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlConnection();
                checkInternetResponse();
                webView.reload();
            }
        });
        refresh_no_connection.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                return true;
            }
        });

        /** Cardview animation */
        if (cardView != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (cardView.getVisibility() == View.VISIBLE) {
                cardView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_in));
            }
        }

        controlConnection();

        setupUi();

        /** Intent Reciever */
        if (savedInstanceState != null) {
            webView.loadUrl(savedInstanceState.getString("url"));
        } else if (getIntent().getAction().matches(Intent.ACTION_VIEW)) {
            webView.loadUrl(getIntent().getData().toString());
            fabIntentShow();
        } else webView.loadUrl("https://www.google.com/");
    }

    /** Fab animation */
    private void fabIntentShow() {

        Toast.makeText(this, "Long press FAB to hide it!", Toast.LENGTH_SHORT).show();

        if (fab.getVisibility() == View.GONE) {
            fab.setVisibility(View.VISIBLE);
            fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_fab_in));
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (fab.getVisibility() == View.VISIBLE) {
                    fab.setVisibility(View.GONE);
                    fab.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_fab_out));
                }
                return true;
            }
        });
    }

    /** Location setup for aquiring GPS */
    private void setLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Without GPS permissions the location won't work!", Toast.LENGTH_SHORT).show();
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

    /** Permissions request */
    private void launchPerms() {
        new PermissionsActivity(getBaseContext())
                .withAppName(getResources().getString(R.string.app_name))
                .withPermissions(new Permission(Permissions.WRITE_EXTERNAL_STORAGE, "To download files, Colombo must have access to your internal memory!"), new Permission(Permissions.ACCESS_FINE_LOCATION, "If you want to use WebApps with geolocation Colombo must have access to your position!"))
                .withPermissionFlowCallback(new PermissionsActivity.PermissionFlowCallback() {
                    @Override
                    public void onPermissionGranted(Permission permission) {
                        Toast.makeText(MainActivity.this, "The permissions are set!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(Permission permission) {
                        Toast.makeText(MainActivity.this, "You won't be able to download files!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setBackgroundColor(Color.parseColor("#4690CD"))
                .setBarColor(Color.parseColor("#236FB0"))
                .setStatusBarColor(Color.parseColor("#236FB0"))
                .launch();
    }

    /** Incognito mode setup */
    private void handleIncognito() {
        isIncognito = !isIncognito;

        WebSettings webSettings = webView.getSettings();
        CookieManager.getInstance().setAcceptCookie(!isIncognito);
        webSettings.setAppCacheEnabled(!isIncognito);
        webView.clearHistory();
        webView.clearCache(isIncognito);
        webView.clearFormData();
        webView.getSettings().setSavePassword(!isIncognito);
        webView.getSettings().setSaveFormData(!isIncognito);
        webView.isPrivateBrowsingEnabled();

        setColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void setupUi() {
        setupSearchView();
        setupWebView();
        setupSwipeRefreshView();
        setupDrawer();
    }

    /** Nav Drawer */
    private void setupDrawer() {
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimary)
                .build();

        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(0).withSelectable(false);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withShowDrawerOnFirstLaunch(false)
                .withAccountHeader(accountHeader)
                .withSelectedItem(-1)
                .addDrawerItems(
                        item0
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            switch ((int) drawerItem.getIdentifier()) {
                                case 0:

                                    break;
                            }
                        }
                        return false;
                    }
                })
                .build();
    }

    /** Searchview ed elementi */
    private void setupSearchView() {
        searchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        if (searchView != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (searchView.getVisibility() == View.VISIBLE) {
                searchView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_in));
            }
        }

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
            }

            @Override
            public void onSearchAction(String currentQuery) {
                if (currentQuery.startsWith("www")) {
                    webView.loadUrl("http://" + currentQuery);
                } else if (currentQuery.startsWith("http")) {
                    webView.loadUrl(currentQuery);
                } else {
                    webView.loadUrl("https://www.google.com/search?q=" + currentQuery);
                }
            }
        });

        searchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                drawer.openDrawer();
            }

            @Override
            public void onMenuClosed() {

            }
        });

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        webView.loadUrl("https://www.google.com");
                        break;
                    case R.id.action_share:
                        String shareBody = webView.getUrl();
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Website Link");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share link"));
                        break;
                    case R.id.action_refresh:
                        webView.reload();
                        break;
                    case R.id.action_incognito:
                        handleIncognito();
                        break;
                    case R.id.action_perms:
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                            launchPerms();
                        } else {
                            Toast.makeText(MainActivity.this, "Permissions are avaliable only for Android 6.0 or >", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }

    /** Swipe to refresh setup */
    private void setupSwipeRefreshView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.swipeRefresh));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });
    }

    /** Code to set up WebView */
    private void setupWebView() {
        setLocation();

        webView = (ObservableWebView) findViewById(R.id.webview);

        if (webView != null) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

            final WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setGeolocationEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            webSettings.setAppCacheEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            webSettings.setSaveFormData(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setPluginState(WebSettings.PluginState.ON);

            webSettings.setBuiltInZoomControls(true);
            webSettings.setSupportZoom(true);
            webSettings.setDisplayZoomControls(false);

            webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAllowFileAccess(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

            webView.setLongClickable(true);
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Snackbar snackbar = Snackbar
                            .make(cardView, "Do you want to copy current URL?", Snackbar.LENGTH_LONG);
                    snackbar.setAction("COPY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Link", webView.getUrl());
                            clipboard.setPrimaryClip(clip);
                            Snackbar snackbar1 = Snackbar
                                    .make(cardView, "URL: " + webView.getUrl() + " copied!", Snackbar.LENGTH_LONG);
                            snackbar1.show();
                        }
                    });
                    snackbar.show();
                    return false;
                }
            });

            /** Client with useful methods */
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("market://") || url.startsWith("https://www.youtube.com")
                            || url.startsWith("https://play.google.com") || url.startsWith("mailto:") || url.startsWith("intent://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                    view.loadUrl(url);
                    return true;
                }

                public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                    swipeRefreshLayout.setRefreshing(true);
                }

                public void onPageFinished(WebView view, String url) {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                    //searchView.setSearchBarTitle(webView.getTitle());
                }
            });

            /** Download files from WebView with this */
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(final String url, String userAgent, final String contentDisposition,
                                            final String mimeType, long contentLength) {
                    final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);

                    final Snackbar snackbar = Snackbar
                            .make(cardView, "Download " + filename1 + "?", Snackbar.LENGTH_LONG);
                    snackbar.setAction("DOWNLOAD", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                launchPerms();
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

            /** Chrome Client for specific actions */
            webView.setWebChromeClient(new WebChromeClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback,
                                                 WebChromeClient.FileChooserParams fileChooserParams) {

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
                    Palette.from(icon).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            setColor(palette.getLightVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                        }
                    });
                }
            });

            webView.setGestureDetector(new GestureDetector(new CustomGestureDetector()));
        }
    }

    /** Handling file upload requests */
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
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK
                    ? null
                    : intent.getData();
            uploadMessagePreLollipop.onReceiveValue(result);
            uploadMessagePreLollipop = null;
        } else
            Toast.makeText(this.getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

    /** Overriding the onBack paramenter */
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /** Class to detect custom gestures on WebView */
    private class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((e1 == null || e2 == null) || (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)) {
                return false;
            } else {
                try {
                    if (e1.getX() - e2.getX() > 500 && Math.abs(velocityX) > 800) {
                        /** Activated on swipe from right to left */
                        if (webView.canGoForward()) {
                            webView.goForward();
                        } else {
                            Snackbar.make(webView, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (e2.getX() - e1.getX() > 500 && Math.abs(velocityX) > 800) {
                        /** Activated on swipe from left to right */
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            Snackbar.make(webView, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                } catch (Exception ignored) {
                }
                return false;
            }
        }
    }

    /** Method to set the color of the status bar and backround */
    private void setColor(int color) {
        color = isIncognito ? ContextCompat.getColor(this, R.color.colorPrimaryIncognito) : color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), StaticUtils.darkColor(color));
            colorAnimation.setDuration(150);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(((int) animator.getAnimatedValue()));
                }
            });

            colorAnimation.start();
        }

        int colorFrom = ContextCompat.getColor(this, !isIncognito ? R.color.colorPrimaryIncognito : R.color.colorPrimary);
        Drawable backgroundFrom = static_backround.getBackground();
        if (backgroundFrom instanceof ColorDrawable) {
            colorFrom = ((ColorDrawable) backgroundFrom).getColor();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(150);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                static_backround.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, isIncognito ? R.color.swipeRefreshIncognito : R.color.swipeRefresh));
    }

    /** Checking internet before proceed */
    private void controlConnection() {
        RelativeLayout no_connection = (RelativeLayout) findViewById(R.id.relative_connection);

        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        /** First if is if there isn't connection */
        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {

            webView.setVisibility(View.GONE);
            no_connection.setVisibility(View.VISIBLE);

        } else {

            webView.setVisibility(View.VISIBLE);
            no_connection.setVisibility(View.GONE);

        }
    }

    private void checkInternetResponse() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        /** First if is if there isn't connection */
        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {

            Snackbar.make(webView, R.string.connection_no, Snackbar.LENGTH_SHORT).show();

        } else {

            Snackbar.make(webView, R.string.connection_ok, Snackbar.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        controlConnection();
    }
}
