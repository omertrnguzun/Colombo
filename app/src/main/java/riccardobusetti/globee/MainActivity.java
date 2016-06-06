package riccardobusetti.globee;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
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

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import riccardobusetti.globee.util.ColorSwitchAnimation;
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

    private boolean isIncognito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardView = (CardView) findViewById(R.id.card);

        if (cardView != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (cardView.getVisibility() == View.VISIBLE) {
                cardView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_in));
            }
        }

        setupUi();
    }

    private void launchPerms() {
        new PermissionsActivity(getBaseContext())
                .withAppName(getResources().getString(R.string.app_name))
                .withPermissions(new Permission(Permissions.WRITE_EXTERNAL_STORAGE, "To download files, Colombo must have access to your internal memory!"))
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

        if (Build.VERSION.SDK_INT >= 21) {
            // StatusBar color switch
            int colorFrom = getResources().getColor(!isIncognito ? R.color.colorPrimaryDarkIncognito : R.color.colorPrimaryDark);
            int colorTo = getResources().getColor(isIncognito ? R.color.colorPrimaryDarkIncognito : R.color.colorPrimaryDark);
            ColorSwitchAnimation.switchColor(getWindow(), colorFrom, colorTo);

            // Background color switch
            colorFrom = getResources().getColor(!isIncognito ? R.color.colorPrimaryIncognito : R.color.colorPrimary);
            colorTo = getResources().getColor(isIncognito ? R.color.colorPrimaryIncognito : R.color.colorPrimary);
            ColorSwitchAnimation.switchColor(findViewById(R.id.static_background), colorFrom, colorTo);

            swipeRefreshLayout.setColorSchemeColors(
                    getResources().getColor(isIncognito
                            ? R.color.swipeRefreshIncognito
                            : R.color.swipeRefresh)
            );
        }
    }

    private void setupUi() {
        setupSearchView();
        setupWebView();
        setupSwipeRefreshView();
    }

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
                }
            }
        });
    }

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

    private void setupWebView() {
        webView = (ObservableWebView) findViewById(R.id.webview);

        if (webView != null) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

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

            webView.loadUrl("https://www.google.com");

            webView.setWebViewClient(new WebViewClient() {

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("market://") || url.startsWith("https://www.youtube.com")
                            || url.startsWith("https://play.google.com") || url.startsWith("mailto:")) {
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
                    // mSearchView.setSearchBarTitle(webView.getTitle());
                }

            });

            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(final String url, String userAgent, final String contentDisposition,
                                            final String mimeType, long contentLength) {
                    final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimeType);

                    final Snackbar snackbar = Snackbar
                            .make(cardView, "Download " + filename1 + "?", Snackbar.LENGTH_LONG);
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

            webView.setWebChromeClient(new WebChromeClient() {
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
            });

            webView.setGestureDetector(new GestureDetector(new CustomGestureDetector()));
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
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK
                    ? null
                    : intent.getData();
            uploadMessagePreLollipop.onReceiveValue(result);
            uploadMessagePreLollipop = null;
        } else
            Toast.makeText(this.getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Gesture detector
    private class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((e1 == null || e2 == null) || (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)) {
                return false;
            } else {
                try {
                    if (e1.getX() - e2.getX() > 500 && Math.abs(velocityX) > 800) {
                        // right to left swipe... go to next page

                        if (webView.canGoForward()) {
                            webView.goForward();
                        } else {
                            Toast.makeText(MainActivity.this, "You haven't any history!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (e2.getX() - e1.getX() > 500 && Math.abs(velocityX) > 800) {
                        // left to right swipe... go to prev page

                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            Toast.makeText(MainActivity.this, "You haven't any history!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                } catch (Exception ignored) {
                }
                return false;
            }
        }
    }
}
