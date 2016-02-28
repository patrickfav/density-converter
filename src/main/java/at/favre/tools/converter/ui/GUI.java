package at.favre.tools.converter.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main GUI Class
 */
public class GUI extends Application {
	private Stage primaryStage;


	public void launchApp(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		setup();

		primaryStage.show();
	}

	private void setup() throws IOException {
		primaryStage.setTitle("Density Converter");

		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
		Parent root = loader.load();
		GUIController controller = loader.<GUIController>getController();
		controller.onCreate();

		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(20);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(56);
		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(12);
		ColumnConstraints column4 = new ColumnConstraints();
		column4.setPercentWidth(12);
		((GridPane) root).getColumnConstraints().addAll(column1, column2, column3, column4);

		Scene scene = new Scene(root, 500, 600);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("density_converter_icon_48.png"));
	}
}
