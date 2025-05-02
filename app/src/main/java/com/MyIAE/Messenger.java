package com.MyIAE;

// Singleton class to manage communication between different controllers
public class Messenger {
    private static Messenger uniqueInstance;
    private MainController mainController;
    private WindowController windowController;

    private Messenger() {}

    public static Messenger getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Messenger();
        }
        return uniqueInstance;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        if (this.mainController == null)
            this.mainController = mainController;
    }

    public WindowController getWindowController() {
        return windowController;
    }

    public void setWindowController(WindowController WindowController) {
        if (this.windowController == null || WindowController == null)
            this.windowController = WindowController;
    }
}
