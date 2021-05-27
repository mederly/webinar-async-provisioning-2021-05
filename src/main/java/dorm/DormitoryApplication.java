package dorm;

import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main class.
 *
 * Responsibilities:
 *
 * - Maintains the data about students
 * - Creates and displays the table of students
 * - Starts the listening for messages
 */
public class DormitoryApplication extends Application {

    private final TableView<Student> table = new TableView<>();

    private final ObservableList<Student> students =
            FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException, CsvValidationException {
        readCsvFile();
        createScene(stage);
        startListener();

        stage.show();
    }

    private void readCsvFile() throws IOException, CsvValidationException {
        boolean exists = CsvFile.INSTANCE.readInto(students);
        if (!exists) {
            CsvFile.INSTANCE.writeFrom(students);
        }
    }

    private void createScene(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Sample Dormitory Application");
        stage.setWidth(1200);
        stage.setHeight(500);

        final Label label = new Label("Registered Students");
        label.setFont(new Font("Arial", 20));

        table.setEditable(false);

        TableColumn<Student, String> loginCol = new TableColumn<>("Login");
        loginCol.setMinWidth(100);
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Student, String> givenNameCol = new TableColumn<>("Given name");
        givenNameCol.setMinWidth(100);
        givenNameCol.setCellValueFactory(new PropertyValueFactory<>("givenName"));

        TableColumn<Student, String> familyNameCol = new TableColumn<>("Family name");
        familyNameCol.setMinWidth(150);
        familyNameCol.setCellValueFactory(new PropertyValueFactory<>("familyName"));

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(300);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Student, String> facultyCol = new TableColumn<>("Faculty");
        facultyCol.setMinWidth(500);
        facultyCol.setCellValueFactory(new PropertyValueFactory<>("faculty"));

        table.setItems(students);
        //noinspection unchecked
        table.getColumns().addAll(loginCol, givenNameCol, familyNameCol, emailCol, facultyCol);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> QueueListener.stopAll());
    }

    private void startListener() {
        QueueListener.start(new UpdatingMessageProcessor(students, table::refresh));
    }
}