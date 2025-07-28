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

public class LoginController {

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;

    // Stil sabitleri
    private final String BACKGROUND_COLOR = "#333333"; // Koyu gri
    private final String FIELD_BACKGROUND_COLOR = "#555555";
    private final String TEXT_COLOR = "white";
    private final String BUTTON_LOGIN_COLOR = "#e74c3c"; // UNO Kırmızısı
    private final String BUTTON_LOGIN_HOVER_COLOR = "#c0392b";
    private final String BUTTON_REGISTER_COLOR = "#f1c40f"; // UNO Sarısı
    private final String BUTTON_REGISTER_HOVER_COLOR = "#f39c12";
    private final String BUTTON_TEXT_COLOR_PRIMARY = "white";
    private final String BUTTON_TEXT_COLOR_SECONDARY = "black"; // Sarı buton için

    public LoginController() {
        root = new VBox(20); // Boşluk artırıldı
        root.setPadding(new Insets(40)); // Padding artırıldı
        root.setAlignment(Pos.CENTER); // Ortala
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // --- UNO Logosu ---
        try {
            // Projenizin resources klasörüne "uno_logo.png" adında bir logo ekleyin
            Image logoImage = new Image(new FileInputStream("src/main/resources/Colors/uno!.png"));
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitHeight(80); // Boyutu ayarlayın
            logoView.setPreserveRatio(true);
            logoView.setEffect(new DropShadow(10, Color.BLACK)); // Gölge efekti
            root.getChildren().add(logoView);
        } catch (FileNotFoundException e) {
            System.err.println("Logo dosyası bulunamadı! (src/main/resources/images/uno_logo.png)");
            // Logo yoksa başlığı kullan
            Label title = new Label("UNO");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 48)); // Font büyütüldü
            title.setTextFill(Color.WHITE);
            title.setEffect(new DropShadow(5, Color.BLACK));
            root.getChildren().add(title);
        }

        Label titleDesc = new Label("Login");
        titleDesc.setFont(Font.font("Arial", FontWeight.BOLD, 28)); // Font ayarlandı
        titleDesc.setTextFill(Color.web(TEXT_COLOR));

        // --- Giriş Alanları ---
        usernameField = createStyledTextField("Username");
        passwordField = createStyledPasswordField("Password");

        // --- Butonlar ---
        Button loginButton = createStyledButton("Login", BUTTON_LOGIN_COLOR, BUTTON_LOGIN_HOVER_COLOR, BUTTON_TEXT_COLOR_PRIMARY);
        loginButton.setOnAction(e -> handleLogin());

        Button registerButton = createStyledButton("Go to Register", BUTTON_REGISTER_COLOR, BUTTON_REGISTER_HOVER_COLOR, BUTTON_TEXT_COLOR_SECONDARY);
        registerButton.setOnAction(e -> {
            RegisterController registerController = new RegisterController();
            // Register sahnesi de benzer stil kullanabilir
            Scene registerScene = new Scene(registerController.getView(), 450, 550); // Boyut ayarlandı
            UnoApp.changeScene(registerScene, "UNO Game - Register");
        });

        root.getChildren().addAll(titleDesc, usernameField, passwordField, loginButton, registerButton);
    }

    // Stil uygulanmış TextField oluşturma metodu
    private TextField createStyledTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setFont(Font.font("Arial", 16));
        textField.setPrefWidth(300);
        textField.setMaxWidth(300);
        textField.setStyle(getTextFieldStyle());
        textField.setPadding(new Insets(10)); // İç padding
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
        passwordField.setPadding(new Insets(10)); // İç padding
        return passwordField;
    }

    // Stil uygulanmış Button oluşturma metodu
    private Button createStyledButton(String text, String baseColor, String hoverColor, String textColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefWidth(300);
        button.setMaxWidth(300);
        button.setTextFill(Color.web(textColor)); // Metin rengi ayarlandı
        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-background-radius: 25; -fx-text-fill: " + textColor + ";";
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-background-radius: 25; -fx-text-fill: " + textColor + ";";

        button.setStyle(baseStyle);
        button.setPadding(new Insets(12)); // İç padding
        button.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.4))); // Gölge

        // Hover efekti
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        return button;
    }

    // TextField ve PasswordField için ortak stil
    private String getTextFieldStyle() {
        return "-fx-background-color: " + FIELD_BACKGROUND_COLOR + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #aaaaaa;" + // Prompt text rengi
                "-fx-background-radius: 25;" + // Kenar yuvarlaklığı
                "-fx-border-color: #777777;" + // Hafif kenarlık
                "-fx-border-radius: 25;";
    }

    public VBox getView() {
        return root;
    }

    // --- LOGIC METODLARI ---
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Giriş alanları boş mu kontrolü
        if (username.isEmpty() || password.isEmpty()) {
            showError("Login Failed", "Username and password cannot be empty.");
            return;
        }

        showLoadingIndicator(true); // Yükleme göstergesi
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5)); // Gecikme süresi azaltıldı
        pause.setOnFinished(event -> {
            // API çağrısı
            boolean success = ApiService.login(username, password);
            showLoadingIndicator(false); // Yükleme göstergesini kaldır

            // --- UI güncellemelerini Platform.runLater içine al ---
            Platform.runLater(() -> {
                if (success) {
                    MainMenuController menu = new MainMenuController(); // Ana menüye de stil uygulanmalı
                    UnoApp.changeScene(new Scene(menu.getView(), 800, 600), "UNO Game - Main Menu"); // Boyut ayarlandı
                } else {
                    // Hata mesajını göster
                    showError("Login Failed", "Invalid username or password.");
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
        alert.setHeaderText(title); // Başlık metni eklendi
        alert.setContentText(message);
        // Stil eklemek için DialogPane'e erişilebilir (isteğe bağlı)
        // DialogPane dialogPane = alert.getDialogPane();
        // dialogPane.setStyle("-fx-background-color: #444444;");
        // dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        // ... vb.
        alert.showAndWait();
    }

    // Yükleme göstergesi
    private void showLoadingIndicator(boolean show) {
        Platform.runLater(() -> { // UI Thread'de çalıştığından emin olalım
            if (show) {
                System.out.println("Logging in...");
                if (root != null) root.setDisable(true);
            } else {
                System.out.println("Login attempt finished.");
                if (root != null) root.setDisable(false);
            }
        });
    }
}