package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


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
    protected void onRadioButtonClicked(ActionEvent event) {
        if (event.getSource().equals(radioCreateMode)) {
            vboxImportSection.setVisible(false);
            vboxCreateSection.setVisible(true);
            radioCreateMode.setSelected(true);
            radioLoadMode.setSelected(false);
        } else {
            vboxImportSection.setVisible(true);
            vboxCreateSection.setVisible(false);
            radioCreateMode.setSelected(false);
            radioLoadMode.setSelected(true);
        }
    }

    @FXML
    protected void handleBrowseConfigFile(ActionEvent event) {
        if (event.getSource() == btnBrowseConfig) {
            File file = selectJsonFile();
            if (file != null) {
                fieldConfigPath.setText(file.getAbsolutePath());
            }
            else System.out.println("File not found!");
        } else if (event.getSource() == btnBrowseTarget) {
            File file = selectDirectory("/ProjectFiles");
            if (file != null) {
                fieldProjectTarget.setText(file.getAbsolutePath());
            }
            else System.out.println("File not found!");
        } else if (event.getSource() == btnBrowseZip) {
            File file = selectDirectory("");
            if (file != null) {
                fieldZipInput.setText(file.getAbsolutePath());
            }
            else System.out.println("File not found!");
        }
        else if (event.getSource() == btnPickDestDir) {
            File file = selectDirectory("/ConfigFiles");
            if (file != null) {
                fieldNewDestDir.setText(file.getAbsolutePath());
            } else System.out.println("File not found!");
        } else if (event.getSource() == btnBrowseEditConfig) {
            File file = selectJsonFile();
            if (file != null) {
                fieldConfigPath.setText(file.getAbsolutePath());
                loadConfigFromJson(file);
            }
            else System.out.println("File not found!");
        } else if (event.getSource() == btnDeleteConfig) {
            File file = selectJsonFile();
            if (file != null) {
                fieldDeletePath.setText(file.getAbsolutePath());
            } else System.out.println("File not found!");
        }
    }


    @FXML
    protected void handleCreateProject() throws IOException {
        Messenger exchangePoint = Messenger.getInstance();
        MainController controller = exchangePoint.getMainController();

        boolean isImport = radioLoadMode.isSelected();
        boolean isNew = radioCreateMode.isSelected();

        if (isNew && validateProjectInputFields(false)) {
            controller.buildNewProject(fieldProjectTarget.getText(), fieldProjectTitle.getText(), false, fieldNewConfigName.getText(), choiceLangSelect.getValue().toString(), fieldZipInput.getText(), null, fieldCmdArgs.getText(), areaExpectedOutput.getText()
            );
            controller.closeWindow();
        } else if (isImport && validateProjectInputFields(true)) {
            controller.buildNewProject(fieldProjectTarget.getText(), fieldProjectTitle.getText(), true, null, null, fieldZipInput.getText(), fieldConfigPath.getText(), null, null);
            controller.closeWindow();
        }

    }

    @FXML
    protected void handleSaveConfig() throws IOException {
        if (!validateEditConfigInputs()) {
            return;
        }

        Messenger exchangePoint = Messenger.getInstance();
        MainController controller = exchangePoint.getMainController();

        controller.updateConfigFile(fieldConfigPath.getText(), choiceLangSelect.getValue().toString(), fieldCmdArgs.getText(), areaExpectedOutput.getText());

        controller.closeWindow();
    }

    @FXML
    protected void handleDeleteConfig() {
        String path = fieldDeletePath.getText();
        if (path == null || path.isEmpty()) {
            return;
        }

        File fileToDelete = new File(path);
        Messenger exchangePoint = Messenger.getInstance();
        MainController controller = exchangePoint.getMainController();

        controller.removeFile(fileToDelete);
        controller.closeWindow();
    }

    @FXML
    protected void handleCreateFromTemplate() throws IOException {
        if (!validateCreateConfigInputs()) {
            return;
        }

        Messenger exchangePoint = Messenger.getInstance();
        MainController controller = exchangePoint.getMainController();

        File configJson = controller.buildConfigFile(fieldNewConfigName.getText(), choiceLangSelect.getValue().toString(), fieldCmdArgs.getText(), areaExpectedOutput.getText());

        controller.moveToDir(configJson, fieldNewDestDir.getText());
        controller.closeWindow();
    }

    private boolean validateCreateConfigInputs() {
        return !fieldNewConfigName.getText().isEmpty() && !areaExpectedOutput.getText().isEmpty() && !fieldNewDestDir.getText().isEmpty();
    }

    private boolean validateEditConfigInputs() {
        return !fieldConfigPath.getText().isEmpty() && !areaExpectedOutput.getText().isEmpty();
    }

    private boolean validateProjectInputFields(boolean importConfig) {
        if (importConfig) {
            return !fieldProjectTitle.getText().isEmpty() && !fieldConfigPath.getText().isEmpty() && !fieldProjectTarget.getText().isEmpty() && !fieldZipInput.getText().isEmpty();
        } else
            return !fieldProjectTitle.getText().isEmpty() && !fieldProjectTarget.getText().isEmpty() && !areaExpectedOutput.getText().isEmpty() && !fieldZipInput.getText().isEmpty();
    }

    protected void loadConfigFromJson(File file){
        if (fieldConfigPath  .getText().isEmpty()){
            return;
        }
        String jsonText ;
        try {
            jsonText = new String(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = new JSONObject(jsonText);

        JSONObject compilerConfig = jsonObject.getJSONObject("compilerConfig");
        String language = compilerConfig.getString("language");
        String compileCommand = compilerConfig.getString("compileCommand");
        String rCommand = compilerConfig.getString("runCommand");

        JSONObject projectConfig = jsonObject.getJSONObject("projectConfig");
        JSONArray arguments = projectConfig.getJSONArray("argument");
        String expectedOut = projectConfig.getString("expectedOutput");

        String argumentsToStr = "";
        for (int i = 0; i < arguments.length(); i++) {
            argumentsToStr += arguments.getString(i);
            if (i != arguments.length()-1)
                argumentsToStr+=",";
        }

        vboxEditSection  .setVisible(true);
        choiceLangSelect  .setValue(language);
        fieldCmdArgs  .setText(argumentsToStr);
        areaExpectedOutput  .setText(expectedOut);

    }

    private File selectJsonFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Configuration File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File configFolder = new File(System.getProperty("user.home"), "Documents/ConfigFiles");
        if (configFolder.exists()) {
            fileChooser.setInitialDirectory(configFolder);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        return fileChooser.showOpenDialog(null);
    }


    private File selectDirectory(String folderName) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory");

        File baseDir = new File(System.getProperty("user.home"), "Documents" + folderName);
        if (baseDir.exists()) {
            directoryChooser.setInitialDirectory(baseDir);
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        return directoryChooser.showDialog(null);
    }



}
