package com.MyEde;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private ContextMenu treeViewContextMenu;

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
        reloadTree();
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

    protected void buildNewProject(String targetDir, String name,
                                   boolean useExistingConfig, String configFileName,
                                   String language, String zipDir,
                                   String existingConfigPath, String arguments,
                                   String expectedOutput) throws IOException {
        File newFolder = new File(targetDir+File.separator + name);
        if (!newFolder.exists()) {
            if (newFolder.mkdirs()) {
                InitDir = newFolder;
            }
        }

        if (!useExistingConfig) {
            moveToDir(buildConfigFile(configFileName,language,arguments,expectedOutput),targetDir+File.separator+name);
        } else {
            File configFile = new File(existingConfigPath);
            Path srcPath = Path.of(existingConfigPath);
            Path destination = Path.of(targetDir+File.separator+name+File.separator+configFile.getName());
            try {
                Files.copy(srcPath,destination,StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Failed to copy file: " + e.getMessage());
            }
        }
        File zipFolder = new File(zipDir);
        File[] zips = zipFolder.listFiles();
        assert zips != null;
        for (File zip : zips) {
            zip.renameTo(new File(targetDir+File.separator+name+File.separator+zip.getName()));
        }
        TreeItem<FileItem> rootItem = new TreeItem<>(new FileItem(newFolder.getAbsoluteFile()));
        rootItem.setExpanded(true);
        projectTreeView.setRoot(rootItem);
        fillTreeView(rootItem);
        setupTreeView();
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
        reloadTree();
    }

    protected void reloadTree() {
        if (InitDir == null) return;
        TreeItem<FileItem> rootItem = new TreeItem<>(new FileItem(InitDir));
        rootItem.setExpanded(true);
        projectTreeView.setRoot(rootItem);
        fillTreeView(rootItem);
        setupTreeView();
    }

    private void setupTreeView() {
        projectTreeView.setOnMouseClicked(event -> {
            if (treeViewContextMenu != null) {
                treeViewContextMenu.hide();
            }

            Pattern pattern = Pattern.compile("'null'");
            Matcher matcher = pattern.matcher(event.getTarget().toString());
            if (matcher.find()) {
                projectTreeView.getSelectionModel().clearSelection();
                return;
            }

            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                TreeItem<FileItem> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() != null) {
                    if (loadFileContent(selectedItem.getValue().file())) {
                        loadTabPane(selectedItem.getValue().toString());
                    }
                }
            }

            if (event.getButton() == MouseButton.SECONDARY) {
                TreeItem<FileItem> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    File file = selectedItem.getValue().file();
                    ContextMenu contextMenu;
                    if (file.isFile()) {
                        String[] splitted = selectedItem.getValue().toString().split("\\.");
                        String extension = splitted.length > 1 ? splitted[1] : "";
                        contextMenu = buildContextMenu(extension, true, selectedItem);
                    } else {
                        contextMenu = buildContextMenu(null, false, selectedItem);
                    }
                    if (contextMenu != null) {
                        treeViewContextMenu = contextMenu;
                        contextMenu.show(projectTreeView, event.getScreenX(), event.getScreenY());
                    }
                }
            }
        });
    }

    private ContextMenu buildContextMenu(String extension, boolean isFile, TreeItem<FileItem> selectedItem) {
        if (isFile) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openItem = new MenuItem("Open");
            openItem.setOnAction(event -> {
                if (loadFileContent(selectedItem.getValue().file())) {
                    loadTabPane(selectedItem.getValue().toString());
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> removeFile(selectedItem.getValue().file()));

            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(event -> {
                try {
                    filePath = selectedItem.getValue().file();
                    handleEditConfig();  //must be implemented
                    filePath = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            MenuItem unzip = new MenuItem("Unzip");
            unzip.setOnAction(event -> {
                try {
                    extractZip(selectedItem.getValue().file());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            if (extension.equalsIgnoreCase("json")) {
                contextMenu.getItems().addAll(openItem, editItem, deleteItem);
            } else if (extension.equalsIgnoreCase("zip")) {
                contextMenu.getItems().addAll(unzip, deleteItem);
            } else {
                contextMenu.getItems().addAll(openItem, deleteItem);
            }
            return contextMenu;
        } else {
            File selectedDir = selectedItem.getValue().file();
            if (selectedDir.getAbsoluteFile().toString().equals(InitDir.getAbsoluteFile().toString())) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem unzipAll = new MenuItem("Unzip All");
                unzipAll.setOnAction(event -> {
                    try {
                        FileFilter filter = file -> file.getName().endsWith("zip");
                        File[] zipFiles = selectedDir.listFiles(filter);
                        if (zipFiles == null) return;
                        for (File zip : zipFiles) {
                            extractZip(zip);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                contextMenu.getItems().add(unzipAll);
                return contextMenu;
            } else {
                return null;
            }
        }
    }
}
