package com.riccardobusetti.colombo.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.riccardobusetti.colombo.R;

/**
 * Created by riccardobusetti on 08/06/16.
 */

public class CustomWebChromeClient extends WebChromeClient implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private ObservableWebView webView;
    private Activity activity;

    private boolean isVideoFullscreen; // Indicates if the video is being displayed using a custom view (typically full-screen)
    private FrameLayout videoViewContainer;
    private CustomViewCallback videoViewCallback;

    private AlertDialog customViewDialog;
    private BottomSheetDialog alertDialog;

    /**
     * Builds a video enabled WebChromeClient.
     * @param webView The owner VideoEnabledWebView. Passing it will enable the VideoEnabledWebChromeClient to detect the HTML5 video ended event and exit full-screen.
     * Note: The web page must only contain one video tag in order for the HTML5 video ended event to work. This could be improved if needed (see Javascript code).
     */
    @SuppressWarnings("unused")
    public CustomWebChromeClient(Activity activity, ObservableWebView webView) {
        this.activity = activity;
        this.webView = webView;

        isVideoFullscreen = false;
    }

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    public boolean isVideoFullscreen()
    {
        return isVideoFullscreen;
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (view instanceof FrameLayout) {
            // A video wants to be shown
            FrameLayout frameLayout = (FrameLayout) view;
            View focusedChild = frameLayout.getFocusedChild();

            // Save video related variables
            this.isVideoFullscreen = true;
            this.videoViewContainer = frameLayout;
            this.videoViewCallback = callback;

            if (customViewDialog != null && customViewDialog.isShowing()) customViewDialog.dismiss();

            customViewDialog = new AlertDialog.Builder(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).setView(videoViewContainer).setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    activity.getWindow().setAttributes(attrs);
                    if (Build.VERSION.SDK_INT >= 14) activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }).create();
            customViewDialog.show();

            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            activity.getWindow().setAttributes(attrs);
            if (Build.VERSION.SDK_INT >= 14) activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

            if (focusedChild instanceof android.widget.VideoView) {
                // android.widget.VideoView (typically API level <11)
                android.widget.VideoView videoView = (android.widget.VideoView) focusedChild;

                // Handle all the required events
                videoView.setOnPreparedListener(this);
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);
            } else {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below
                if (webView != null && webView.getSettings().getJavaScriptEnabled() && focusedChild instanceof SurfaceView) {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    String js = "javascript:";
                    js += "var _ytrp_html5_video_last;";
                    js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];";
                    js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {";
                    {
                        js += "_ytrp_html5_video_last = _ytrp_html5_video;";
                        js += "function _ytrp_html5_video_ended() {";
                        {
                            js += "_VideoEnabledWebView.notifyVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView
                        }
                        js += "}";
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
                    }
                    js += "}";
                    webView.loadUrl(js);
                }
            }
        }
    }

    @Override @SuppressWarnings("deprecation")
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        // This method should be manually called on video end in all cases because it's not always called automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).

        if (isVideoFullscreen) {
            // Hide the video view, remove it, and show the non-video view
            if (customViewDialog != null && customViewDialog.isShowing()) customViewDialog.dismiss();

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback.getClass().getName().contains(".chromium.")) {
                videoViewCallback.onCustomViewHidden();
            }

            // Reset video related variables
            isVideoFullscreen = false;
            videoViewContainer = null;
            videoViewCallback = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onHideCustomView();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false; // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    @SuppressWarnings("unused")
    public boolean onBackPressed() {
        if (isVideoFullscreen) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
        Palette.from(view.getFavicon()).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();

                alertDialog = new BottomSheetDialog(activity);
                View v = activity.getLayoutInflater().inflate(R.layout.layout_popup, null, false);

                v.setBackgroundColor(palette.getDarkVibrantColor(Color.BLACK));

                ((TextView) v.findViewById(R.id.title)).setText(url);
                ((TextView) v.findViewById(R.id.content)).setText(message);

                v.findViewById(R.id.cancel).setVisibility(View.GONE);
                v.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.confirm();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        result.cancel();
                        dialog.dismiss();
                    }
                });
                alertDialog.setContentView(v);
                alertDialog.show();
            }
        });
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, final String url, final String message, final JsResult result) {
        Palette.from(view.getFavicon()).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();

                alertDialog = new BottomSheetDialog(activity);
                View v = activity.getLayoutInflater().inflate(R.layout.layout_popup, null, false);

                v.setBackgroundColor(palette.getDarkVibrantColor(Color.BLACK));

                ((TextView) v.findViewById(R.id.title)).setText(url);
                ((TextView) v.findViewById(R.id.content)).setText(message);

                v.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.cancel();
                        alertDialog.dismiss();
                    }
                });

                v.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.confirm();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        result.cancel();
                        dialog.dismiss();
                    }
                });
                alertDialog.setContentView(v);
                alertDialog.show();
            }
        });
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, final String message, final String defaultValue, final JsPromptResult result) {
        Palette.from(view.getFavicon()).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();

                alertDialog = new BottomSheetDialog(activity);
                View v = activity.getLayoutInflater().inflate(R.layout.layout_popup, null, false);

                v.setBackgroundColor(palette.getDarkVibrantColor(Color.BLACK));

                ((TextView) v.findViewById(R.id.title)).setText(message);
                v.findViewById(R.id.content).setVisibility(View.GONE);
                v.findViewById(R.id.inputLayout).setVisibility(View.VISIBLE);

                EditText input = (EditText) v.findViewById(R.id.input);
                input.setHint(defaultValue);

                v.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.cancel();
                        alertDialog.dismiss();
                    }
                });

                v.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.confirm();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        result.cancel();
                        dialog.dismiss();
                    }
                });
                alertDialog.setContentView(v);
                alertDialog.show();
            }
        });

        return true;
    }
}
