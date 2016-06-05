package riccardobusetti.globee;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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
import android.widget.TextView;
import android.widget.Toast;

import com.amqtech.permissions.helper.objects.Permission;
import com.amqtech.permissions.helper.objects.Permissions;
import com.amqtech.permissions.helper.objects.PermissionsActivity;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import riccardobusetti.globee.util.Fab;
import riccardobusetti.globee.util.ObservableWebView;

import static riccardobusetti.globee.R.id.swipe_layout;

public class MainActivity extends PlaceholderUiActivity {

    //Elementi e View
    ObservableWebView webView;
    FloatingSearchView mSearchView;
    CoordinatorLayout coordinatorLayout;
    SwipeRefreshLayout swipe;
    Fab fab;
    View v;
    View backround;
    CardView card;

    //Viste e context
    Context context;

    //Textview Fab
    TextView back, forward;

    //File Chooser
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        card = (CardView) findViewById(R.id.card);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

            //Animazione fab
            if (card.getVisibility() == View.VISIBLE) {
                card.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_in));

            }

        }

        backround = findViewById(R.id.static_backround);

        webView = (ObservableWebView) findViewById(R.id.webview);

        setMyWebView();

        swipe = (SwipeRefreshLayout) findViewById(swipe_layout);
        swipe.setColorSchemeColors(Color.parseColor("#757575"));

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                webView.reload();
            }
        });

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

            //Animazione fab
            if (mSearchView.getVisibility() == View.VISIBLE) {
                mSearchView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_in));

            }

        }

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {


            }

            @Override
            public void onSearchAction(String currentQuery) {

                if(currentQuery.startsWith("www")) {

                    webView.loadUrl("http://" + currentQuery);

                } else {

                    webView.loadUrl("https://www.google.com/search?q=" + currentQuery);

                }

                if(currentQuery.startsWith("http")) {

                    webView.loadUrl(currentQuery);

                }

            }

        });

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_home) {

                    webView.loadUrl("https://www.google.com");

                }

                if (item.getItemId() == R.id.action_share) {

                    String shareBody = webView.getUrl();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "WebSite Link");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share your link"));

                }

                if (item.getItemId() == R.id.action_refresh) {

                    webView.reload();

                }

                if (item.getItemId() == R.id.action_incognito) {

                    Toast.makeText(MainActivity.this, "To disable incognito mode restart Colombo", Toast.LENGTH_SHORT).show();

                    final WebSettings webSettings = webView.getSettings();
                    CookieManager.getInstance().setAcceptCookie(false);
                    webSettings.setAppCacheEnabled(false);
                    webView.clearHistory();
                    webView.clearCache(true);
                    webView.clearFormData();
                    webView.getSettings().setSavePassword(false);
                    webView.getSettings().setSaveFormData(false);
                    webView.isPrivateBrowsingEnabled();

                    if (Build.VERSION.SDK_INT >= 21) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkIncognito));

                        backround.setBackgroundColor(getResources().getColor(R.color.colorPrimaryIncognito));
                        swipe.setColorSchemeColors(Color.parseColor("#424242"));
                    }

                }
            }
        });

        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(this.getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

    public void setMyWebView() {

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        // edit webview settings
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true); // too important! tnks stackoverflow
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setAppCacheEnabled(true); // if all anypark offline, we don't need it
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
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // loaded online by default

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.loadUrl("https://www.google.com");

        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("market://")||url.startsWith("https://www.youtube.com")||url.startsWith("https://play.google.com")||url.startsWith("mailto:"))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                swipe.setRefreshing(true);

            }

            public void onPageFinished(WebView view, String url) {
                swipe.setRefreshing(false);
                swipe.setEnabled(false);
                //mSearchView.setSearchBarTitle(webView.getTitle());
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart ( final String url, String userAgent, final String contentDisposition, final String mimetype, long contentLength){

                final String filename1 = URLUtil.guessFileName(url, contentDisposition, mimetype);

                final Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Download " + filename1 + "?", Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.parseColor("#4690CD"));
                snackbar.setAction("DOWNLOAD", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //no dude, i need to permit to read my gps position

                            launchPerms(v);

                        } else {

                            DownloadManager.Request request = new DownloadManager.Request(
                                    Uri.parse(url));

                            final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            dm.enqueue(request);
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                            intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                            intent.setType("*/*");
                            Toast.makeText(MainActivity.this, "Downloading: " + filename, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                snackbar.show();

            }
        });

        // give me power, googlechromeclient!!
        webView.setWebChromeClient(new WebChromeClient() {

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

        });

        webView.setOnScrollChangeListener(new ObservableWebView.OnScrollChangeListener(){
            @Override
            public void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY <= 0) {

                    swipe.setEnabled(true);

                }

                /*if (scrollY > 0) {

                    mSearchView.setVisibility(View.GONE);

                } else {

                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

                        //Animazione fab
                        if (mSearchView.getVisibility() == View.GONE) {
                            mSearchView.setVisibility(View.VISIBLE);
                            mSearchView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_in));

                        }

                    }
                }*/

            }
        });

        webView.setGestureDetector(new GestureDetector(new CustomeGestureDetector()));
    }

    //Gesture detector
    private class CustomeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1 == null || e2 == null) return false;
            if(e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
            else {
                try { // right to left swipe .. go to next page
                    if(e1.getX() - e2.getX() > 500 && Math.abs(velocityX) > 800) {
                        //do your stuff
                        if(webView.canGoForward()) {
                            webView.goForward();
                        } else {

                            Toast.makeText(MainActivity.this, "You haven't any history!", Toast.LENGTH_SHORT).show();

                        }
                        return true;
                    } //left to right swipe .. go to prev page
                    else if (e2.getX() - e1.getX() > 500 && Math.abs(velocityX) > 800) {
                        //do your stuff
                        if(webView.canGoBack()) {
                            webView.goBack();
                        } else {

                            Toast.makeText(MainActivity.this, "You haven't any history!", Toast.LENGTH_SHORT).show();

                        }
                        return true;
                    } //bottom to top, go to next document
                    /*else if(e1.getY() - e2.getY() > 100 && Math.abs(velocityY) > 800
                            && webView.getScrollY() >= webView.getScale() * (webView.getContentHeight() - webView.getHeight())) {
                        //do your stuff
                        return true;
                    } //top to bottom, go to prev document
                    else if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 800 ) {
                        //do your stuff
                        return true;
                    }*/
                } catch (Exception e) { // nothing
                }
                return false;
            }
        }
    }

    public void launchPerms(View view) {

        new PermissionsActivity(getBaseContext())
                .withAppName(getResources().getString(R.string.app_name))
                .withPermissions(new Permission(Permissions.WRITE_EXTERNAL_STORAGE, "For download files Colombo must have access to your internal memory!"))
                .withPermissionFlowCallback(new PermissionsActivity.PermissionFlowCallback() {
                    @Override
                    public void onPermissionGranted(Permission permission) {
                        // I want to show a toast here
                        // if the permission was granted
                        Toast.makeText(MainActivity.this, "The permissions are setted!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(Permission permission) {
                        // I want to show a toast here
                        // if the permission was denied
                        Toast.makeText(MainActivity.this, "You won't be able to download files!", Toast.LENGTH_SHORT).show();
                    }
                })
                // Optional background color
                .setBackgroundColor(Color.parseColor("#4690CD"))
                // Optional bar color
                .setBarColor(Color.parseColor("#236FB0"))
                // Optional status bar color
                .setStatusBarColor(Color.parseColor("#236FB0"))
                .launch();
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        }
    }
}
