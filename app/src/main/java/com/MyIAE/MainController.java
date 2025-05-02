package com.MyIAE;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
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

    private List<Student> lastCheckedStudents;


    protected void extractZip(File zip) throws IOException {
        String destinationDir = zip.getParent() + File.separator + zip.getName().replaceAll("\\.zip$", "");
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destinationDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        boolean deleted = zip.delete();
        reloadTree();
    }



    @FXML
    protected void handleNewProject() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("createProject.fxml"));
        Stage newProjectStage = new Stage();
        Messenger messenger = Messenger.getInstance();

        newProjectStage.initOwner(getPrimaryStage());
        newProjectStage.initModality(Modality.APPLICATION_MODAL);
        newProjectStage.setTitle("New Project");
        newProjectStage.setResizable(false);
        newProjectStage.setScene(loader.load());

        messenger.setWindowController(loader.getController());
        setPopup(newProjectStage);

        newProjectStage.showAndWait();




    }
    public Stage getPopup() {
        return window;
    }
    public void setPopup(Stage window) {
        this.window = window;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    @FXML
    protected void handleOpenProject() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Project Directory");
        File initialDir = new File(System.getProperty("user.home"), "Documents/ProjectFiles");
        if (!initialDir.exists()) {
            boolean created = initialDir.mkdirs();
            if (!created) {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            } else {
                directoryChooser.setInitialDirectory(initialDir);
            }
        } else {
            directoryChooser.setInitialDirectory(initialDir);
        }

        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null) {
            return;
        }

        InitDir = selectedDirectory.getAbsoluteFile();
        resultsTableView.getColumns().clear();
        resultsTableView.getItems().clear();
        TreeItem<FileItem> root = new TreeItem<>(new FileItem(selectedDirectory.getAbsoluteFile()));
        root.setExpanded(true);
        projectTreeView.setRoot(root);
        fillTreeView(root);
        setupTreeView();
    }


    @FXML
    protected void handleCloseProject() throws IOException {
      projectTreeView.setRoot(null);
        editorTabPane.getTabs().clear();
        resultsTableView.getColumns().clear();
        resultsTableView.getItems().clear();

    }

    @FXML
    protected void handleEditConfig() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("editConfig.fxml"));
        Messenger messenger = Messenger.getInstance();
        if (messenger.getWindowController() != null) {
            messenger.setWindowController(null);
        }

        // Scene
        setPopup(new Stage());
        window.initOwner(getPrimaryStage());
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Edit Config File");
        window.setResizable(false);
        window.setScene(fxmlLoader.load());
        // This comes after load() function. The reason behind of this, if we set the controller before load it the PopupController will store null
        messenger.setWindowController(fxmlLoader.getController());

        if (filePath != null) {
            messenger.getWindowController().fieldConfigPath.setText(filePath.getAbsolutePath());
            messenger.getWindowController().loadConfigFromJson(filePath);
        }
        window.showAndWait();


    }

    @FXML
    protected void handleCreateConfig() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("createConfig.fxml"));
        Messenger messenger = Messenger.getInstance();

        // Scene
        setPopup(new Stage());
        window.initOwner(getPrimaryStage());
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Create Configuration File");
        window.setResizable(false);
        window.setScene(fxmlLoader.load());
        // This comes after load() function. The reason behind of this, if we set the controller before load it the PopupController will store null
        messenger.setWindowController(fxmlLoader.getController());
        window.showAndWait();
    }

    @FXML
    protected void handleExportConfig() throws IOException {
        File configDir = new File(System.getProperty("user.home"), "Documents/ConfigFiles");
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (!created) {
                System.err.println("Could not create ConfigFiles directory.");
                return;
            }
        }
        Desktop.getDesktop().open(configDir);
    }


    @FXML
    protected void handleDeleteConfig() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("removeConfig.fxml"));
       Messenger messenger = Messenger.getInstance();

        // Scene
        setPopup(new Stage());
        window.initOwner(getPrimaryStage());
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Delete Configuration File");
        window.setResizable(false);
        window.setScene(fxmlLoader.load());
        // This comes after load() function. The reason behind of this, if we set the controller before load it the PopupController will store null
        messenger.setWindowController(fxmlLoader.getController());
        window.showAndWait();

    }

    @FXML
    protected void handleCheck() throws IOException {
        if (projectTreeView == null) {
            return;
        }

        if (InitDir == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Project Not Selected", "Please open or create a project.");
            return;
        }

        String basePath = InitDir.getAbsolutePath();
        checkOutputsOfStudents(basePath);

        String resultsCsv = basePath + "/StudentResults.csv";

        resultsTableView.getItems().clear();
        resultsTableView.getColumns().clear();

        TableColumn<Student, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentID"));

        TableColumn<Student, Boolean> studentResultCol = new TableColumn<>("Result");
        studentResultCol.setCellValueFactory(new PropertyValueFactory<>("hasPassed"));
        studentResultCol.setCellFactory(col -> new TextFieldTableCell<>() {
            @Override
            public void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value ? "pass" : "fail");
                }
            }
        });

        TableColumn<Student, Void> actionCol = new TableColumn<>("");

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button(" ");

            {
                button.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    String output = student.getOutput();

                    TextArea textArea = new TextArea(output);
                    textArea.setWrapText(true);
                    textArea.setEditable(false);

                    ScrollPane scrollPane = new ScrollPane(textArea);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);

                    Stage outputStage = new Stage();
                    outputStage.setTitle("Student Output __ " + student.getStudentID());
                    outputStage.setScene(new Scene(scrollPane, 750, 600));
                    outputStage.show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });





        resultsTableView.getColumns().addAll(studentIdCol, studentResultCol, actionCol);

        try (BufferedReader reader = new BufferedReader(new FileReader(resultsCsv))) {
            String record;
            while ((record = reader.readLine()) != null) {
                String[] fields = record.split(",");
                if (fields.length > 1) {
                    boolean isMatch = "Success".equals(fields[1]);

                    String output = "";
                    if (lastCheckedStudents != null) {
                        for (Student stu : lastCheckedStudents) {
                            if (stu.getStudentID().equals(fields[0])) {
                                output = stu.getOutput();
                                break;
                            }
                        }
                    }
                    resultsTableView.getItems().add(new Student(fields[0], isMatch, output));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        reloadTree();
    }

    protected void checkOutputsOfStudents(String projectPath) throws IOException {
        String configOfProject = getJsonFilePath(projectPath);

        if (configOfProject == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Config File", "No .json file found in selected project directory. Make Sure you selected the right project directory");
            return;
        }

        JSONObject projectConfig = getObject(configOfProject, "projectConfig");
        String expOutput = projectConfig.getString("expectedOutput");
        String pathOfCSV = projectPath + "/StudentResults.csv";
        FileWriter writer = new FileWriter(pathOfCSV);

        try {
            ArrayList<Student> studentList = queryStudents(projectPath);
            for (Student student : studentList) {
                System.out.println(">> Student ID: " + student.getStudentID());
                System.out.println(">> Output from run: [" + student.getOutput() + "]");
                System.out.println(">> Expected Output: [" + expOutput + "]");
                System.out.println(">> Output == Expected? : " + student.getOutput().equals(expOutput));
                System.out.println(">> Output.trim == Expected.trim? : " + student.getOutput().trim().equals(expOutput.trim()));
                System.out.println("-------------------------------------------------------------");

                student.setHasPassed(student.getOutput().trim().equals(expOutput.trim()));
                saveToCsv(writer, student.getStudentID(), student.getHasPassed());
                this.lastCheckedStudents = studentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Execution Failed", e.getMessage());
        }
    }


    protected ArrayList<Student> queryStudents(String filePath) throws Exception {
        File configFile = new File(getJsonFilePath(filePath));
        String configFilePath = configFile.getAbsolutePath();
        ArrayList<Student> students = new ArrayList<>();

        File directory =new File(filePath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] sourceFiles = file.listFiles();

                    assert sourceFiles != null;
                    for(File sourceFile: sourceFiles){
                        if (sourceFile.getName().endsWith(".java")){
                            Student student = runJava(configFilePath,sourceFile.getAbsolutePath());
                            student.setStudentID(file.getName());
                            students.add(student);
                        }else if (sourceFile.getName().endsWith(".c")){
                            Student student = cRun(configFilePath,sourceFile.getAbsolutePath());
                            student.setStudentID(file.getName());
                            students.add(student);
                        }else if (sourceFile.getName().endsWith(".py")){
                            Student student = pythonRun(configFilePath,sourceFile.getAbsolutePath());
                            student.setStudentID(file.getName());
                            students.add(student);
                        } else if (sourceFile.getName().endsWith(".cpp")) {
                            Student student = cppRun(configFilePath,sourceFile.getAbsolutePath());
                            student.setStudentID(file.getName());
                            students.add(student);
                        }
                    }
                }
            }
        }
        return students;
    }

    public Student cppRun(String configFilePath, String sourceFile){
        File cppFile = new File(sourceFile);
        String fileName = cppFile.getName();
        JSONObject compilerConfig;
        JSONObject projectConfig;

        try {
            compilerConfig = getObject(configFilePath, "compilerConfig");
            projectConfig = getObject(configFilePath, "projectConfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Extract the base name (without path) and remove .cpp extension
        String executableName = fileName.substring(0, fileName.length() - 4);

        String[] compileCommand = {
                compilerConfig.getString("compileCommand"),
                sourceFile,
                "-o",
                cppFile.getParent() + "\\" + executableName
        };

        JSONArray arguments = projectConfig.getJSONArray("argument");
        String[] executeCommand = new String[arguments.length() + 1];
        executeCommand[0] = cppFile.getParent() + "\\" + executableName;
        for (int i = 0; i < arguments.length(); i++) {
            executeCommand[i + 1] = arguments.getString(i);
        }

        return runSCode(compileCommand,executeCommand);
    }

    public Student pythonRun(String configFilePath, String sourceFile){
        //python -m py_compile
        JSONObject compilerConfig = null;
        JSONObject projectConfig = null;
        try {
            compilerConfig = getObject(configFilePath,"compilerConfig");
            projectConfig = getObject(configFilePath,"projectConfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONArray arguments = projectConfig.getJSONArray("argument");

        String[] compileCommand = {compilerConfig.getString("compileCommand")};
        String[] executeCommand = new String[arguments.length()+2];
        executeCommand[0] = compilerConfig.getString("runCommand");
        executeCommand[1] = sourceFile;
        for (int i = 0; i < arguments.length(); i++) {
            executeCommand[i+2] = arguments.getString(i);
        }

        return runSCode(compileCommand,executeCommand);


    }


    public Student cRun(String configFilePath, String sourceFile){
        File cFile = new File(sourceFile);
        String fileName = cFile.getName();
        JSONObject compilerConfig = null;
        JSONObject projectConfig = null;
        try {
            compilerConfig = getObject(configFilePath,"compilerConfig");
            projectConfig = getObject(configFilePath,"projectConfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String substring = fileName.substring(0, fileName.length() - 2);
        String[] compileCommand = {compilerConfig.getString("compileCommand"),sourceFile,"-o", cFile.getParent() + "\\" + substring};
        JSONArray arguments = projectConfig.getJSONArray("argument");
        String[] executeCommand = new String[arguments.length()+1];
        executeCommand[0] = cFile.getParent() + "\\" + substring;
        for (int i = 0; i < arguments.length(); i++) {
            executeCommand[i+1] = arguments.getString(i);
        }

        return runSCode(compileCommand,executeCommand);

    }

    public Student runJava(String configFilePath, String sourceFile){

        JSONObject compilerConfig = null;
        JSONObject projectConfig = null;

        try {
            compilerConfig = getObject(configFilePath,"compilerConfig");
            projectConfig = getObject(configFilePath,"projectConfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String jCompile = compilerConfig.getString("compileCommand");
        String runCommand = compilerConfig.getString("runCommand");

        String[] compileCommand = {jCompile,sourceFile};


        JSONArray arguments = projectConfig.getJSONArray("argument");

        String[] executeCommand = new String[arguments.length()+2];
        executeCommand[0] = runCommand;
        executeCommand[1] = sourceFile;
        for (int i = 0; i < arguments.length(); i++) {
            executeCommand[i+2] = arguments.getString(i);
        }

        return runSCode(compileCommand,executeCommand);


    }

    public Student runSCode(String[] compilerCommand, String[] executeCommand) {

        Student student = new Student();
        boolean isCompiled = true;
        boolean isRan = true;
        try {
            if (!Objects.equals(executeCommand[0], "python")) {
                // Compile the source
                ProcessBuilder compileProcessBuilder = new ProcessBuilder(compilerCommand);
                Process compileProcess = compileProcessBuilder.start();
                compileProcess.waitFor();

                // Check if the compilation was successful
                if (compileProcess.exitValue() != 0) {
                    isCompiled = false;
                }
            }

            // Run the compiled code
            ProcessBuilder runProcessBuilder = new ProcessBuilder(executeCommand);
            Process runProcess = runProcessBuilder.start();
            runProcess.waitFor();

            // Check if the run was successful
            if (runProcess.exitValue() != 0) {
                isRan = false;
            }

            // Get the output of the run
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line1;
            while ((line1 = reader1.readLine()) != null) {
                output.append(line1).append("\n");
            }
            student.setCompiled(isCompiled);
            student.setIsRan(isRan);
            student.setOutput(output.toString());

            System.out.println("Final Output in runSCode: [" + output.toString() + "]");
            System.out.println("Compiled: " + isCompiled + " | Ran: " + isRan);



            return student;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void saveToCsv(FileWriter writer, String studentId, boolean hasPassed) {
        try  {
            // Write CSV records
            writer.append(studentId);
            writer.append(",");
            if (hasPassed) {
                writer.append("Success");
            } else {
                writer.append("Failure");
            }
            //writer.append(",");
            //writer.append(studentOutput);
            writer.append("\n");
            writer.flush();

        } catch (IOException e) {
            System.err.println("Error with writing csv: " + e.getMessage());
        }
    }
    protected String getJsonFilePath(String dirPath) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirPath))) {
            for (Path path : stream) {
                if (path.toString().endsWith(".json")) {
                    return path.toString();
                }
            }
        }
        return null;
    }

    public JSONObject getObject(String configFilePath,String objectName) throws IOException {

        String jsonText = new String(Files.readAllBytes(Path.of(configFilePath)));
        JSONObject json = new JSONObject(jsonText);
        return json.getJSONObject(objectName);
    }
    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    @FXML
    protected void handleQuit() {
        System.exit(0);

    }


    @FXML
    protected void handleUserManual() {
        String filePath = "UserManual.txt";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            showPopupWithContent(content.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showPopupWithContent(String content) throws FileNotFoundException {
        Stage popupStage = new Stage();
        popupStage.setTitle("User Manual");
        popupStage.getIcons().add(new Image(new FileInputStream("img.png")));

        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setEditable(false);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 750, 600);
        popupStage.setScene(scene);
        popupStage.show();
    }

    protected void moveToDir(File file, String newDestDir) {
        if (file == null || !file.exists()) {
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
            File configJson = buildConfigFile(configFileName, language, arguments, expectedOutput);
            File configFilesDir = new File(System.getProperty("user.home"), "Documents/ConfigFiles");
            if (!configFilesDir.exists()) configFilesDir.mkdirs();
            File copyTarget = new File(configFilesDir, configJson.getName());
            try {
                Files.copy(configJson.toPath(), copyTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Couldn't copy config to ConfigFiles: " + e.getMessage());
            }
            moveToDir(configJson, targetDir + File.separator + name);

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
