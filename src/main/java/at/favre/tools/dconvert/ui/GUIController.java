/*
 * Copyright (C) 2016 Patrick Favre-Bulle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package at.favre.tools.dconvert.ui;

import at.favre.tools.dconvert.ConverterHandler;
import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.util.MiscUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * JavaFx main controller for GUI
 */
public class GUIController {

	private final PreferenceStore preferenceStore = new PreferenceStore();
	private final FileChooser srcFileChooser = new FileChooser();
	private final DirectoryChooser srcDirectoryChooser = new DirectoryChooser();

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
	public CheckBox cbAndroidIncludeLdpiTvdpi;
	public CheckBox cbHaltOnError;
	public CheckBox chEnablePngCrush;
	public Slider scaleSlider;
	public Label labelScale;
	public Label labelResult;
	public TextArea textFieldConsole;
	public CheckBox cbPostConvertWebp;
	public CheckBox cbMipmapInsteadDrawable;
	public Label labelVersion;
	public GridPane gridPaneChoiceBoxes;
	public GridPane gridPanePostProcessors;
	public GridPane gridPaneOptionsCheckboxes;
	public Label labelScaleSubtitle;
	public CheckBox cbAntiAliasing;
	public Button btnReset;
	public TextField textFieldDp;
	public Label labelDp;
	public ToggleGroup scaleTypeToggleGroup;
	public RadioButton rbFactor;
	public RadioButton rbDp;
	public GridPane gridPaneScaleFactorLabel;
	public Label labelDpPostFix;

	public void onCreate() {

		btnSrcFile.setOnAction(event -> {
			srcFileChooser.setTitle("Select Image");
			File file = new File(textFieldSrcPath.getText());
			if (file != null && file.isFile()) {
				file = file.getParentFile();
			}

			if (file == null || textFieldSrcPath.getText().isEmpty() || !file.exists() || !file.isDirectory()) {
				srcFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			} else {
				srcFileChooser.setInitialDirectory(file);
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
				saveToPrefs(arg);

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
							resetUIAfterExecution();
							labelResult.setText("Finished Jobs: " + finsihedJobs + " / Errors: " + exceptions.size() + " / Duration: " + MiscUtil.duration(time));
							textFieldConsole.setText(log);
							textFieldConsole.appendText("");

							if (!exceptions.isEmpty()) {
								Alert alert = new Alert(Alert.AlertType.WARNING);
								alert.setTitle("Errors during exection");
								alert.setHeaderText(null);
								alert.setContentText("There were " + exceptions.size() + " errors while execution. See log for details.");
								alert.showAndWait();
							}
						});

					}
				}, false);
			} catch (Exception e) {
				resetUIAfterExecution();
				labelResult.setText("Error: " + e.getClass().getSimpleName());
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle(e.getClass().getSimpleName());
				alert.setHeaderText(e.getMessage());
				alert.setContentText(MiscUtil.getStackTrace(e));
				alert.showAndWait();
			}
		});

		btnDstFolder.setGraphic(new ImageView(new Image("img/folder-symbol.png")));
		btnSrcFolder.setGraphic(new ImageView(new Image("img/folder-symbol.png")));
		btnSrcFile.setGraphic(new ImageView(new Image("img/file-symbol.png")));

		btnReset.setOnAction(event -> {
			saveToPrefs(new Arguments());
			loadPrefs();
		});

		scaleTypeToggleGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
			scaleSlider.setVisible(!rbDp.isSelected());
			gridPaneScaleFactorLabel.setVisible(!rbDp.isSelected());
			textFieldDp.setVisible(rbDp.isSelected());
			labelDpPostFix.setVisible(rbDp.isSelected());
			labelDp.setVisible(rbDp.isSelected());
		});

		scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			labelScale.setText("Factor (x" + String.format(Locale.US, "%.2f", Math.round(scaleSlider.getValue() * 4f) / 4f) + ")");
			labelScaleSubtitle.setText(getNameForScale((float) scaleSlider.getValue()));
		});
		scaleSlider.setValue(Arguments.DEFAULT_SCALE);

		choicePlatform.setItems(FXCollections.observableArrayList(
				EPlatform.ALL, new Separator(), EPlatform.ANROID, EPlatform.IOS));
		choicePlatform.getSelectionModel().select(Arguments.DEFAULT_PLATFORM);

		choiceCompression.setItems(FXCollections.observableArrayList(
				EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, EOutputCompressionMode.SAME_AS_INPUT_STRICT, new Separator(), EOutputCompressionMode.AS_JPG,
				EOutputCompressionMode.AS_PNG, EOutputCompressionMode.AS_GIF, EOutputCompressionMode.AS_BMP, EOutputCompressionMode.AS_JPG_AND_PNG));
		choiceCompression.getSelectionModel().select(Arguments.DEFAULT_OUT_COMPRESSION);

		choiceCompressionQuality.setItems(FXCollections.observableArrayList(
				0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f));
		choiceCompressionQuality.getSelectionModel().select((int) (Arguments.DEFAULT_COMPRESSION_QUALITY * 10f));

		choiceRounding.setItems(FXCollections.observableArrayList(
				RoundingHandler.Strategy.ROUND_HALF_UP, RoundingHandler.Strategy.CEIL, RoundingHandler.Strategy.FLOOR));
		choiceRounding.getSelectionModel().select(Arguments.DEFAULT_ROUNDING_STRATEGY);

		choiceThreads.setItems(FXCollections.observableArrayList(
				1, 2, 3, 4, 5, 6, 7, 8));
		choiceThreads.getSelectionModel().select(Arguments.DEFAULT_THREAD_COUNT - 1);

		cbVerboseLog.selectedProperty().addListener((observable, oldValue, newValue) -> {
			textFieldConsole.setVisible(cbVerboseLog.isSelected());
		});
		labelVersion.setText("v" + GUIController.class.getPackage().getImplementationVersion());

		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(20);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(30);
		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(20);
		ColumnConstraints column4 = new ColumnConstraints();
		column4.setPercentWidth(30);
		gridPaneChoiceBoxes.getColumnConstraints().addAll(column1, column2, column3, column4);

		ColumnConstraints column1C = new ColumnConstraints();
		column1C.setPercentWidth(50);
		ColumnConstraints column2C = new ColumnConstraints();
		column2C.setPercentWidth(50);
		gridPaneOptionsCheckboxes.getColumnConstraints().addAll(column1C, column2C);
		gridPanePostProcessors.getColumnConstraints().addAll(column1C, column2C);

		textFieldDp.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				textFieldDp.setText(newValue.replaceAll("[^\\d]", ""));
			}
			if (textFieldDp.getText().length() > 10) {
				String s = textFieldDp.getText().substring(0, 10);
				textFieldDp.setText(s);
			}
		});


		loadPrefs();
	}

	private void saveToPrefs(Arguments arg) {
		preferenceStore.save(arg);
	}

	private void loadPrefs() {
		Arguments args = preferenceStore.get();
		if (args != null) {
			textFieldSrcPath.setText(args.src != null ? args.src.getAbsolutePath() : "");
			textFieldDstPath.setText(args.dst != null ? args.dst.getAbsolutePath() : "");

			if (args.scaleType == EScaleType.FACTOR) {
				rbFactor.setSelected(true);
				scaleSlider.setValue(args.scale);
				textFieldDp.setText("");
			} else {
				rbDp.setSelected(true);
				scaleSlider.setValue(Arguments.DEFAULT_SCALE);
				textFieldDp.setText(String.valueOf((int) args.scale));
			}

			choicePlatform.getSelectionModel().select(args.platform);
			choiceCompression.getSelectionModel().select(args.outputCompressionMode);
			choiceCompressionQuality.getSelectionModel().select(args.compressionQuality);
			choiceRounding.getSelectionModel().select(args.roundingHandler);
			choiceThreads.getSelectionModel().select(Integer.valueOf(args.threadCount));

			cbSkipExisting.setSelected(args.skipExistingFiles);
			cbSkipUpscaling.setSelected(args.skipUpscaling);
			cbVerboseLog.setSelected(args.verboseLog);
			cbAndroidIncludeLdpiTvdpi.setSelected(args.includeAndroidLdpiTvdpi);
			cbAntiAliasing.setSelected(args.enableAntiAliasing);
			cbMipmapInsteadDrawable.setSelected(args.createMipMapInsteadOfDrawableDir);
			cbHaltOnError.setSelected(args.haltOnError);

			chEnablePngCrush.setSelected(args.enablePngCrush);
			cbPostConvertWebp.setSelected(args.postConvertWebp);
		}
	}

	private Arguments getFromUI() throws InvalidArgumentException {
		float scale;
		try {
			scale = rbFactor.isSelected() ? (float) scaleSlider.getValue() : Float.valueOf(textFieldDp.getText());
		} catch (NumberFormatException e) {
			throw new InvalidArgumentException("could not parse dp: " + textFieldDp.getText() + " must be a number");
		}

		Arguments.Builder builder = new Arguments.Builder(new File(textFieldSrcPath.getText()), scale);
		builder.dstFolder(textFieldDstPath.getText() != null && !textFieldDstPath.getText().trim().isEmpty() ? new File(textFieldDstPath.getText()) : null);
		builder.scaleType(rbFactor.isSelected() ? EScaleType.FACTOR : EScaleType.DP);
		builder.platform((EPlatform) choicePlatform.getSelectionModel().getSelectedItem());
		builder.compression((EOutputCompressionMode) choiceCompression.getSelectionModel().getSelectedItem(), (Float) choiceCompressionQuality.getSelectionModel().getSelectedItem());
		builder.scaleRoundingStragy((RoundingHandler.Strategy) choiceRounding.getSelectionModel().getSelectedItem());
		builder.threadCount((Integer) choiceThreads.getSelectionModel().getSelectedItem());

		builder.skipExistingFiles(cbSkipExisting.isSelected());
		builder.skipUpscaling(cbSkipUpscaling.isSelected());
		builder.verboseLog(cbVerboseLog.isSelected());
		builder.includeAndroidLdpiTvdpi(cbAndroidIncludeLdpiTvdpi.isSelected());
		builder.haltOnError(cbHaltOnError.isSelected());
		builder.createMipMapInsteadOfDrawableDir(cbMipmapInsteadDrawable.isSelected());
		builder.antiAliasing(cbAntiAliasing.isSelected());
		builder.enablePngCrush(chEnablePngCrush.isSelected());
		builder.postConvertWebp(cbPostConvertWebp.isSelected());

		return builder.build();
	}

	private void resetUIAfterExecution() {
		progressBar.setProgress(1);
		btnConvert.setDisable(false);
		textFieldConsole.setDisable(false);
	}

	private static String getNameForScale(float scale) {
		String scaleString = String.format(Locale.US, "%.2f", Math.round(scale * 4f) / 4f);
		switch (scaleString) {
			case "0.75":
				return "ldpi";
			case "1.00":
				return "mdpi / 1x";
			case "1.50":
				return "hdpi";
			case "2.00":
				return "xhdpi / 2x";
			case "3.00":
				return "xxhdpi / 3x";
			case "4.00":
				return "xxxhdpi";
		}
		return "";
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

			if (dir != null && dir.isFile()) {
				dir = dir.getParentFile();
			}

			if (textFieldPath.getText().isEmpty() || !dir.exists() || !dir.isDirectory()) {
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			} else {
				directoryChooser.setInitialDirectory(dir);
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
