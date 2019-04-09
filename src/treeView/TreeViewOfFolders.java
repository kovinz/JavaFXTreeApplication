package treeView;

import java.io.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TreeViewOfFolders extends Application {

  private TreeView<File> treeView;
  private TabPane tabPane;
  private String stringToFind;
  private String directory;
  private String typeOfFiles;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle("TreeView");
    stage.setWidth(900);
    stage.setHeight(650);

    VBox vbox = new VBox();
    vbox.setPadding(new Insets(20));

    HBox hboxForViews = new HBox();
    hboxForViews.setPadding(new Insets(20));

    HBox hboxForInputs = new HBox();
    hboxForInputs.setPadding(new Insets(20));

    tabPane = new TabPane();
    tabPane.setPrefSize(400, 400);


    treeView = new TreeView<>();
    treeView.setShowRoot(false);
    treeView.setPrefSize(400, 400);

    treeView.setCellFactory(treeView -> new TreeCell<>() {
      @Override
      public void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (empty) {
          setText(null);
        } else {
          setText(file.getName());
        }
      }
    });

    Label labelDirectory = new Label("Directory:");
    TextField fieldDirectory = new TextField();

    Label labelStringToFind = new Label("String to find:");
    TextField fieldStringToFind = new TextField();

    Label labelTypeOfFiles = new Label("Type of files:");
    TextField fieldTypeOfFiles = new TextField();

    Button buttonOpen = new Button("Open");
    buttonOpen.setOnAction(event -> {
      File file = treeView.getSelectionModel().getSelectedItem().getValue();
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        TextArea textArea = new TextArea();
        textArea.setPrefSize(400, 400);

        Tab tab = new Tab();
        tab.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("tabIcon.png"))));
        tab.setContent(textArea);
        tabPane.getTabs().add(tab);

        String line;
        while ((line = reader.readLine()) != null) {
          textArea.appendText(line + "\n");
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });

    Button buttonSubmit = new Button("Submit");
    buttonSubmit.setOnAction(event -> {
      directory = fieldDirectory.getText();
      if (directory.equals("")) {
        directory = System.getProperty("user.dir");
      }

      typeOfFiles = fieldTypeOfFiles.getText();
      if (typeOfFiles.equals("")) {
        typeOfFiles = ".*.log";
      }

      stringToFind = fieldStringToFind.getText();

      File rootFile = new File(directory);
      TreeItem<File> root = new TreeItem<>(rootFile);
      root.setExpanded(true);
      File[] files = rootFile.listFiles();

      createTree(files, root);

      TreeItem<File> filteredRoot = new TreeItem<>(rootFile);
      createFilteredTree(root, filteredRoot);
      treeView.setRoot(filteredRoot);
    });

    hboxForViews.getChildren().addAll(treeView, tabPane);
    hboxForViews.setSpacing(10);

    hboxForInputs.getChildren().addAll(labelDirectory, fieldDirectory,
            labelStringToFind, fieldStringToFind, labelTypeOfFiles, fieldTypeOfFiles);
    hboxForInputs.setSpacing(10);

    vbox.getChildren().addAll(hboxForInputs, buttonSubmit, hboxForViews, buttonOpen);
    vbox.setSpacing(10);

    Scene scene = new Scene(vbox);

    stage.setScene(scene);
    stage.show();
  }

  private void createTree(File[] files, TreeItem<File> root) {
    for (File file : files) {
      if (file.isDirectory()) {
        TreeItem<File> newItem = new TreeItem<>(file);
        root.getChildren().add(newItem);
        createTree(file.listFiles(), newItem);
      } else {
        if (file.getName().matches(typeOfFiles)) {
          try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (reader.lines().anyMatch(string -> string.contains(stringToFind))) {
              TreeItem<File> newItem = new TreeItem<>(file);
              root.getChildren().add(newItem);
            }
          } catch (IOException ex) {
            ex.getStackTrace();
          }
        }
      }
    }
  }

  private void createFilteredTree(TreeItem<File> root, TreeItem<File> filteredRoot) {
    for (TreeItem<File> child : root.getChildren()) {
      TreeItem<File> filteredChild = new TreeItem<>(child.getValue());
      filteredChild.setExpanded(true);

      createFilteredTree(child, filteredChild);

      if (!filteredChild.getChildren().isEmpty() || filteredChild.getValue().getName().matches(typeOfFiles)) {
        filteredRoot.getChildren().add(filteredChild);
      }
    }
  }
}