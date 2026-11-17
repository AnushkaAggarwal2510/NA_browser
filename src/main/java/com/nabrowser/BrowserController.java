package com.nabrowser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BrowserController {

    @FXML private TabPane tabPane;
    @FXML private TextField addressBar;
    @FXML private javafx.scene.layout.StackPane webContainer;
    @FXML private ProgressBar loadingBar;

    private final Map<Tab, BrowserTab> browserTabs = new HashMap<>();

    private final BrowserDataStore dataStore = new BrowserDataStore();

    @FXML
    public void initialize() {

        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) -> showTab(newTab)
        );

        createBrowserTab(getHomePageUrl());

        Platform.runLater(this::installKeyboardShortcuts);
    }

    private String getHomePageUrl() {
        return Main.class.getResource("home.html").toExternalForm();
    }

    private void createBrowserTab(String url) {

        BrowserTab browserTab = new BrowserTab(url);
        Tab tab = browserTab.getTab();
        WebEngine engine = browserTab.getWebEngine();

        browserTabs.put(tab, browserTab);

        WebView webView = browserTab.getWebView();
        webView.setVisible(false);
        webView.setManaged(false);
        webContainer.getChildren().add(webView);

        engine.titleProperty().addListener(
            (observable, oldTitle, newTitle) -> {
                if (newTitle != null && !newTitle.isBlank()) {
                    tab.setText(newTitle);
                }
            }
        );

        engine.locationProperty().addListener(
            (observable, oldLocation, newLocation) -> {
                if (tabPane.getSelectionModel().getSelectedItem() == tab) {
                    addressBar.setText(newLocation);
                }
            }
        );

        engine.getLoadWorker().stateProperty().addListener(
            (observable, oldState, newState) -> {

                if (newState == Worker.State.SUCCEEDED) {

                    String pageUrl = engine.getLocation();
                    String pageTitle = engine.getTitle();

                    if (pageUrl != null &&
                        (pageUrl.startsWith("http://") ||
                         pageUrl.startsWith("https://"))) {

                        if (pageTitle == null || pageTitle.isBlank()) {
                            pageTitle = pageUrl;
                        }

                        dataStore.addHistory(pageTitle, pageUrl);
                    }
                }
            }
        );

        tab.setOnClosed(event -> {
    BrowserTab closedTab = browserTabs.remove(tab);

    if (closedTab != null) {
        webContainer.getChildren().remove(closedTab.getWebView());
    }

    if (tabPane.getTabs().isEmpty()) {
        createBrowserTab(getHomePageUrl());
    }
      });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void showTab(Tab tab) {

        for (Map.Entry<Tab, BrowserTab> entry : browserTabs.entrySet()) {
            boolean active = entry.getKey() == tab;

            WebView view = entry.getValue().getWebView();
            view.setVisible(active);
            view.setManaged(active);
        }

        if (tab == null) {
            addressBar.clear();
            loadingBar.progressProperty().unbind();
            loadingBar.setProgress(0);
            return;
        }

        BrowserTab browserTab = browserTabs.get(tab);

        if (browserTab == null) {
            return;
        }

        WebEngine engine = browserTab.getWebEngine();

        addressBar.setText(engine.getLocation());

        loadingBar.progressProperty().unbind();
        loadingBar.progressProperty().bind(
            engine.getLoadWorker().progressProperty()
        );
    }

    private WebEngine getCurrentEngine() {

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == null) {
            return null;
        }

        BrowserTab browserTab = browserTabs.get(selectedTab);

        return browserTab == null ? null : browserTab.getWebEngine();
    }

    private void loadPage(String input) {

        if (input == null || input.isBlank()) {
            return;
        }

        String url = input.trim();

        if (!url.contains("://") && url.contains(".")) {
            url = "https://" + url;
        } else if (!url.contains("://")) {
    url = "https://www.google.com/search?q="
            + URLEncoder.encode(url, StandardCharsets.UTF_8);
}

        WebEngine engine = getCurrentEngine();

        if (engine != null) {
            engine.load(url);
        }
    }

    @FXML
    private void loadAddress() {
        loadPage(addressBar.getText());
    }

    @FXML
    private void goBack() {

        WebEngine engine = getCurrentEngine();

        if (engine != null &&
            engine.getHistory().getCurrentIndex() > 0) {
            engine.getHistory().go(-1);
        }
    }

    @FXML
    private void goForward() {

        WebEngine engine = getCurrentEngine();

        if (engine != null &&
            engine.getHistory().getCurrentIndex()
                < engine.getHistory().getEntries().size() - 1) {
            engine.getHistory().go(1);
        }
    }

    @FXML
    private void refreshPage() {

        WebEngine engine = getCurrentEngine();

        if (engine != null) {
            engine.reload();
        }
    }

    @FXML
    private void goHome() {
        WebEngine engine = getCurrentEngine();

        if (engine != null) {
            engine.load(getHomePageUrl());
        }
    }

    @FXML
    private void newTab() {
        createBrowserTab(getHomePageUrl());
    }

    @FXML
    private void addBookmark() {

        WebEngine engine = getCurrentEngine();

        if (engine == null) {
            return;
        }

        String url = engine.getLocation();
        String title = engine.getTitle();

        if (url == null ||
            !(url.startsWith("http://") || url.startsWith("https://"))) {
            showMessage("Bookmark", "Open a website before bookmarking it.");
            return;
        }

        if (title == null || title.isBlank()) {
            title = url;
        }

        if (dataStore.isBookmarked(url)) {
            showMessage("Bookmark", "This page is already bookmarked.");
            return;
        }

        dataStore.addBookmark(title, url);
        showMessage("Bookmark saved", title);
    }

    @FXML
    private void showBookmarks() {

        ListView<BrowserDataStore.Bookmark> listView =
            new ListView<>(
                FXCollections.observableArrayList(dataStore.getBookmarks())
            );

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(
                BrowserDataStore.Bookmark item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.title() + "\n" + item.url());
                }
            }
        });

        Button openButton = new Button("Open");
        Button removeButton = new Button("Remove Bookmark");

        openButton.setOnAction(event -> {
            BrowserDataStore.Bookmark selected =
                listView.getSelectionModel().getSelectedItem();

            if (selected != null) {
                loadPage(selected.url());
            }
        });

        removeButton.setOnAction(event -> {
            BrowserDataStore.Bookmark selected =
                listView.getSelectionModel().getSelectedItem();

            if (selected != null) {
                dataStore.removeBookmark(selected.url());
                listView.getItems().remove(selected);
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BrowserDataStore.Bookmark selected =
                    listView.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    loadPage(selected.url());
                }
            }
        });

        showDataWindow("NA Browser - Bookmarks",
                listView, openButton, removeButton);
    }

    @FXML
    private void showHistory() {

        ListView<BrowserDataStore.Visit> listView =
            new ListView<>(
                FXCollections.observableArrayList(dataStore.getHistory())
            );

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(
                BrowserDataStore.Visit item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.title() + "\n" + item.url()
                            + "\n" + item.visitTime());
                }
            }
        });

        Button openButton = new Button("Open");
        Button clearButton = new Button("Clear History");

        openButton.setOnAction(event -> {
            BrowserDataStore.Visit selected =
                listView.getSelectionModel().getSelectedItem();

            if (selected != null) {
                loadPage(selected.url());
            }
        });

        clearButton.setOnAction(event -> {
            dataStore.clearHistory();
            listView.getItems().clear();
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BrowserDataStore.Visit selected =
                    listView.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    loadPage(selected.url());
                }
            }
        });

        showDataWindow("NA Browser - History",
                listView, openButton, clearButton);
    }

    private void showDataWindow(
        String title,
        ListView<?> listView,
        Button firstButton,
        Button secondButton) {

        Label heading = new Label(title);
heading.setStyle(
    "-fx-font-size: 18px; " +
    "-fx-font-weight: bold; " +
    "-fx-text-fill: #2E4060;"
);

HBox buttons = new HBox(10, firstButton, secondButton);
VBox layout = new VBox(12, heading, listView, buttons);

layout.setStyle(
    "-fx-background-color: #F4DAD4; " +
    "-fx-padding: 18px;"
);

listView.setStyle(
    "-fx-background-color: #FFF9F6; " +
    "-fx-control-inner-background: #FFF9F6; " +
    "-fx-text-fill: #2E4060;"
);

firstButton.setStyle(
    "-fx-background-color: #DD8C96; " +
    "-fx-text-fill: #2E4060; " +
    "-fx-font-weight: bold;"
);

secondButton.setStyle(
    "-fx-background-color: #DD8C96; " +
    "-fx-text-fill: #2E4060; " +
    "-fx-font-weight: bold;"
);

VBox.setVgrow(listView, Priority.ALWAYS);

        Stage window = new Stage();
        window.setTitle(title);
        window.initOwner(tabPane.getScene().getWindow());
        window.initModality(Modality.NONE);
        window.setScene(new Scene(layout, 550, 450));
        window.show();
    }

    private void showMessage(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeCurrentTab() {

        Tab selected = tabPane.getSelectionModel().getSelectedItem();

        if (selected != null) {
            tabPane.getTabs().remove(selected);
        }
    }

    private void installKeyboardShortcuts() {

        Scene scene = tabPane.getScene();

        if (scene == null) {
            return;
        }

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
            this::newTab
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
            this::closeCurrentTab
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
            () -> {
                addressBar.requestFocus();
                addressBar.selectAll();
            }
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
            this::refreshPage
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
            this::addBookmark
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
            this::showHistory
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN),
            this::showBookmarks
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN),
            this::goBack
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN),
            this::goForward
        );

        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.HOME, KeyCombination.ALT_DOWN),
            this::goHome
        );
    }
}
