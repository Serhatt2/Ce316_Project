package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
public class MainController {

    @FXML
    public TableView resultsTableView;
    public SplitPane mainSplitPane;
    private Stage window;
    private Stage primaryStage;
    @FXML
    private TabPane editorTabPane;
    private File InitDir;
    private final ArrayList<String> supportedFileExtensions = new ArrayList<>(Arrays.asList("txt", "java", "c", "cpp", "py", "json", "csv"));
    public TreeView<FileItem> projectTreeView;
    private File filePath;



    @FXML
    protected void handleNewProject() throws IOException {

    }
    @FXML
    protected void handleOpenProject() throws IOException {

    }

    @FXML
    protected void handleCloseProject() throws IOException {

    }

    @FXML
    protected void handleEditConfig() throws IOException {

    }

    @FXML
    protected void handleCreateConfig(){

    }
    @FXML
    protected void handleExportConfig(){

    }

    @FXML
    protected void handleDeleteConfig() {

    }

    @FXML
    protected void handleCheck() {

    }

    @FXML
    protected void handleQuit() {

    }
    @FXML
    protected void handleAbout() {

    }

    @FXML
    protected void handleUserManual() {

    }

    protected void moveToDir(File file, String newDestDir) {  //Her System.err.println() yerine System.out.println()
        if (file == null || !file.exists()) {                  //yerine GUI da göstermek için Alert kullanılabilir.
            System.err.println("The directory does not exist.");
            return;
        }
        File destinationDir = new File(newDestDir);
        if (!destinationDir.exists()) {
            boolean created = destinationDir.mkdirs();
            if (!created) {
                System.err.println("Target directory could not be created.");
                return;
            }
        }
        boolean success = file.renameTo(new File(newDestDir, file.getName()));
        if (!success) {
            System.err.println("File could not be moved.");
        }
    }

    @FXML
    protected void closeWindow(){
        window.close();
        Messenger messageExchangePoint = Messenger.getInstance();
        messageExchangePoint.setWindowController(null);
    }

    record FileItem(File file) {
        @Override
        public String toString() {
            return file.getName();
        }
    }

    protected File buildConfigFile(String fileName, String language, String arguments, String expectedOutput) throws IOException {
        JSONObject root = new JSONObject();
        JSONObject compilerConfig = new JSONObject();
        JSONObject projectConfig = new JSONObject();
        JSONArray argumentArray = new JSONArray();
        String compileCommand = "";
        String runCommand = "";
        switch (language.toLowerCase()) {
            case "java":
                compileCommand = "javac";
                runCommand = "java";
                break;
            case "c":
                compileCommand = "gcc";
                runCommand = "";
                break;
            case "c++":
                compileCommand = "g++";
                runCommand = "";
                break;
            case "python":
                compileCommand = "";
                runCommand = "python";
                break;
        }
        compilerConfig.put("language", language);
        compilerConfig.put("compileCommand", compileCommand);
        compilerConfig.put("runCommand", runCommand);

        if (arguments != null && !arguments.isEmpty()) {
            for (String arg : arguments.split(",")) {
                argumentArray.put(arg.trim());
            }
        }
        projectConfig.put("argument", argumentArray);
        projectConfig.put("expectedOutput", expectedOutput);
        root.put("compilerConfig", compilerConfig);
        root.put("projectConfig", projectConfig);
        File configFile = new File(fileName + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(root.toString(4));
        }
        return configFile;
    }

    @FXML
    protected void updateConfigFile(String filePath, String language, String arguments, String expectedOutput) throws IOException {
        JSONObject root = new JSONObject();
        JSONObject compilerConfig = new JSONObject();
        String compileCommand = "";
        String runCommand = "";
        switch (language.toLowerCase()) {
            case "java":
                compileCommand = "javac";
                runCommand = "java";
                break;
            case "c":
                compileCommand = "gcc";
                break;
            case "c++":
                compileCommand = "g++";
                break;
            case "python":
                runCommand = "python";
                break;
        }
        compilerConfig.put("language", language);
        compilerConfig.put("compileCommand", compileCommand);
        compilerConfig.put("runCommand", runCommand);
        JSONObject projectConfig = new JSONObject();
        JSONArray argumentArray = new JSONArray();

        if (arguments != null && !arguments.trim().isEmpty()) {
            for (String arg : arguments.split(",")) {
                argumentArray.put(arg.trim());
            }
        }
        projectConfig.put("argument", argumentArray);
        projectConfig.put("expectedOutput", expectedOutput);
        root.put("compilerConfig", compilerConfig);
        root.put("projectConfig", projectConfig);

        Files.write(Paths.get(filePath), root.toString(4).getBytes());
    }




}
