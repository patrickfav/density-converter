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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main GUI Class
 */
public class GUI extends Application {
    static int MIN_HEIGHT = 860;

    public void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setup(primaryStage, new SerializePreferenceStore(), Toolkit.getDefaultToolkit().getScreenSize());
        primaryStage.show();
    }

    public static GUIController setup(Stage primaryStage, IPreferenceStore store, Dimension screenSize) throws IOException {
        primaryStage.setTitle("Density Converter");

        ResourceBundle bundle = ResourceBundle.getBundle("bundles.strings", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(GUI.class.getClassLoader().getResource("main.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        GUIController controller = loader.getController();
        controller.onCreate(primaryStage, store, bundle);

        if (screenSize.getHeight() <= 768) {
            MIN_HEIGHT = 740;
        }

        Scene scene = new Scene(root, 600, MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
        primaryStage.getIcons().add(new Image("img/density_converter_icon_16.png"));
        primaryStage.getIcons().add(new Image("img/density_converter_icon_24.png"));
        primaryStage.getIcons().add(new Image("img/density_converter_icon_48.png"));
        primaryStage.getIcons().add(new Image("img/density_converter_icon_64.png"));
        primaryStage.getIcons().add(new Image("img/density_converter_icon_128.png"));
        primaryStage.getIcons().add(new Image("img/density_converter_icon_256.png"));

        return controller;
    }
}
