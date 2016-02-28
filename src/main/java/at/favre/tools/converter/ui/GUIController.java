package at.favre.tools.converter.ui;

import at.favre.tools.converter.ConverterHandler;
import at.favre.tools.converter.arg.Arguments;
import at.favre.tools.converter.arg.EOutputCompressionMode;
import at.favre.tools.converter.arg.EPlatform;
import at.favre.tools.converter.arg.RoundingHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * JavaFx Controller for main UI
 */
public class GUIController {
	final FileChooser srcFileChooser = new FileChooser();
	final DirectoryChooser srcDirectoryChooser = new DirectoryChooser();

	public TextField textFieldSrcPath;
	public Button btnSrcFile;
	public Button btnSrcFolder;
	public ProgressBar progressBar;
	public Button btnConvert;
	public TextField textFieldDstPath;
	public Button btnDstFolder;

	public ChoiceBox choicePlatform;
	public ChoiceBox choiceCompression;
	public ChoiceBox choiceCompressionQuality;
	public ChoiceBox choiceRounding;
	public ChoiceBox choiceThreads;

	public CheckBox cbSkipExisting;
	public CheckBox cbSkipUpscaling;
	public CheckBox cbVerboseLog;
	public CheckBox cbIncludeObsolete;
	public CheckBox cbHaltOnError;
	public CheckBox chEnablePngCrush;
	public Slider scaleSlider;
	public Label labelScale;
	public Label labelResult;
	public TextArea textFieldConsole;
	public CheckBox cbPostConvertWebp;

	public void onCreate() {
		btnSrcFile.setOnAction(event -> {
			srcFileChooser.setTitle("Select Image");
			File file = new File(btnSrcFile.getText());
			if (btnSrcFile.getText().isEmpty() || !file.exists() || !file.isFile()) {
				srcFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			} else {
				srcFileChooser.setInitialDirectory(new File(btnSrcFile.getText()));
			}
			srcFileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
			File srcFile = srcFileChooser.showOpenDialog(btnSrcFile.getScene().getWindow());
			if (srcFile != null) {
				textFieldSrcPath.setText(srcFile.getAbsolutePath());
				if ((textFieldDstPath.getText() == null || textFieldDstPath.getText().trim().isEmpty())) {
					textFieldDstPath.setText(srcFile.getParentFile().getAbsolutePath());
				}
			}
		});
		btnSrcFolder.setOnAction(new FolderPicker(srcDirectoryChooser, textFieldSrcPath, textFieldDstPath));
		btnDstFolder.setOnAction(new FolderPicker(srcDirectoryChooser, textFieldDstPath, null));
		btnConvert.setOnAction(event -> {

			try {
				Arguments arg = getFromUI();

				btnConvert.setDisable(true);
				progressBar.setDisable(true);
				labelResult.setText("");
				textFieldConsole.setText("");
				textFieldConsole.setDisable(true);
				progressBar.setProgress(0);
				progressBar.setDisable(false);

				new ConverterHandler().execute(arg, new ConverterHandler.HandlerCallback() {
					@Override
					public void onProgress(float progress, String log) {
						Platform.runLater(() -> progressBar.setProgress(progress));
					}

					@Override
					public void onFinished(int finsihedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log) {
						Platform.runLater(() -> {
							progressBar.setProgress(1);
							btnConvert.setDisable(false);
							labelResult.setText("Finished Jobs: " + finsihedJobs + " / Errors: " + exceptions.size() + " / Duration: " + time + "ms");
							textFieldConsole.setDisable(false);
							textFieldConsole.setText(log);
						});

					}
				}, false);
			} catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle(e.getClass().getSimpleName());
				alert.setHeaderText(null);
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		});

		scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			labelScale.setText("Scale (x" + String.format(Locale.US, "%.2f", Math.round(scaleSlider.getValue() * 4f) / 4f) + ")");
		});

		choicePlatform.setItems(FXCollections.observableArrayList(
				EPlatform.ALL, new Separator(), EPlatform.ANROID, EPlatform.IOS));
		choicePlatform.getSelectionModel().selectFirst();

		choiceCompression.setItems(FXCollections.observableArrayList(
				EOutputCompressionMode.SAME_AS_INPUT, new Separator(), EOutputCompressionMode.JPG,
				EOutputCompressionMode.PNG, EOutputCompressionMode.GIF, EOutputCompressionMode.JPG_AND_PNG));
		choiceCompression.getSelectionModel().selectFirst();

		choiceCompressionQuality.setItems(FXCollections.observableArrayList(
				0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f));
		choiceCompressionQuality.getSelectionModel().select((int) (Arguments.DEFAULT_COMPRESSION_QUALITY * 10f));

		choiceRounding.setItems(FXCollections.observableArrayList(
				RoundingHandler.Strategy.ROUND_HALF_UP, RoundingHandler.Strategy.CEIL, RoundingHandler.Strategy.FLOOR));
		choiceRounding.getSelectionModel().selectFirst();

		choiceThreads.setItems(FXCollections.observableArrayList(
				1, 2, 3, 4, 5, 6, 7, 8));
		choiceThreads.getSelectionModel().select(Arguments.DEFAULT_THREAD_COUNT - 1);

		cbVerboseLog.selectedProperty().addListener((observable, oldValue, newValue) -> {
			textFieldConsole.setVisible(cbVerboseLog.isSelected());
			{
			}
		});
	}

	private Arguments getFromUI() throws InvalidArgumentException {
		Arguments.Builder builder = new Arguments.Builder(new File(textFieldSrcPath.getText()), (float) scaleSlider.getValue());
		builder.dstFolder(textFieldDstPath.getText() != null && !textFieldDstPath.getText().trim().isEmpty() ? new File(textFieldDstPath.getText()) : null);

		builder.platform((EPlatform) choicePlatform.getSelectionModel().getSelectedItem());
		builder.compression((EOutputCompressionMode) choiceCompression.getSelectionModel().getSelectedItem(), (Float) choiceCompressionQuality.getSelectionModel().getSelectedItem());
		builder.scaleRoundingStragy((RoundingHandler.Strategy) choiceRounding.getSelectionModel().getSelectedItem());
		builder.threadCount((Integer) choiceThreads.getSelectionModel().getSelectedItem());

		builder.skipExistingFiles(cbSkipExisting.isSelected());
		builder.skipUpscaling(cbSkipUpscaling.isSelected());
		builder.verboseLog(cbVerboseLog.isSelected());
		builder.includeObsoleteFormats(cbIncludeObsolete.isSelected());
		builder.haltOnError(cbHaltOnError.isSelected());
		builder.enablePngCrush(chEnablePngCrush.isSelected());
		builder.postConvertWebp(cbPostConvertWebp.isSelected());

		return builder.build();
	}

	private static class FolderPicker implements EventHandler<ActionEvent> {
		private DirectoryChooser directoryChooser;
		private TextField textFieldPath;
		private TextField dstTextFieldPath;

		public FolderPicker(DirectoryChooser directoryChooser, TextField textFieldPath, TextField dstTextFieldPath) {
			this.directoryChooser = directoryChooser;
			this.textFieldPath = textFieldPath;
			this.dstTextFieldPath = dstTextFieldPath;
		}

		@Override
		public void handle(ActionEvent event) {
			directoryChooser.setTitle("Select Image Folder");
			File dir = new File(textFieldPath.getText());
			if (textFieldPath.getText().isEmpty() || !dir.exists() || !dir.isDirectory()) {
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			} else {
				directoryChooser.setInitialDirectory(new File(textFieldPath.getText()));
			}
			File srcFile = directoryChooser.showDialog(textFieldPath.getScene().getWindow());
			if (srcFile != null) {
				textFieldPath.setText(srcFile.getAbsolutePath());
				if (dstTextFieldPath != null && (dstTextFieldPath.getText() == null || dstTextFieldPath.getText().trim().isEmpty())) {
					dstTextFieldPath.setText(srcFile.getAbsolutePath());
				}
			}
		}
	}


}
