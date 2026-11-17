# NA Browser

**NA Browser** is a desktop web browser developed using Java and JavaFX. It provides essential browsing functionality through a simple, user-friendly interface.

## Features

- **Multi-tab Browsing:** Open and switch between multiple browser tabs.
- **Website Navigation:** Visit websites using the address bar.
- **Google Search:** Search directly from the address bar.
- **Navigation Controls:** Back, Forward, Refresh and Home.
- **Bookmarks:** Save, view, open and remove bookmarked websites.
- **Browsing History:** Automatically record visited websites, view previous visits and clear history.
- **Persistent Storage:** Bookmarks and browsing history are saved locally and remain available after restarting the application.
- **Loading Indicator:** View website loading progress.
- **Keyboard Shortcuts:** Use keyboard combinations for common browser operations.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| JavaFX | Desktop graphical user interface |
| JavaFX WebView | Displaying web pages |
| FXML | Defining the browser interface |
| CSS | Styling and visual design |
| Maven | Dependency management and project build |

## User Interface Theme

NA Browser uses a coordinated colour palette:

- **Almond:** `#F4DAD4`
- **Dull Pink:** `#DD8C96`
- **Rhino:** `#2E4060`

The interface includes a custom title bar, navigation toolbar, tab bar, address bar and loading indicator.

## Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| Ctrl + T | Open a new tab |
| Ctrl + W | Close current tab |
| Ctrl + L | Focus address bar |
| Ctrl + R | Refresh page |
| Ctrl + D | Add bookmark |
| Ctrl + B | Open bookmarks |
| Ctrl + H | Open browsing history |
| Alt + Left | Go back |
| Alt + Right | Go forward |
| Alt + Home | Go to home page |

## Project Structure

```text
NA_browser/
│
├── pom.xml
├── README.md
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── nabrowser/
        │           ├── Main.java
        │           ├── BrowserController.java
        │           ├── BrowserTab.java
        │           └── BrowserDataStore.java
        │
        └── resources/
            └── com/
                └── nabrowser/
                    ├── browser.fxml
                    ├── browser.css
                    └── home.html
```

## Requirements

- Java Development Kit (JDK 26)
- Apache Maven 3.9.16 or compatible version
- Internet connection for accessing external websites

JavaFX dependencies are managed through Maven.

## Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/AnushkaAggarwal2510/NA_browser.git
```

### 2. Open the project directory

```bash
cd NA_browser
```

### 3. Compile the project

```bash
mvn clean compile
```

### 4. Run the application

```bash
mvn javafx:run
```

## Data Storage

The application stores browsing information locally in the user's home directory:

```text
.na-browser/
├── history.txt
└── bookmarks.txt
```

This allows browsing history and bookmarks to persist between application sessions.

## Team Members

- **Niharika**
- **Anushka**

## Future Improvements

Potential enhancements for future versions include:

- Download manager
- Private browsing mode
- Improved browser security and privacy controls
- Enhanced homepage customization
- Better navigation and browsing experience

---

**NA Browser — Simple, Functional and User-Friendly Desktop Browsing.**