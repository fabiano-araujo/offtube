package br.com.developer.fabiano.ytdownload;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import br.com.developer.fabiano.ytdownload.adapters.Control;
import br.com.developer.fabiano.ytdownload.adapters.DownloadTask;
import br.com.developer.fabiano.ytdownload.models.Link;

public class MainActivity extends AppCompatActivity {
    WebView browser;
    private MenuItem itemDownload;
    private EditText edtUrl;
    private String currentLink = "";
    private Handler durationHandler = new Handler();
    WebView browserLinks;
    private Toolbar toolbar;
    List<DownloadTask> downloadList = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtUrl = (EditText) findViewById(R.id.edtUrl);
        browser = (WebView)findViewById(R.id.webView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        edtUrl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String url = edtUrl.getText().toString();
                    if (!url.startsWith("http://") && !url.startsWith("https://")){
                        url = "http://"+url;
                    }
                    if (!isUrlValid(url)){
                        url = "http://www.google.com/search?q="+url;
                    }
                    browser.loadUrl(url);
                    if(getCurrentFocus()!=null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("onPageStarted","true");

                super.onPageStarted(view, url, favicon);
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        browser.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                /*try {
                    String url = view.getUrl();
                    Log.i("progress",progress+"");
                    edtUrl.setText(url);
                    if (!url.equals(currentLink)){
                        currentLink = url;
                        isUrlDownload(currentLink);
                    }
                }catch (Exception e){

                }*/
            }
        });

        browser.getSettings().setAppCacheEnabled(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        browser.loadUrl("http://m.youtube.com/");
        durationHandler.postDelayed(updateSeekBarTime, 50);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("linkClick",browser.getUrl());
            }
        });
    }

    private boolean pararUIupdate = false;
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            String url = browser.getUrl();
            if (!url.equals(currentLink)){
                Log.i("runCode","ok");
                edtUrl.setText(url);
                currentLink = url;
                isUrlDownload(currentLink);
            }
            if (!pararUIupdate){
                durationHandler.postDelayed(this, 500);
            }
        }
    };

    public boolean isUrlValid(String urlString) {
        return Patterns.WEB_URL.matcher(urlString.toLowerCase()).matches();
    }
    public void isUrlDownload(String url){
        if (itemDownload != null){
            itemDownload.setVisible(false);
        }
        browserLinks = (WebView) findViewById(R.id.webView2);

        browserLinks.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                browserLinks.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });
        browserLinks.getSettings().setJavaScriptEnabled(true);

        browserLinks.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        browserLinks.loadUrl("http://www.saveitoffline.com/#"+url);

    }
    public boolean download(Link link){
        try {
            if (downloadList.size() == 4){
                return false;
            }else{
                Log.i("linkDown",link.link);
                DownloadTask downloadTask = new DownloadTask(this,"teste",".mp3",downloadList);

                downloadList.add(downloadTask);
                downloadTask.downloadTask = downloadTask;
                downloadTask.execute(link.link);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            links = new ArrayList<>();
            links = Control.getLinkDownload(html);
            for (int i = 0; i < links.size(); i++) {
                Link link = links.get(i);
                Log.i("links",link.link);
                /*if (!link.label.contains("Video") && !link.label.contains("Audio")){

                }*/
            }
            if (links.size()>0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (itemDownload != null){
                            itemDownload.setVisible(true);
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemDownload = menu.findItem(R.id.item_downaload);
        itemDownload.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        try {
            switch (id){
                case android.R.id.home:
                    onBackPressed();
                    break;
                case R.id.item_downaload:

                    download(links.get(links.size()-1));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (browser.canGoBack()){
            browser.goBack();
        }else{
            browserLinks.stopLoading();
            browserLinks.destroy();
            pararUIupdate = true;
            browser.stopLoading();
            browser.destroy();
            browser.loadUrl("about:blank");
            this.finish();
        }
    }
}
