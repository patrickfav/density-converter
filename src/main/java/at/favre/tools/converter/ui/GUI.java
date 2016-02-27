package at.favre.tools.converter.ui;

import at.favre.tools.converter.ConverterHandler;
import at.favre.tools.converter.arg.Arguments;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Created by PatrickF on 27.02.2016.
 */
public class GUI extends Application {
	private Stage primaryStage;
	final FileChooser srcFileChooser = new FileChooser();
	final DirectoryChooser srcDirectoryChooser = new DirectoryChooser();

	public void launchApp(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		setup();

		primaryStage.show();
	}

	private void setup() {
		primaryStage.setTitle("Density Converter");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Scene scene = new Scene(grid, 400, 300);
		primaryStage.setScene(scene);


		Text scenetitle = new Text("Welcome");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		Label userName = new Label("Source");
		grid.add(userName, 0, 1);

		TextField srcPath = new TextField();
		srcPath.setDisable(true);
		grid.add(srcPath, 1, 1);

		Button btnSelectSrc = new Button("file");
		btnSelectSrc.setOnAction(event -> {
			srcFileChooser.setTitle("Select Image");
			srcFileChooser.setInitialDirectory(
					new File(System.getProperty("user.home"))
			);
			srcFileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
			File srcFile = srcFileChooser.showOpenDialog(primaryStage);
			if (srcFile != null) {
				srcPath.setText(srcFile.getAbsolutePath());
			}
		});
		grid.add(btnSelectSrc, 2, 1);

		Button btnSelectSrcFolder = new Button("folder");
		btnSelectSrcFolder.setOnAction(event -> {
			srcDirectoryChooser.setTitle("Select Image Folder");
			srcDirectoryChooser.setInitialDirectory(
					new File(System.getProperty("user.home"))
			);
			File srcFile = srcDirectoryChooser.showDialog(primaryStage);
			if (srcFile != null) {
				srcPath.setText(srcFile.getAbsolutePath());
			}
		});
		grid.add(btnSelectSrcFolder, 3, 1);

		final ProgressBar progressBar = new ProgressBar(0);
		progressBar.setDisable(true);
		grid.add(progressBar, 0, 3, 4, 1);

		Button btnMainConvert = new Button("Convert");
		btnMainConvert.setOnAction(event -> {
			btnMainConvert.setDisable(true);
			progressBar.setDisable(true);

			Arguments.Builder builder = new Arguments.Builder(new File(srcPath.getText()), 3f);

			try {
				Arguments arg = builder.build();
				progressBar.setDisable(false);
				new ConverterHandler().execute(arg, new ConverterHandler.HandlerCallback() {
					@Override
					public void onProgress(float progress) {
						System.out.println(progress);
						progressBar.setProgress(progress);
					}

					@Override
					public void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess) {
						btnMainConvert.setDisable(false);
					}
				});
			} catch (InvalidArgumentException e) {
				btnMainConvert.setDisable(false);
			}


		});
		grid.add(btnMainConvert, 0, 7, 4, 1);


	}
}
