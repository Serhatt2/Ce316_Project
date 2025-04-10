package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
    private final ArrayList<String> supportedExtensions = new ArrayList<>(Arrays.asList("txt", "java", "c", "cpp", "py", "json", "csv"));
    public TreeView<FileItem> projectTreeView;
    private File filePath;
    protected ArrayList<String> fileContent = new ArrayList<>();

    protected void extractZip(File zip) throws IOException {
        String dir = zip.getParent()+File.separator+zip.getName().replaceAll("\\.zip$", "");
        byte[] buffer = new byte[1024];
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        try {
            ZipEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                File output = new File(dir, entry.getName());
                new File(output.getParent()).mkdirs();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(output);
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                entry = inputStream.getNextEntry();
            }
            inputStream.closeEntry();
        } finally {
            inputStream.close();
        }
        zip.delete();
        refreshTreeView();
    }


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

    private void fillTreeView(TreeItem<FileItem> Item) {
        File file = Item.getValue().file();
        if (!file.isDirectory()) return;

        File[] childs = file.listFiles();
        if (childs == null) return;

        for (File child : childs) {
            TreeItem<FileItem> childItem = new TreeItem<>(new FileItem(child));
            Item.getChildren().add(childItem);
            fillTreeView(childItem);
        }
    }

    private boolean loadFileContent(File file) {
        fileContent.clear();
        if (!file.isFile()) {
            return false;
        }
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.'); //index of the dot
        if (index == -1 || index == fileName.length() - 1) {return false;}
        String extension = fileName.substring(index+1).toLowerCase(); //noktadan sonrasını alıyor
        if (!supportedExtensions.contains(extension)) {return false;}

        try (Scanner reader = new Scanner(file)){
            while (reader.hasNextLine()) {
                fileContent.add(reader.nextLine());
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadTabPane(String tittle) {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        for (String a: fileContent) {
            textArea.appendText(a + "\n");
        }
        Tab newTab = new Tab(tittle,textArea);
        editorTabPane.getTabs().add(newTab);
    }

    protected void removeFile(File file){
        file.delete();
        refreshTreeView();
    }

    protected void refreshTreeView() {
        //Daha doldurulacak
    }
}
