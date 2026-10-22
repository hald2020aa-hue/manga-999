package com.mangamm.reader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Android native browser shell. JS bridge is registered ONLY on bundled assets WebView,
 * never on websites the user visits. Browsing and content are fully separate WebViews. */
public class MainActivity extends Activity {
    private static final int PICK_IMPORT = 601;
    private static final int SAVE_BACKUP = 602;
    private static final int BG = Color.rgb(3, 12, 22);
    private static final int CYAN = Color.rgb(0, 210, 252);
    private WebView ui, browser;
    private LinearLayout browserLayout;
    private EditText browserAddress;
    private ValueCallback<Uri[]> fileChooser;
    private String backupPending;
    private boolean browsing;
    private volatile boolean adBlockEnabled = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        setContentView(root);

        ui = new WebView(this);
        configure(ui, true);
        ui.setBackgroundColor(BG);
        ui.addJavascriptInterface(new UICommands(), "MangaBridge");
        ui.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri target = request.getUrl();
                if ("appassets.androidplatform.net".equals(target.getHost())) return !target.getPath().startsWith("/assets/");
                if (isWeb(target.toString())) { showBrowser(target.toString()); return true; }
                return true;
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (!"appassets.androidplatform.net".equals(u.getHost())) return null;
                String path = u.getPath();
                if (path == null || !path.startsWith("/assets/") || path.contains("..")) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
                String file = path.substring("/assets/".length());
                String type = file.endsWith(".html") ? "text/html" :
                    file.endsWith(".js") ? "text/javascript" :
                    file.endsWith(".css") ? "text/css" :
                    file.endsWith(".json") ? "application/json" :
                    file.endsWith(".svg") ? "image/svg+xml" :
                    file.endsWith(".jpg") || file.endsWith(".jpeg") ? "image/jpeg" :
                    file.endsWith(".png") ? "image/png" : "application/octet-stream";
                try {
                    InputStream stream = getAssets().open(file);
                    return new WebResourceResponse(type, "utf-8", stream);
                } catch (Exception ex) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
            }
        });
        ui.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView web, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooser != null) fileChooser.onReceiveValue(null);
                fileChooser = callback;
                Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "application/zip", "application/octet-stream"});
                try { startActivityForResult(pick, PICK_IMPORT); return true; }
                catch (Exception ex) { fileChooser = null; callback.onReceiveValue(null); return false; }
            }
        });
        root.addView(ui, new FrameLayout.LayoutParams(-1, -1));
        ui.loadUrl("https://appassets.androidplatform.net/assets/index.html");

        browserLayout = new LinearLayout(this);
        browserLayout.setOrientation(LinearLayout.VERTICAL);
        browserLayout.setBackgroundColor(BG);
        root.addView(browserLayout, new FrameLayout.LayoutParams(-1, -1));
        browserLayout.setVisibility(View.GONE);
        setupBrowserBar();
        browser = new WebView(this);
        configure(browser, false);
        browser.setBackgroundColor(BG);
        browser.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!adBlockEnabled) return null;
                String host=request.getUrl().getHost();
                if (host == null) return null;
                String[] denied={"doubleclick.net","googlesyndication.com","googleadservices.com","adnxs.com","popads.net","exoclick.com","propellerads.com","adskeeper.co.uk"};
                for (String domain : denied) {
                    if (host.equalsIgnoreCase(domain)||host.toLowerCase().endsWith("."+domain)) {
                        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                    }
                }
                return null;
            }
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap fav) {
                browserAddress.setText(url);
                if (isWeb(url)) callUI("window.MangaMM && MangaMM.visit(" + JSONObject.quote(url) + ")");
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String u = request.getUrl().toString();
                if (isWeb(u)) return false;
                if (u.startsWith("intent:") || u.startsWith("market:") || u.startsWith("mailto:") || u.startsWith("tel:")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); } catch(Exception ignored) { toast("No app can open this link"); }
                }
                return true;
            }
        });
        browser.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                // Native page progress; site content remains isolated.
                if (p == 100) browserAddress.clearFocus();
            }
        });
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(browser, true);
        browser.setDownloadListener((url, userAgent, contentDisposition, mimeType, length) -> download(url, userAgent, contentDisposition, mimeType));
        browserLayout.addView(browser, new LinearLayout.LayoutParams(-1, 0, 1));
        setupBrowserBottomBar();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configure(WebView v, boolean local) {
        WebSettings s = v.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(local);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setBuiltInZoomControls(!local);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(!local);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
    }
    private boolean isWeb(String u) {
        if (u == null) return false;
        Uri parsed = Uri.parse(u);
        return ("https".equalsIgnoreCase(parsed.getScheme()) || "http".equalsIgnoreCase(parsed.getScheme()))
            && parsed.getHost() != null && !parsed.getHost().isEmpty();
    }
    private String normalized(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.length() == 0) return "";
        if (s.startsWith("http://") || s.startsWith("https://")) return isWeb(s) ? s : "";
        if (s.contains(" ") || !s.contains(".")) return "https://www.google.com/search?q=" + Uri.encode(s);
        return "https://" + s;
    }
    private TextView action(String label, int minWidth, Runnable callback) {
        TextView t = new TextView(this);
        t.setText(label); t.setTextColor(CYAN); t.setTextSize(21);
        t.setGravity(Gravity.CENTER); t.setMinWidth(dp(minWidth));
        t.setPadding(dp(6), dp(8), dp(6), dp(8));
        t.setOnClickListener(v -> callback.run());
        return t;
    }
    private int dp(float dp) { return (int)(getResources().getDisplayMetrics().density * dp + 0.5f); }
    private void setupBrowserBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(5), dp(6), dp(5), dp(6));
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Color.rgb(5, 21, 35));
        bar.addView(action("‹", 35, () -> { if (browser.canGoBack()) browser.goBack(); else hideBrowser(); }));
        bar.addView(action("›", 35, () -> { if (browser.canGoForward()) browser.goForward(); }));
        browserAddress = new EditText(this);
        browserAddress.setTextColor(Color.WHITE);
        browserAddress.setHintTextColor(Color.rgb(126, 156, 183));
        browserAddress.setHint("Search or enter URL");
        browserAddress.setSingleLine(true);
        browserAddress.setTextSize(13);
        browserAddress.setSelectAllOnFocus(true);
        browserAddress.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_CLASS_TEXT);
        browserAddress.setBackgroundColor(Color.rgb(12, 35, 53));
        browserAddress.setPadding(dp(9), dp(10), dp(9), dp(10));
        browserAddress.setImeOptions(2);
        browserAddress.setOnEditorActionListener((v, actionId, event) -> {
            String u = normalized(browserAddress.getText().toString());
            if (isWeb(u)) browser.loadUrl(u); else toast("Invalid URL");
            browserAddress.clearFocus();
            return true;
        });
        bar.addView(browserAddress, new LinearLayout.LayoutParams(0, -2, 1));
        bar.addView(action("▣", 38, this::extractChapterImages));
        bar.addView(action("☆", 39, () -> {
            String u = browser.getUrl();
            if (isWeb(u)) callUI("window.MangaMM && MangaMM.bookmarkUrl(" + JSONObject.quote(u) + ")");
            toast("Saved to your sites");
        }));
        bar.addView(action("×", 39, this::hideBrowser));
        browserLayout.addView(bar, new LinearLayout.LayoutParams(-1, -2));
    }
    private void setupBrowserBottomBar() {
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(8), dp(6), dp(8), dp(5));
        nav.setBackgroundColor(Color.rgb(4, 17, 29));
        String[] labels = {"⌂ Home", "◈ Sources", "▣ Library", "▤ Reader", "⚙ Settings"};
        String[] tabs = {"home","sources","library","reader","settings"};
        for (int i=0;i<tabs.length;i++) {
            String tab=tabs[i];
            TextView t = new TextView(this);
            t.setText(labels[i]); t.setTextColor(Color.rgb(130,185,214));
            t.setTextSize(10); t.setGravity(Gravity.CENTER);
            nav.addView(t, new LinearLayout.LayoutParams(0, dp(45), 1));
            t.setOnClickListener(v -> {
                if ("reader".equals(tab)) { extractChapterImages(); return; }
                hideBrowser();
                callUI("window.MangaMM && MangaMM.route(" + JSONObject.quote(tab) + ")");
            });
        }
        browserLayout.addView(nav);
    }
    /** Best-effort chapter image capture initiated only by the user on the opened page.
     *  Never executes an app-privileged JS bridge on the remote browser. */
    private void extractChapterImages() {
        String script = "(function(){" +
            "var found=[];var seen={};Array.prototype.forEach.call(document.querySelectorAll('img'),function(img){" +
            "var src=img.getAttribute('data-src')||img.getAttribute('data-original')||img.getAttribute('data-lazy-src')||img.currentSrc||img.src;" +
            "if(!src||src.indexOf('data:')===0)return;try{src=new URL(src,location.href).href}catch(e){return;}" +
            "if(!/^https?:/i.test(src)||seen[src])return;" +
            "var w=img.naturalWidth||img.width||0;var h=img.naturalHeight||img.height||0;" +
            "if(w>=360||h>=450||/chapter|page|reader|webtoon/i.test(img.className+' '+img.parentNode.className)){seen[src]=1;found.push(src)}" +
            "});return JSON.stringify({title:document.title||'Web Chapter',url:location.href,images:found.slice(0,150)});" +
            "})()";
        browser.evaluateJavascript(script, result -> {
            try {
                String decoded = new org.json.JSONArray("[" + result + "]").getString(0);
                JSONObject payload = new JSONObject(decoded);
                if (payload.getJSONArray("images").length() == 0) {
                    toast("No chapter images found. Open a chapter page first.");
                    return;
                }
                hideBrowser();
                callUI("window.MangaMM && MangaMM.importChapterImages(" + JSONObject.quote(decoded) + ")");
            } catch(Exception error) { toast("This page does not support image extraction."); }
        });
    }
    private void showBrowser(String raw) {
        String u = normalized(raw);
        if (!isWeb(u)) { toast("Invalid website URL"); return; }
        runOnUiThread(() -> {
            browsing = true;
            browserLayout.setVisibility(View.VISIBLE);
            browserAddress.setText(u);
            browser.loadUrl(u);
        });
    }
    private void hideBrowser() {
        browsing = false;
        browserLayout.setVisibility(View.GONE);
        callUI("window.MangaMM && MangaMM.refresh()");
    }
    private void callUI(String js) {
        runOnUiThread(() -> { if (ui != null) ui.evaluateJavascript(js, null); });
    }
    private void toast(String msg) { runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()); }
    private void download(String url, String agent, String disposition, String mime) {
        if (!isWeb(url)) { toast("Unsupported download link"); return; }
        try {
            String filename = URLUtil.guessFileName(url, disposition, mime);
            filename = filename.replaceAll("[\\\\/:*?\"<>|]", "_");
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mime == null ? "application/octet-stream" : mime);
            request.setTitle(filename);
            request.setDescription("Manga MM Download");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            CookieManager cookie = CookieManager.getInstance();
            String cookies = cookie.getCookie(url);
            if (cookies != null) request.addRequestHeader("Cookie", cookies);
            if (agent != null) request.addRequestHeader("User-Agent", agent);
            DownloadManager manager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);
            toast("Downloading " + filename);
        } catch(Exception ex) { toast("Download failed: " + ex.getMessage()); }
    }
    @Override public void onBackPressed() {
        if (browsing) {
            if (browser.canGoBack()) browser.goBack(); else hideBrowser();
        } else {
            callUI("window.MangaMM && MangaMM.goBack()");
        }
    }
    @Override protected void onActivityResult(int code, int result, Intent data) {
        super.onActivityResult(code,result,data);
        if (code == PICK_IMPORT) {
            if (fileChooser != null) {
                Uri uri = result == RESULT_OK && data != null ? data.getData() : null;
                if (uri != null && ("application/zip".equals(getContentResolver().getType(uri)) || uri.toString().toLowerCase().endsWith(".zip"))) {
                    try(ZipInputStream zip = new ZipInputStream(getContentResolver().openInputStream(uri))) {
                        ZipEntry entry;
                        boolean restored = false;
                        while ((entry = zip.getNextEntry()) != null) {
                            if ("manga-mm-state.json".equals(entry.getName()) && entry.getSize() < 4_000_000) {
                                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                                byte[] block = new byte[4096]; int read;
                                while ((read=zip.read(block)) != -1) {
                                    if (bytes.size()+read>4_000_000) throw new Exception("Backup too large");
                                    bytes.write(block,0,read);
                                }
                                String json = bytes.toString("UTF-8");
                                callUI("window.MangaMM && MangaMM.restoreBackupJson(" + JSONObject.quote(json) + ")");
                                restored = true;
                                break;
                            }
                            zip.closeEntry();
                        }
                        if (!restored) toast("No Manga MM backup found inside ZIP");
                    } catch(Exception ex) { toast("Cannot read ZIP: " + ex.getMessage()); }
                    fileChooser.onReceiveValue(null);
                } else fileChooser.onReceiveValue(uri == null ? null : new Uri[]{uri});
                fileChooser = null;
            }
        } else if (code == SAVE_BACKUP) {
            if (result == RESULT_OK && data != null && data.getData() != null && backupPending != null) {
                try(OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out != null) {
                        ZipOutputStream zip = new ZipOutputStream(out);
                        zip.putNextEntry(new ZipEntry("manga-mm-state.json"));
                        zip.write(backupPending.getBytes(StandardCharsets.UTF_8));
                        zip.closeEntry();
                        zip.finish();
                        zip.flush();
                    }
                    toast("ZIP metadata backup saved");
                } catch(Exception ex) { toast("Backup error: " + ex.getMessage()); }
            }
            backupPending = null;
        }
    }
    @Override protected void onDestroy() {
        if (browser != null) browser.destroy();
        if (ui != null) ui.destroy();
        super.onDestroy();
    }
    private class UICommands {
        @JavascriptInterface public void openBrowser(String url) { showBrowser(url); }
        @JavascriptInterface public void toast(String text) { MainActivity.this.toast(text); }
        @JavascriptInterface public void saveBackup(String json) {
            runOnUiThread(() -> {
                backupPending = json;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/zip");
                i.putExtra(Intent.EXTRA_TITLE, "Manga-MM-backup.zip");
                try { startActivityForResult(i,SAVE_BACKUP); }
                catch(Exception ex) { toast("Cannot open file picker"); }
            });
        }
        @JavascriptInterface public void shareText(String text) {
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT,text);
                try { startActivity(Intent.createChooser(send,"Share with")); }
                catch(Exception ex) { toast("No sharing app found"); }
            });
        }
        @JavascriptInterface public void setAdBlock(boolean enabled) { adBlockEnabled=enabled; }
        @JavascriptInterface public void openDownloads() {
            runOnUiThread(() -> {
                try { startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)); }
                catch(Exception ex) { toast("Downloads app unavailable"); }
            });
        }
        @JavascriptInterface public void exitApp() { runOnUiThread(() -> finish()); }
        @JavascriptInterface public void downloadUrl(String url) { runOnUiThread(() -> download(url,null,null,null)); }
    }
}
