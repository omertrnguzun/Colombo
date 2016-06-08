package com.riccardobusetti.colombo;

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
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
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
import android.widget.Toast;

import com.amqtech.permissions.helper.objects.Permission;
import com.amqtech.permissions.helper.objects.Permissions;
import com.amqtech.permissions.helper.objects.PermissionsActivity;
import com.riccardobusetti.colombo.util.StaticUtils;
import com.riccardobusetti.colombo.util.VideoEnabledWebChromeClient;
import com.riccardobusetti.colombo.view.ObservableWebView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_FILE = 100;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;

    private ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> uploadMessagePreLollipop;

    private Toolbar toolbar;
    private AppBarLayout appbar;
    private SearchView searchView;
    private ObservableWebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CardView cardView, search;

    private boolean isIncognito;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        cardView = (CardView) findViewById(R.id.card);
        webView = (ObservableWebView) findViewById(R.id.webview);
        search = (CardView) findViewById(R.id.search);

        if (cardView != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (cardView.getVisibility() == View.VISIBLE) {
                cardView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_in));
            }
            if (search.getVisibility() == View.VISIBLE) {
                search.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_in));
            }
        }

        setSupportActionBar(toolbar);
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Snackbar.make(webView, R.string.msg_connection_failed, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_network_settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }).show();
        }

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

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        WebSettings webSettings = webView.getSettings();
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

        webView.setWebViewClient(new WebViewClient() {
            @Override
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

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(false);
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
                final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);

                Snackbar snackbar = Snackbar.make(cardView, "Download " + filename1 + "?", Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.parseColor("#4690CD"));
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

        webView.setWebChromeClient(new VideoEnabledWebChromeClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

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
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                toolbar.setTitle(title);
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
        webView.setOnScrollListener(new ObservableWebView.OnScrollListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                if (Math.abs(oldt - t) > 25) {
                    if (oldt - t > 0) {
                        appbar.setExpanded(true);
                    } else {
                        appbar.setExpanded(false);
                    }
                }
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

        if (savedInstanceState != null) {
            webView.loadUrl(savedInstanceState.getString("url"));
        } else if (getIntent().getAction().matches(Intent.ACTION_VIEW)) {
            webView.loadUrl(getIntent().getData().toString());
        } else webView.loadUrl("https://www.google.com/");
    }

    private void launchPerms() {
        new PermissionsActivity(getBaseContext())
                .withAppName(getResources().getString(R.string.app_name))
                .withPermissions(new Permission(Permissions.WRITE_EXTERNAL_STORAGE, "To download files, Colombo must have access to your storage!"), new Permission(Permissions.ACCESS_FINE_LOCATION, "If you want to use WebApps with geolocation Colombo must have access to your position!"))
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
        } else Snackbar.make(webView, R.string.msg_upload_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
            if (start != null && finish != null && start.getPointerCount() > 0 && finish.getPointerCount() > 0) {
                if (start.getX() - finish.getX() > 500 && Math.abs(velocityX) > 800) {
                    // right to left swipe... go to next page

                    if (webView.canGoForward()) {
                        webView.goForward();
                    } else {
                        Snackbar.make(webView, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
                    }
                    return true;
                }

                if (finish.getX() - start.getX() > 500 && Math.abs(velocityX) > 800) {
                    // left to right swipe... go to prev page

                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        Snackbar.make(webView, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
            return false;
        }
    }

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
        Drawable backgroundFrom = appbar.getBackground();
        if (backgroundFrom instanceof ColorDrawable) {
            colorFrom = ((ColorDrawable) backgroundFrom).getColor();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(150);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                appbar.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, isIncognito ? R.color.swipeRefreshIncognito : R.color.swipeRefresh));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("url", webView.getUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.startsWith("www") || query.startsWith("http")) {
                    if (!URLUtil.isValidUrl(query)) query = URLUtil.guessUrl(query);
                    webView.loadUrl(query);
                } else {
                    webView.loadUrl("https://www.google.com/search?q=" + query);
                }
                searchView.setIconified(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        menu.findItem(R.id.action_perms).setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                break;
            case R.id.action_perms:
                launchPerms();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Snackbar.make(webView, R.string.msg_connection_failed, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_network_settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }).show();
        }
    }
}
