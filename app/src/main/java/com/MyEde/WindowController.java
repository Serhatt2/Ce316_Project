package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class WindowController {

    public RadioButton radioCreateMode;
    public RadioButton radioLoadMode;
    public VBox vboxImportSection;
    public VBox vboxCreateSection;
    public TextField fieldConfigPath;
    public TextField fieldProjectTitle;
    public TextField fieldZipInput;
    public ChoiceBox choiceLangSelect;
    public TextField fieldCmdArgs;
    public TextField fieldProjectTarget;
    public Button btnBrowseConfig;
    public Button btnBrowseTarget;
    public Button btnBrowseZip;
    public TextArea areaExpectedOutput;
    public TextField fieldNewConfigName;
    public TextField fieldNewDestDir;
    public Button btnPickDestDir;
    public VBox vboxEditSection;
    public TextField fieldCompileCmd;
    public TextField fieldExecuteCmd;
    public Button btnBrowseEditConfig;
    public TextField fieldDeletePath;
    public Button btnDeleteConfig;


    @FXML
    protected void onRadioButtonClicked(ActionEvent event)  {
        if (event.getSource().equals(radioCreateMode)) {
            vboxImportSection.setVisible(false);
            vboxCreateSection.setVisible(true);
            radioCreateMode.setSelected(true);
            radioCreateMode.setSelected(false);
        }
        else {
            vboxImportSection.setVisible(true);
            vboxCreateSection.setVisible(false);
            radioCreateMode.setSelected(false);
            radioLoadMode.setSelected(true);
        }

    }@FXML
    protected void handleBrowseConfigFile()  {


    }@FXML
    protected void handleCreateProject() throws IOException {

    }@FXML
    protected void handleSaveConfig()  {

    }@FXML
    protected void handleDeleteConfig()  {

    }@FXML
    protected void handleCreateFromTemplate()  {

    }


}
