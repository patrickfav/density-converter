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

		Scene scene = new Scene(root, 550, 800);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("img/density_converter_icon_36.png"));
	}
}
