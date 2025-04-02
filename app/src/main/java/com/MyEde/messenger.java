package com.MyEde;

// Singleton class to manage communication between different controllers
public class messenger {
    private static messenger uniqueInstance;
    private Controller mainController;
    private PopupController alertController;

    private messenger() {}

    public static messenger getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new messenger();
        }
        return uniqueInstance;
    }

    public Controller getMainController() {
        return mainController;
    }

    public void registerMainController(Controller controller) {
        if (this.mainController == null)
            this.mainController = controller;
    }

    public PopupController getAlertController() {
        return alertController;
    }

    public void registerAlertController(PopupController popupController) {
        if (this.alertController == null || popupController == null)
            this.alertController = popupController;
    }
}
