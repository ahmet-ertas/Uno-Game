package com.group12.uno;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.group12.uno.controller.LoginController;

public class UnoApp extends Application {

	private static Stage primaryStage;

	@Override
	public void start(Stage stage) {
		primaryStage = stage;
		showLoginScene();
	}

	public static void showLoginScene() {
		LoginController loginController = new LoginController();
		Scene loginScene = new Scene(loginController.getView(), 450, 550);
		primaryStage.setScene(loginScene);
		primaryStage.setTitle("UNO Game - Login");
		primaryStage.show();
	}

	public static void changeScene(Scene newScene, String title) {
		primaryStage.setScene(newScene);
		primaryStage.setTitle(title);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
