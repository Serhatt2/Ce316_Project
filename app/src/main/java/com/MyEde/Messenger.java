package com.MyEde;

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

    public void registerMainController(MainController controller) {
        if (this.mainController == null)
            this.mainController = controller;
    }

    public WindowController getWindowController() {
        return windowController;
    }

    public void registerAlertController(WindowController popupController) {
        if (this.windowController == null || popupController == null)
            this.windowController = popupController;
    }
}
