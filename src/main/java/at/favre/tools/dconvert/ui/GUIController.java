/*
 *  Copyright 2016 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.favre.tools.dconvert.ui;

import at.favre.tools.dconvert.DConvert;
import at.favre.tools.dconvert.arg.*;
import at.favre.tools.dconvert.converters.postprocessing.MozJpegProcessor;
import at.favre.tools.dconvert.converters.postprocessing.PngCrushProcessor;
import at.favre.tools.dconvert.converters.postprocessing.WebpProcessor;
import at.favre.tools.dconvert.exceptions.InvalidArgumentException;
import at.favre.tools.dconvert.util.MiscUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * JavaFx main controller for GUI
 */
public class GUIController {
    public VBox vboxOptionsCheckboxes;
    public VBox vboxPostProcessors;
    public Label labelRounding;
    public Label labelThreads;
    public HBox hboxWhy;
    public VBox vboxFillFreeSpace;
    public CheckBox cbCleanBeforeConvert;
    public Label labelDownScale;
    public ChoiceBox choiceDownScale;
    public Label labelUpScale;
    public ChoiceBox choiceUpScale;
    private IPreferenceStore preferenceStore;
    private final FileChooser srcFileChooser = new FileChooser();
    private final DirectoryChooser srcDirectoryChooser = new DirectoryChooser();
    private ResourceBundle bundle;
    public Label labelPostProcessor;
    public TextField textFieldSrcPath;
    public Button btnSrcFile;
    public Button btnSrcFolder;
    public ProgressBar progressBar;
    public Button btnConvert;
    public TextField textFieldDstPath;
    public Button btnDstFolder;
    public ChoiceBox choiceCompression;
    public ChoiceBox choiceCompressionQuality;
    public ChoiceBox choiceRounding;
    public ChoiceBox choiceThreads;
    public CheckBox cbSkipExisting;
    public CheckBox cbSkipUpscaling;
    public CheckBox cbAndroidIncludeLdpiTvdpi;
    public CheckBox cbHaltOnError;
    public CheckBox cbEnablePngCrush;
    public CheckBox cbEnableMozJpeg;
    public CheckBox cbKeepUnoptimized;
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
    public Label labelDpWidth;
    public Label labelDpHeight;
    public ToggleGroup scaleTypeToggleGroup;
    public RadioButton rbFactor;
    public RadioButton rbDpWidth;
    public RadioButton rbDpHeight;
    public GridPane gridPaneScaleFactorLabel;
    public Label labelDpPostFix;
    public CheckBox cbDryRun;
    public GridPane rootGridPane;
    public Label labelWhyPP;
    public Button btnDstFolderOpen;
    public CheckBox cbIosCreateImageset;
    public GridPane gridPaneToggleGroup;
    public ToggleButton tgAndroid;
    public ToggleButton tgIos;
    public ToggleButton tgWindows;
    public ToggleButton tgWeb;
    public RadioButton rbOptSimple;
    public RadioButton rbOptAdvanced;
    public ToggleGroup optionTypeToggleGroup;

    public void onCreate(final Stage stage, IPreferenceStore store, ResourceBundle bundle) {
        this.bundle = bundle;
        this.preferenceStore = store;

        setupLayout();

        btnSrcFile.setOnAction(event -> {
            srcFileChooser.setTitle(bundle.getString("main.filechooser.titel"));
            File file = new File(textFieldSrcPath.getText());
            if (file != null && file.isFile()) {
                file = file.getParentFile();
            }

            if (file == null || textFieldSrcPath.getText().isEmpty() || !file.exists() || !file.isDirectory()) {
                srcFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            } else {
                srcFileChooser.setInitialDirectory(file);
            }
            srcFileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.psd", "*.svg"));
            File srcFile = srcFileChooser.showOpenDialog(btnSrcFile.getScene().getWindow());
            if (srcFile != null) {
                textFieldSrcPath.setText(srcFile.getAbsolutePath());
                if ((textFieldDstPath.getText() == null || textFieldDstPath.getText().trim().isEmpty())) {
                    textFieldDstPath.setText(srcFile.getParentFile().getAbsolutePath());
                }
            }
        });

        btnSrcFolder.setOnAction(new FolderPicker(srcDirectoryChooser, textFieldSrcPath, textFieldDstPath, bundle));
        btnDstFolder.setOnAction(new FolderPicker(srcDirectoryChooser, textFieldDstPath, null, bundle));
        btnConvert.setOnAction(event -> {
            WinTaskbarProgress winTaskbarProgress = new WinTaskbarProgress();
            try {
                Arguments arg = getFromUI(false);
                saveToPrefs(arg);
                btnConvert.setDisable(true);
                labelResult.setText("");
                textFieldConsole.setText("");
                textFieldConsole.setDisable(true);
                progressBar.setProgress(0);
                btnConvert.setText("");

                new DConvert().execute(arg, false, new DConvert.HandlerCallback() {
                    @Override
                    public void onProgress(float progress) {
                        Platform.runLater(() -> {
                            progressBar.setProgress(progress);
                            winTaskbarProgress.updateProgress(progress);
                        });
                    }

                    @Override
                    public void onFinished(int finishedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log) {
                        Platform.runLater(() -> {
                            resetUIAfterExecution();
                            labelResult.setText(
                                    MessageFormat.format(bundle.getString("main.label.finish"), finishedJobs, exceptions.size(), MiscUtil.duration(time)));
                            textFieldConsole.setText(log);
                            textFieldConsole.appendText("");

                            if (!exceptions.isEmpty()) {
                                winTaskbarProgress.error();
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle(bundle.getString("main.alert.title"));
                                alert.setHeaderText(null);
                                alert.setContentText(MessageFormat.format(bundle.getString("main.alert.content"), exceptions.size()));
                                alert.showAndWait();
                            } else {
                                winTaskbarProgress.finish();
                            }
                        });

                    }
                });
            } catch (Exception e) {
                resetUIAfterExecution();
                String stacktrace = MiscUtil.getStackTrace(e);
                labelResult.setText("Error: " + e.getClass().getSimpleName());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(e.getClass().getSimpleName());
                alert.setHeaderText(e.getMessage());
                alert.setContentText(stacktrace.length() > 600 ? stacktrace.substring(0, 600) + "..." : stacktrace);
                alert.showAndWait();
            }
        });

        btnDstFolder.setGraphic(new ImageView(new Image("img/folder-symbol.png")));
        btnSrcFolder.setGraphic(new ImageView(new Image("img/folder-symbol.png")));
        btnSrcFile.setGraphic(new ImageView(new Image("img/file-symbol.png")));
        btnDstFolderOpen.setGraphic(new ImageView(new Image("img/eye.png")));

        btnReset.setOnAction(event -> {
            saveToPrefs(new Arguments());
            loadPrefs();
        });

        scaleTypeToggleGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            scaleSlider.setVisible(!rbDpWidth.isSelected() && !rbDpHeight.isSelected());
            gridPaneScaleFactorLabel.setVisible(!rbDpWidth.isSelected() && !rbDpHeight.isSelected());
            textFieldDp.setVisible(rbDpWidth.isSelected() || rbDpHeight.isSelected());
            labelDpPostFix.setVisible(rbDpWidth.isSelected() || rbDpHeight.isSelected());
            labelDpWidth.setVisible(rbDpWidth.isSelected());
            labelDpHeight.setVisible(rbDpHeight.isSelected());
        });

        optionTypeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (rbOptAdvanced.isSelected()) {
                if (stage.getHeight() < GUI.MIN_HEIGHT) {
                    stage.setHeight(GUI.MIN_HEIGHT);
                }
            }
        });

        gridPaneOptionsCheckboxes.managedProperty().bind(rbOptAdvanced.selectedProperty());
        gridPaneOptionsCheckboxes.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        gridPanePostProcessors.managedProperty().bind(rbOptAdvanced.selectedProperty());
        gridPanePostProcessors.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        labelPostProcessor.managedProperty().bind(rbOptAdvanced.selectedProperty());
        labelPostProcessor.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        hboxWhy.managedProperty().bind(rbOptAdvanced.selectedProperty());
        hboxWhy.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        vboxOptionsCheckboxes.managedProperty().bind(rbOptAdvanced.selectedProperty());
        vboxOptionsCheckboxes.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        vboxPostProcessors.managedProperty().bind(rbOptAdvanced.selectedProperty());
        vboxPostProcessors.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        gridPaneChoiceBoxes.managedProperty().bind(rbOptAdvanced.selectedProperty());
        gridPaneChoiceBoxes.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        textFieldConsole.managedProperty().bind(rbOptAdvanced.selectedProperty());
        textFieldConsole.visibleProperty().bind(rbOptAdvanced.selectedProperty());
        vboxFillFreeSpace.managedProperty().bind(rbOptSimple.selectedProperty());
        vboxFillFreeSpace.visibleProperty().bind(rbOptSimple.selectedProperty());

        scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            labelScale.setText(
                    MessageFormat.format(bundle.getString("main.label.factor"), String.format(Locale.US, "%.2f", Math.round(scaleSlider.getValue() * 4f) / 4f)));
            labelScaleSubtitle.setText(getNameForScale((float) scaleSlider.getValue()));
        });
        scaleSlider.setValue(Arguments.DEFAULT_SCALE);

        setPlatformToogles(Arguments.DEFAULT_PLATFORM);
        choiceCompression.setConverter(new StringConverter() {
            @Override
            public String toString(Object object) {
                return bundle.getString(((EOutputCompressionMode) object).rbKey);
            }

            @Override
            public Object fromString(String string) {
                return EOutputCompressionMode.getFromString(string, bundle);
            }
        });
        choiceCompression.setItems(FXCollections.observableArrayList(
                EOutputCompressionMode.SAME_AS_INPUT_PREF_PNG, EOutputCompressionMode.SAME_AS_INPUT_STRICT, new Separator(), EOutputCompressionMode.AS_JPG,
                EOutputCompressionMode.AS_PNG, EOutputCompressionMode.AS_GIF, EOutputCompressionMode.AS_BMP, EOutputCompressionMode.AS_JPG_AND_PNG));
        choiceCompression.getSelectionModel().select(Arguments.DEFAULT_OUT_COMPRESSION);

        choiceCompressionQuality.setItems(FXCollections.observableArrayList(
                toJpgQ(0f), toJpgQ(0.1f), toJpgQ(0.2f), toJpgQ(0.3f), toJpgQ(0.4f), toJpgQ(0.5f), toJpgQ(0.6f), toJpgQ(0.7f), toJpgQ(0.75f), toJpgQ(0.8f), toJpgQ(0.85f), toJpgQ(0.9f), toJpgQ(0.95f), toJpgQ(1.0f)));
        choiceCompressionQuality.getSelectionModel().select(toJpgQ(Float.valueOf(Arguments.DEFAULT_COMPRESSION_QUALITY)));

        choiceRounding.setItems(FXCollections.observableArrayList(
                RoundingHandler.Strategy.ROUND_HALF_UP, RoundingHandler.Strategy.CEIL, RoundingHandler.Strategy.FLOOR));
        choiceRounding.getSelectionModel().select(Arguments.DEFAULT_ROUNDING_STRATEGY);

        choiceThreads.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6, 7, 8));
        choiceThreads.getSelectionModel().select(Arguments.DEFAULT_THREAD_COUNT - 1);

        choiceDownScale.setItems(FXCollections.observableArrayList(EScalingAlgorithm.getForType(EScalingAlgorithm.Type.DOWNSCALING)));
        choiceDownScale.getSelectionModel().select(Arguments.DEFAULT_DOWNSCALING_QUALITY);

        choiceUpScale.setItems(FXCollections.observableArrayList(EScalingAlgorithm.getForType(EScalingAlgorithm.Type.UPSCALING)));
        choiceUpScale.getSelectionModel().select(Arguments.DEFAULT_UPSCALING_QUALITY);

        labelVersion.setText("v" + GUIController.class.getPackage().getImplementationVersion());

        textFieldDp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textFieldDp.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (textFieldDp.getText().length() > 10) {
                String s = textFieldDp.getText().substring(0, 10);
                textFieldDp.setText(s);
            }
        });

        labelWhyPP.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(bundle.getString("alert.whypp.title"));
            alert.setContentText(bundle.getString("alert.whypp.text"));
            alert.showAndWait();
        });

        btnDstFolderOpen.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(new File(textFieldDstPath.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        textFieldDstPath.textProperty().addListener(observable -> {
            if (textFieldDstPath != null) {
                File dstFolder = new File(textFieldDstPath.getText());
                if (dstFolder.exists() && dstFolder.isDirectory()) {
                    btnDstFolderOpen.setDisable(false);
                    return;
                }
            }
            btnDstFolderOpen.setDisable(true);
        });

        loadPrefs();
        new Thread(new PostProcessorChecker()).start();
    }

    public static String toJpgQ(Float floatQuality) {
        return String.format(Locale.US, "%.0f", floatQuality * 100f) + "%";
    }

    private void setPlatformToogles(Set<EPlatform> platformSet) {
        tgAndroid.setSelected(platformSet.contains(EPlatform.ANDROID));
        tgIos.setSelected(platformSet.contains(EPlatform.IOS));
        tgWindows.setSelected(platformSet.contains(EPlatform.WINDOWS));
        tgWeb.setSelected(platformSet.contains(EPlatform.WEB));
    }

    private void setupLayout() {
        ColumnConstraints column1M = new ColumnConstraints();
        column1M.setPercentWidth(20);
        ColumnConstraints column2M = new ColumnConstraints();
        column2M.setPercentWidth(56);
        ColumnConstraints column3M = new ColumnConstraints();
        column3M.setPercentWidth(12);
        ColumnConstraints column4M = new ColumnConstraints();
        column4M.setPercentWidth(12);
        rootGridPane.getColumnConstraints().addAll(column1M, column2M, column3M, column4M);

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

        ColumnConstraints column1D = new ColumnConstraints();
        column1D.setPercentWidth(25);
        ColumnConstraints column2D = new ColumnConstraints();
        column2D.setPercentWidth(25);
        ColumnConstraints column3D = new ColumnConstraints();
        column3D.setPercentWidth(25);
        ColumnConstraints column4D = new ColumnConstraints();
        column4D.setPercentWidth(25);
        gridPaneToggleGroup.getColumnConstraints().addAll(column1D, column2D, column3D, column4D);
    }

    private void saveToPrefs(Arguments arg) {
        preferenceStore.save(arg);
    }

    private void loadPrefs() {
        Arguments args = preferenceStore.get();
        if (args != null) {
            textFieldSrcPath.setText(args.src != null ? args.src.getAbsolutePath() : "");
            textFieldDstPath.setText(args.dst != null ? args.dst.getAbsolutePath() : "");

            scaleSlider.setValue(Arguments.DEFAULT_SCALE);
            textFieldDp.setText(String.valueOf((int) args.scale));

            if (args.scaleMode == EScaleMode.FACTOR) {
                rbFactor.setSelected(true);
                scaleSlider.setValue(args.scale);
                textFieldDp.setText("");
            } else if (args.scaleMode == EScaleMode.DP_WIDTH) {
                rbDpWidth.setSelected(true);
            } else if (args.scaleMode == EScaleMode.DP_HEIGHT) {
                rbDpHeight.setSelected(true);
            }

            setPlatformToogles(args.platform);
            choiceCompression.getSelectionModel().select(args.outputCompressionMode);
            choiceCompressionQuality.getSelectionModel().select(toJpgQ(args.compressionQuality));
            choiceRounding.getSelectionModel().select(args.roundingHandler);
            choiceThreads.getSelectionModel().select(Integer.valueOf(args.threadCount));
            choiceDownScale.getSelectionModel().select(args.downScalingAlgorithm);
            choiceUpScale.getSelectionModel().select(args.upScalingAlgorithm);

            cbSkipExisting.setSelected(args.skipExistingFiles);
            cbSkipUpscaling.setSelected(args.skipUpscaling);
            cbAndroidIncludeLdpiTvdpi.setSelected(args.includeAndroidLdpiTvdpi);
            cbAntiAliasing.setSelected(args.enableAntiAliasing);
            cbMipmapInsteadDrawable.setSelected(args.createMipMapInsteadOfDrawableDir);
            cbHaltOnError.setSelected(args.haltOnError);
            cbDryRun.setSelected(args.dryRun);
            cbEnablePngCrush.setSelected(args.enablePngCrush);
            cbPostConvertWebp.setSelected(args.postConvertWebp);
            cbEnableMozJpeg.setSelected(args.enableMozJpeg);
            cbKeepUnoptimized.setSelected(args.keepUnoptimizedFilesPostProcessor);
            cbIosCreateImageset.setSelected(args.iosCreateImagesetFolders);
            cbCleanBeforeConvert.setSelected(args.clearDirBeforeConvert);
            rbOptAdvanced.setSelected(args.guiAdvancedOptions);
            rbOptSimple.setSelected(!args.guiAdvancedOptions);

        }
    }

    public Arguments getFromUI(boolean skipValidation) throws InvalidArgumentException {
        float scale = Arguments.DEFAULT_SCALE;

        try {
            scale = rbFactor.isSelected() ? (float) scaleSlider.getValue() : Float.valueOf(textFieldDp.getText());
        } catch (NumberFormatException e) {
            if (!skipValidation) {
                throw new InvalidArgumentException(
                        MessageFormat.format(bundle.getString("error.parse.dp"), textFieldDp.getText()));
            }
        }

        Set<EPlatform> platformSet = new HashSet<>();
        if (tgAndroid.isSelected()) {
            platformSet.add(EPlatform.ANDROID);
        }
        if (tgIos.isSelected()) {
            platformSet.add(EPlatform.IOS);
        }
        if (tgWindows.isSelected()) {
            platformSet.add(EPlatform.WINDOWS);
        }
        if (tgWeb.isSelected()) {
            platformSet.add(EPlatform.WEB);
        }

        Arguments.Builder builder = new Arguments.Builder(new File(textFieldSrcPath.getText()), scale);
        builder.dstFolder(textFieldDstPath.getText() != null && !textFieldDstPath.getText().trim().isEmpty() ? new File(textFieldDstPath.getText()) : null);
        builder.scaleMode(rbFactor.isSelected() ? EScaleMode.FACTOR : rbDpWidth.isSelected() ? EScaleMode.DP_WIDTH : EScaleMode.DP_HEIGHT);
        builder.platform(platformSet);
        builder.compression((EOutputCompressionMode) choiceCompression.getSelectionModel().getSelectedItem(), toJpgQFloat(choiceCompressionQuality.getSelectionModel().getSelectedItem()));
        builder.scaleRoundingStragy((RoundingHandler.Strategy) choiceRounding.getSelectionModel().getSelectedItem());
        builder.threadCount((Integer) choiceThreads.getSelectionModel().getSelectedItem());
        builder.downScaleAlgorithm((EScalingAlgorithm) choiceDownScale.getSelectionModel().getSelectedItem());
        builder.upScaleAlgorithm((EScalingAlgorithm) choiceUpScale.getSelectionModel().getSelectedItem());

        builder.skipExistingFiles(cbSkipExisting.isSelected());
        builder.skipUpscaling(cbSkipUpscaling.isSelected());
        builder.verboseLog(true);
        builder.includeAndroidLdpiTvdpi(cbAndroidIncludeLdpiTvdpi.isSelected());
        builder.haltOnError(cbHaltOnError.isSelected());
        builder.createMipMapInsteadOfDrawableDir(cbMipmapInsteadDrawable.isSelected());
        builder.antiAliasing(cbAntiAliasing.isSelected());
        builder.dryRun(cbDryRun.isSelected());
        builder.enablePngCrush(cbEnablePngCrush.isSelected());
        builder.postConvertWebp(cbPostConvertWebp.isSelected());
        builder.enableMozJpeg(cbEnableMozJpeg.isSelected());
        builder.keepUnoptimizedFilesPostProcessor(cbKeepUnoptimized.isSelected());
        builder.iosCreateImagesetFolders(cbIosCreateImageset.isSelected());
        builder.guiAdvancedOptions(rbOptAdvanced.isSelected());
        builder.clearDirBeforeConvert(cbCleanBeforeConvert.isSelected());

        return builder.skipParamValidation(skipValidation).build();
    }

    private float toJpgQFloat(Object selectedItem) {
        String raw = selectedItem.toString();
        raw = raw.replace("%", "");
        int rawInt = Integer.parseInt(raw);
        return (float) rawInt / 100f;
    }

    private void resetUIAfterExecution() {
        progressBar.setProgress(0);
        btnConvert.setDisable(false);
        textFieldConsole.setDisable(false);
        btnConvert.setText(bundle.getString("main.btn.convert"));
    }

    public void setSrcForTest(File srcFile) {
        if (srcFile != null) {
            textFieldSrcPath.setText(srcFile.getAbsolutePath());
            if (textFieldDstPath != null && (textFieldDstPath.getText() == null || textFieldDstPath.getText().trim().isEmpty())) {
                textFieldDstPath.setText(srcFile.getAbsolutePath());
            }
        }
    }

    private static String getNameForScale(float scale) {
        String scaleString = String.format(Locale.US, "%.2f", Math.round(scale * 4f) / 4f);
        switch (scaleString) {
            case "0.75":
                return "ldpi / " + getDpi(scaleString);
            case "1.00":
                return "mdpi / 1x / " + getDpi(scaleString);
            case "1.50":
                return "hdpi / " + getDpi(scaleString);
            case "2.00":
                return "xhdpi / 2x / " + getDpi(scaleString);
            case "3.00":
                return "xxhdpi / 3x / " + getDpi(scaleString);
            case "4.00":
                return "xxxhdpi / " + getDpi(scaleString);
            default:
                return getDpi(scaleString);
        }
    }

    private static String getDpi(String scaleString) {
        return String.format(Locale.US, "%.0f", Float.valueOf(scaleString) * 160) + "dpi";
    }

    private static class FolderPicker implements EventHandler<ActionEvent> {
        private final DirectoryChooser directoryChooser;
        private final TextField textFieldPath;
        private final TextField dstTextFieldPath;
        private final ResourceBundle bundle;

        public FolderPicker(DirectoryChooser directoryChooser, TextField textFieldPath, TextField dstTextFieldPath, ResourceBundle bundle) {
            this.directoryChooser = directoryChooser;
            this.textFieldPath = textFieldPath;
            this.dstTextFieldPath = dstTextFieldPath;
            this.bundle = bundle;
        }

        @Override
        public void handle(ActionEvent event) {
            directoryChooser.setTitle(bundle.getString("main.dirchooser.titel"));
            File dir = new File(textFieldPath.getText());

            if (dir != null && dir.isFile()) {
                dir = dir.getParentFile();
            }

            while (true) {
                if (dir == null || dir.isDirectory()) break;
                if (!dir.exists()) {
                    dir = dir.getParentFile();
                }
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

    private class PostProcessorChecker implements Runnable {
        @Override
        public void run() {
            boolean pngcrushSupported = new PngCrushProcessor().isSupported();
            boolean mozJpegSupported = new MozJpegProcessor().isSupported();
            boolean webpSupported = new WebpProcessor().isSupported();

            Platform.runLater(() -> {
                cbEnablePngCrush.setDisable(!pngcrushSupported);
                cbEnableMozJpeg.setDisable(!mozJpegSupported);
                cbPostConvertWebp.setDisable(!webpSupported);
                labelWhyPP.setVisible(!pngcrushSupported || !mozJpegSupported || !webpSupported);
            });
        }
    }

}
