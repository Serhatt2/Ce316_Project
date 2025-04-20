package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
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
        Object source = event.getSource();
        File selectedFile = null;

        if (source == btnBrowseConfig   || source == btnBrowseEditConfig   || source == btnDeleteConfig) {
            selectedFile = selectJsonFile();
        } else if (source == btnBrowseTarget  ) {
            selectedFile = selectDirectory("/ProjectFiles");
        } else if (source == btnBrowseZip  ) {
            selectedFile = selectDirectory("");
        } else if (source == btnPickDestDir  ) {
            selectedFile = selectDirectory("/ConfigFiles");
        }

        if (selectedFile == null) {
            System.out.println("File not found!");
            return;
        }

        String path = selectedFile.getAbsolutePath();

        if (source == btnBrowseConfig  ) {
            fieldConfigPath  .setText(path);
        } else if (source == btnBrowseEditConfig  ) {
            fieldConfigPath  .setText(path);
            loadConfigFromJson(selectedFile);
        } else if (source == btnDeleteConfig) {
            fieldDeletePath  .setText(path);
        } else if (source == btnBrowseTarget  ) {
            fieldProjectTarget  .setText(path);
        } else if (source == btnBrowseZip  ) {
            fieldZipInput  .setText(path);
        } else if (source == btnPickDestDir  ) {
            fieldProjectTarget.setText(path);
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
        fileChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath() + "/ConfigFiles"));
        return fileChooser.showOpenDialog(new Popup());
    }

    private File selectDirectory(String folderName) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory");
        directoryChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath() + folderName));
        return directoryChooser.showDialog(new Popup());
    }


}
