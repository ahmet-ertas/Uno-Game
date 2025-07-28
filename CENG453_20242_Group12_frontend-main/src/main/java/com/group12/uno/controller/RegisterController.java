package com.group12.uno.controller;

import com.group12.uno.UnoApp;
import com.group12.uno.service.ApiService;
import javafx.animation.PauseTransition; // Eklendi
import javafx.application.Platform; // Eklendi
import javafx.geometry.Insets;
import javafx.geometry.Pos; // Eklendi
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow; // Eklendi
import javafx.scene.image.Image; // Eklendi
import javafx.scene.image.ImageView; // Eklendi
import javafx.scene.layout.*;
import javafx.scene.paint.Color; // Eklendi
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight; // Eklendi
import javafx.util.Duration; // Eklendi

import java.io.FileInputStream; // Eklendi
import java.io.FileNotFoundException; // Eklendi

public class RegisterController {

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    // Stil sabitleri
    private final String BACKGROUND_COLOR = "#333333";
    private final String FIELD_BACKGROUND_COLOR = "#555555";
    private final String TEXT_COLOR = "white";
    private final String BUTTON_REGISTER_COLOR = "#5599FF"; // UNO Mavisi
    private final String BUTTON_REGISTER_HOVER_COLOR = "#3377DD";
    private final String BUTTON_BACK_COLOR = "#55DD55"; // UNO Yeşili
    private final String BUTTON_BACK_HOVER_COLOR = "#33BB33";
    private final String BUTTON_TEXT_COLOR_PRIMARY = "white";
    private final String BUTTON_TEXT_COLOR_SECONDARY = "black"; // Yeşil buton için

    public RegisterController() {
        root = new VBox(18); // Boşluk ayarlandı
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // --- UNO Logosu ---
        try {
            Image logoImage = new Image(new FileInputStream("src/main/resources/Colors/uno!.png"));
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitHeight(70);
            logoView.setPreserveRatio(true);
            logoView.setEffect(new DropShadow(10, Color.BLACK));
            root.getChildren().add(logoView);
        } catch (FileNotFoundException e) {
            System.err.println("Logo dosyası bulunamadı! (src/main/resources/images/uno_logo.png)");
            Label title = new Label("UNO");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            title.setTextFill(Color.WHITE);
            title.setEffect(new DropShadow(5, Color.BLACK));
            root.getChildren().add(title);
        }

        Label titleDesc = new Label("Register");
        titleDesc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleDesc.setTextFill(Color.web(TEXT_COLOR));

        // --- Giriş Alanları ---
        usernameField = createStyledTextField("Choose Username");
        passwordField = createStyledPasswordField("Password");
        confirmPasswordField = createStyledPasswordField("Confirm Password");

        // --- Butonlar ---
        Button registerButton = createStyledButton("Register", BUTTON_REGISTER_COLOR, BUTTON_REGISTER_HOVER_COLOR, BUTTON_TEXT_COLOR_PRIMARY);
        registerButton.setOnAction(e -> handleRegister());

        Button backButton = createStyledButton("Back to Login", BUTTON_BACK_COLOR, BUTTON_BACK_HOVER_COLOR, BUTTON_TEXT_COLOR_SECONDARY);
        backButton.setOnAction(e -> {
            LoginController loginController = new LoginController();
            Scene loginScene = new Scene(loginController.getView(), 450, 550); // Boyut ayarlandı
            UnoApp.changeScene(loginScene, "UNO Game - Login");
        });

        root.getChildren().addAll(titleDesc, usernameField, passwordField, confirmPasswordField, registerButton, backButton);
    }

    // Stil uygulanmış TextField oluşturma metodu
    private TextField createStyledTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setFont(Font.font("Arial", 16));
        textField.setPrefWidth(300);
        textField.setMaxWidth(300);
        textField.setStyle(getTextFieldStyle());
        textField.setPadding(new Insets(10));
        return textField;
    }

    // Stil uygulanmış PasswordField oluşturma metodu
    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(prompt);
        passwordField.setFont(Font.font("Arial", 16));
        passwordField.setPrefWidth(300);
        passwordField.setMaxWidth(300);
        passwordField.setStyle(getTextFieldStyle());
        passwordField.setPadding(new Insets(10));
        return passwordField;
    }

    // Stil uygulanmış Button oluşturma metodu
    private Button createStyledButton(String text, String baseColor, String hoverColor, String textColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefWidth(300);
        button.setMaxWidth(300);
        button.setTextFill(Color.web(textColor));
        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-background-radius: 25; -fx-text-fill: " + textColor + ";";
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-background-radius: 25; -fx-text-fill: " + textColor + ";";

        button.setStyle(baseStyle);
        button.setPadding(new Insets(12));
        button.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.4)));

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        return button;
    }

    // TextField ve PasswordField için ortak stil
    private String getTextFieldStyle() {
        return "-fx-background-color: " + FIELD_BACKGROUND_COLOR + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #aaaaaa;" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: #777777;" +
                "-fx-border-radius: 25;";
    }

    public VBox getView() {
        return root;
    }

    // --- LOGIC METODLARI ---
    private void handleRegister() {
        String username = usernameField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (username.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            showError("Error", "All fields are required!");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showError("Error", "Passwords do not match!");
            passwordField.clear();
            confirmPasswordField.clear();
            passwordField.requestFocus();
            return;
        }

        showLoadingIndicator(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5)); // Gecikme süresi azaltıldı
        pause.setOnFinished(event -> {
            // API çağrısı
            boolean success = ApiService.register(username, pass);
            showLoadingIndicator(false); // Yüklemeyi bitir

            // --- UI güncellemelerini Platform.runLater içine al ---
            Platform.runLater(() -> {
                if (success) {
                    System.out.println("Register successful!");

                    // Bilgi mesajını göster
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Registration successful! You can now log in.");
                    alert.showAndWait(); // runLater içinde sorun olmamalı

                    // Login ekranına dön
                    LoginController loginController = new LoginController();
                    Scene loginScene = new Scene(loginController.getView(), 450, 550);
                    UnoApp.changeScene(loginScene, "UNO Game - Login");
                } else {
                    // Hata mesajını göster
                    showError("Registration Failed", "Registration failed. Try a different username.");
                }
            });
            // ----------------------------------------------------
        });
        pause.play();
    }

    // Hata gösterme metodu
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Yükleme göstergesi
    private void showLoadingIndicator(boolean show) {
        Platform.runLater(() -> { // UI Thread'de çalıştığından emin olalım
            if (show) {
                System.out.println("Registering...");
                if (root != null) root.setDisable(true);
            } else {
                System.out.println("Registration attempt finished.");
                if (root != null) root.setDisable(false);
            }
        });
    }
}