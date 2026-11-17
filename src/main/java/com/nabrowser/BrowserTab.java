package com.nabrowser;

import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class BrowserTab {

    private final Tab tab;
    private final WebView webView;
    private final WebEngine webEngine;

    public BrowserTab(String url) {

        tab = new Tab("New Tab");

        webView = new WebView();
        webEngine = webView.getEngine();

        // WebView is displayed in the shared browser container.

        webEngine.titleProperty().addListener(
            (observable, oldTitle, newTitle) -> {
                if (newTitle != null && !newTitle.isBlank()) {
                    tab.setText(newTitle);
                }
            }
        );

        webEngine.load(url);
    }

    public Tab getTab() {
        return tab;
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }
}
