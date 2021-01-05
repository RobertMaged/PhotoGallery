package z.com.android.photogallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.BitmapCompat;

public class PhotoPageFragment extends VisibleFragment{
    private static final String ARG_URI = "photo_page_url";

    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private OnBackPressedCallback callback;
    public static PhotoPageFragment newInstance(Uri uri){
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUri = getArguments().getParcelable(ARG_URI);

        callback = new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed() {
                backHandle();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        //backHandle();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

       mProgressBar = view.findViewById(R.id.progress_bar);
       mProgressBar.setMax(100);

       mWebView = view.findViewById(R.id.web_view);
       mWebView.getSettings().setJavaScriptEnabled(true);
       mWebView.setWebChromeClient(new WebChromeClient() {
           @Override
           public void onProgressChanged(WebView view, int newProgress) {
               if (newProgress == 100){
                   mProgressBar.setVisibility(View.GONE);
               } else {
                   mProgressBar.setVisibility(View.VISIBLE);
                   mProgressBar.setProgress(newProgress);
               }
           }

           AppCompatActivity activity = activity = (AppCompatActivity) getActivity();
           @Override
           public void onReceivedTitle(WebView view, String title) {
               activity.getSupportActionBar().setSubtitle(title);
           }

           /*@Override
           public void onReceivedIcon(WebView view, Bitmap icon) {
               activity.getSupportActionBar().setIcon();
           }*/
       } );
       mWebView.loadUrl(mUri.toString());
       mWebView.canGoBack();



       return view;
    }

    private void backHandle(){
        if (mWebView != null && mWebView.copyBackForwardList().getCurrentIndex() > 0){
            mWebView.goBack();
        } else {
            mWebView.clearCache(true);
            callback.remove();
            getActivity().finish();
        }
    }

}
