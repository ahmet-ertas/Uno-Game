package com.group12.uno.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group12.uno.UnoApp;
import com.group12.uno.service.ApiService;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import java.io.File;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayerGameBoardController {
    private final BorderPane root;
    private final String gameId;
    private final String username;
    private final Label statusLabel;
    private final Label directionLabel;
    private final Label messageLabel;
    private final VBox[] handBoxes;
    private final Button drawButton;
    private final Button unoButton;
    private final Timer timer = new Timer(true);
    private long lastTimestamp = 0;
    private Map<String, Object> gameState;
    private boolean isMyTurn = false;
    private boolean gameEnded = false;
    private VBox topCardBox;
    private StackPane colorIndicatorPane = new StackPane();
    private Label colorIndicatorLabel = new Label();
    private PauseTransition colorIndicatorFade = new PauseTransition(Duration.seconds(2));

    public MultiplayerGameBoardController(String gameId, String username) {
        this.gameId = gameId;
        this.username = username;
        this.root = new BorderPane();
        this.handBoxes = new VBox[2];
        this.gameEnded = false;
        this.isMyTurn = false;

        // Set window size
        root.setMinSize(1024, 768);
        root.setPrefSize(1200, 900);

        // Set gradient background
        root.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff512f")),
                new Stop(1, Color.web("#dd2476"))),
            new CornerRadii(0),
            Insets.EMPTY)));

        root.setPadding(new Insets(20));

        // Initialize top status area
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15; -fx-padding: 15;");
        topBox.setMaxHeight(100);
        
        this.statusLabel = new Label("Waiting for game to start...");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        statusLabel.setTextFill(Color.WHITE);
        
        this.directionLabel = new Label("Direction: ⟳ Clockwise");
        directionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        directionLabel.setTextFill(Color.LIGHTBLUE);
        
        topBox.getChildren().addAll(statusLabel, directionLabel);
        root.setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(0, 0, 10, 0));

        // Initialize game board
        BorderPane boardPane = new BorderPane();
        boardPane.setPadding(new Insets(20));
        boardPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 20;");

        // Initialize player areas
        handBoxes[0] = createHandBox(0);
        handBoxes[1] = createHandBox(1);

        VBox bottomPlayerBox = wrapWithAvatarBox(handBoxes[0], 0);
        VBox topPlayerBox = wrapWithAvatarBox(handBoxes[1], 1);
        
        bottomPlayerBox.setMaxHeight(200);
        topPlayerBox.setMaxHeight(200);
        
        boardPane.setBottom(bottomPlayerBox);
        boardPane.setTop(topPlayerBox);
        
        BorderPane.setMargin(bottomPlayerBox, new Insets(10));
        BorderPane.setMargin(topPlayerBox, new Insets(10));

        // Initialize center area
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.12); -fx-background-radius: 20; -fx-padding: 30;");
        centerBox.setMaxSize(400, 400);
        
        HBox piles = new HBox(40);
        piles.setAlignment(Pos.CENTER);
        
        ImageView drawPile = new ImageView(getCardBackImage());
        drawPile.setFitWidth(120);
        drawPile.setFitHeight(180);
        drawPile.setRotate(-10);
        drawPile.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");
        
        this.topCardBox = new VBox(10);
        topCardBox.setAlignment(Pos.CENTER);
        topCardBox.setMinSize(120, 180);
        
        piles.getChildren().addAll(drawPile, topCardBox);

        this.colorIndicatorPane = new StackPane();
        this.colorIndicatorLabel = new Label();
        colorIndicatorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        colorIndicatorLabel.setTextFill(Color.WHITE);
        colorIndicatorPane.getChildren().add(colorIndicatorLabel);
        colorIndicatorPane.setStyle("-fx-background-radius: 30; -fx-padding: 15 40;");
        colorIndicatorPane.setVisible(true); // Always visible
        
        centerBox.getChildren().addAll(piles, colorIndicatorPane);
        boardPane.setCenter(centerBox);

        // Initialize bottom controls
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        bottomBox.setMinHeight(150);
        bottomBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15;");

        this.messageLabel = new Label("Waiting for opponent...");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(800);
        
        HBox actionBox = new HBox(30);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(10));
        actionBox.setMinHeight(80);

        this.drawButton = createStyledButton("Draw Card", "#e74c3c");
        drawButton.setOnAction(e -> sendDrawCard());
        drawButton.setDisable(true);

        this.unoButton = createStyledButton("UNO!", "#2ecc71");
        unoButton.setOnAction(e -> sendSayUno());
        unoButton.setDisable(true);

        actionBox.getChildren().addAll(drawButton, unoButton);
        bottomBox.getChildren().addAll(messageLabel, actionBox);

        root.setBottom(bottomBox);
        root.setCenter(boardPane);

        BorderPane.setMargin(bottomBox, new Insets(10, 0, 0, 0));
        BorderPane.setMargin(boardPane, new Insets(10));

        // Start the game without CPU players
        ApiService.startGame(gameId);

        // Start polling
        pollGameState();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> pollGameState());
            }
        }, 1000, 1000);
    }

    private Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        button.setMinSize(180, 60);
        button.setPrefSize(200, 60);
        
        // Normal state
        String normalStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-background-radius: 12; -fx-padding: 16 36; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);",
            baseColor
        );
        
        // Hover state
        String hoverStyle = String.format(
            "-fx-background-color: derive(%s, -20%%); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-background-radius: 12; " +
            "-fx-padding: 16 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 6);",
            baseColor
        );
        
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
        
        return button;
    }

    private VBox createHandBox(int index) {
        VBox handBox = new VBox(10);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(15));
        handBox.setMinHeight(150);
        handBox.setMaxHeight(200);
        handBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15;");
        return handBox;
    }

    private VBox wrapWithAvatarBox(VBox handBox, int index) {
        VBox wrapper = new VBox(15);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(20));
        wrapper.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15;");
        
        HBox playerInfo = new HBox(15);
        playerInfo.setAlignment(Pos.CENTER);
        
        // Create avatar circle with player number
        StackPane avatarStack = new StackPane();
        Circle avatar = new Circle(30);
        avatar.setFill(getAvatarColor(index));
        avatar.setStroke(Color.WHITE);
        avatar.setStrokeWidth(2);
        
        Label playerNum = new Label("P" + (index + 1));
        playerNum.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        playerNum.setTextFill(Color.WHITE);
        
        avatarStack.getChildren().addAll(avatar, playerNum);
        avatarStack.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);");
        
        // Player name label
        Label nameLabel = new Label();
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10; -fx-padding: 5 15;");
        
        playerInfo.getChildren().addAll(avatarStack, nameLabel);
        
        // Store the name label for later updates
        if (index == 0) {
            nameLabel.setText(username + " (You)");
        } else {
            // We'll update opponent's name when we receive game state
            nameLabel.setText("Waiting for opponent...");
        }
        
        wrapper.getChildren().addAll(playerInfo, handBox);
        return wrapper;
    }

    private Color getAvatarColor(int index) {
        Color[] colors = { Color.web("#f39c12"), Color.web("#2980b9") };
        return colors[index % colors.length];
    }

    private void updateUI() {
        if (gameState == null) return;

        // Get all players (already filtered for human players only)
        List<String> playerIds = (List<String>) gameState.get("playerIds");
        if (playerIds == null || playerIds.isEmpty()) {
            messageLabel.setText("Waiting for players to join...");
            return;
        }

        // Update status and direction
        String currentPlayer = (String) gameState.get("currentPlayer");
        isMyTurn = username.equals(currentPlayer);
        
        // Update turn status with player names
        if (isMyTurn) {
            statusLabel.setText("Your turn!");
            statusLabel.setTextFill(Color.LIGHTGREEN);
        } else if (currentPlayer != null) {
            statusLabel.setText(currentPlayer + "'s turn");
            statusLabel.setTextFill(Color.WHITE);
        }

        // Update direction indicator with icon
        boolean isClockwise = (int)gameState.get("direction") == -1;
        directionLabel.setText("Direction: " + (isClockwise ? "⟳ Clockwise" : "⟲ Counter-clockwise"));
        directionLabel.setTextFill(isClockwise ? Color.LIGHTBLUE : Color.LIGHTPINK);

        // Update top card and current color
        Map<String, Object> topCard = (Map<String, Object>) gameState.get("topCard");
        if (topCard != null) {
            topCardBox.getChildren().clear();
            ImageView topImg = new ImageView(getCardImage(topCard));
            topImg.setFitWidth(120);
            topImg.setFitHeight(180);
            topImg.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
            topCardBox.getChildren().add(topImg);

            // Always show current color - prioritize currentColor over card's color
            String currentColor = (String) topCard.get("currentColor");
            String cardColor = (String) topCard.get("color");
            String displayColor = currentColor != null && !currentColor.equals("WILD") ? currentColor : cardColor;
            
            if (displayColor != null) {
                updateColorDisplay(displayColor);
            }
        }

        // Update hands
        Map<String, List<Map<String, Object>>> playerHands = (Map<String, List<Map<String, Object>>>) gameState.get("playerHands");
        
        // Update current player's hand (bottom)
        updatePlayerHand(handBoxes[0], username, playerHands, currentPlayer, topCard, true);
        
        // Update opponent's hand (top) if there is one
        if (playerIds.size() > 1) {
            String opponentId = playerIds.get(1);
            updatePlayerHand(handBoxes[1], opponentId, playerHands, currentPlayer, topCard, false);
        }

        // Update buttons state
        drawButton.setDisable(!isMyTurn);
        unoButton.setDisable(!isMyTurn);

        // Update message based on game state
        String message = formatGameMessage(gameState);
        if (message != null && !message.isEmpty()) {
            messageLabel.setText(message);
        }
    }

    private void updatePlayerHand(VBox handBox, String playerId, Map<String, List<Map<String, Object>>> playerHands, 
                                String currentPlayer, Map<String, Object> topCard, boolean isCurrentPlayer) {
        handBox.getChildren().clear();

        List<Map<String, Object>> playerHand = playerHands.get(playerId);
        int cardCount = playerHand != null ? playerHand.size() : 0;

        // Create card display container
        HBox cardContainer = new HBox(-30); // Negative spacing for card overlap
        cardContainer.setAlignment(Pos.CENTER);

        if (isCurrentPlayer) {
            // Show actual cards for current player
            if (playerHand != null) {
                for (Map<String, Object> card : playerHand) {
                    ImageView cardImg = new ImageView(getCardImage(card));
                    cardImg.setFitWidth(80);
                    cardImg.setFitHeight(120);
                    
                    Button cardBtn = new Button();
                    cardBtn.setGraphic(cardImg);
                    cardBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    
                    boolean canPlay = isMyTurn && canPlayCard(card, topCard);
                    cardBtn.setDisable(!canPlay);
                    
                    if (canPlay) {
                        cardBtn.setOnMouseEntered(e -> {
                            cardImg.setFitWidth(90);
                            cardImg.setFitHeight(135);
                            cardImg.setTranslateY(-20);
                        });
                        cardBtn.setOnMouseExited(e -> {
                            cardImg.setFitWidth(80);
                            cardImg.setFitHeight(120);
                            cardImg.setTranslateY(0);
                        });
                        
                        final Map<String, Object> cardToPlay = card;
                        cardBtn.setOnAction(e -> handlePlayCard(cardToPlay));
                    }
                    
                    cardContainer.getChildren().add(cardBtn);
                }
            }
        } else {
            // Show card backs for opponent
            for (int j = 0; j < cardCount; j++) {
                ImageView cardBack = new ImageView(getCardBackImage());
                cardBack.setFitWidth(60);
                cardBack.setFitHeight(90);
                cardBack.setRotate(180); // Rotate opponent's cards
                StackPane cardPane = new StackPane(cardBack);
                cardPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
                cardContainer.getChildren().add(cardPane);
            }
        }

        handBox.getChildren().add(cardContainer);

        // Update card count label
        Label countLabel = new Label("Cards: " + cardCount);
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        countLabel.setTextFill(Color.WHITE);
        countLabel.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10; -fx-padding: 5 15;");
        handBox.getChildren().add(countLabel);

        // Update hand box style based on turn
        VBox wrapper = (VBox) handBox.getParent();
        wrapper.setStyle("-fx-background-color: " + 
            (playerId.equals(currentPlayer) ? "rgba(46, 204, 113, 0.2)" : "rgba(255, 255, 255, 0.1)") + 
            "; -fx-background-radius: 15;");

        // Update player name in the wrapper
        HBox playerInfo = (HBox) wrapper.getChildren().get(0);
        Label nameLabel = (Label) ((HBox) playerInfo).getChildren().get(1);
        nameLabel.setText(playerId + (isCurrentPlayer ? " (You)" : ""));
    }

    private String formatGameMessage(Map<String, Object> gameState) {
        String message = "";
        
        if (gameState.containsKey("lastUpdate")) {
            Map<String, Object> lastUpdate = (Map<String, Object>) gameState.get("lastUpdate");
            if (lastUpdate != null) {
                String type = (String) lastUpdate.get("type");
                Map<String, Object> data = (Map<String, Object>) lastUpdate.get("data");
                
                if (data != null && data.containsKey("playerId")) {
                    String playerId = (String) data.get("playerId");
                    String playerName = playerId.equals(username) ? "You" : 
                                      playerId.startsWith("CPU") ? "CPU" : playerId;
                    
                    switch (type) {
                        case "PLAYER_JOINED":
                            message = playerName + " joined the game";
                            break;
                        case "CARD_PLAYED":
                            message = playerName + " played a card";
                            break;
                        case "CARD_DRAWN":
                            message = playerName + " drew a card";
                            break;
                        case "UNO_CALLED":
                            message = playerName + " called UNO!";
                            break;
                        case "TURN_CHANGED":
                            message = (isMyTurn ? "Your turn!" : playerName + "'s turn");
                            break;
                        default:
                            message = "";
                    }
                }
            }
        }
        
        return message;
    }

    private void showGameOverDialog(String winner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Game Over! " + winner + " wins!");

        // Create a custom dialog content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        // Winner announcement with style
        Label winnerLabel = new Label(winner + " wins!");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        winnerLabel.setTextFill(Color.WHITE);
        winnerLabel.setStyle("-fx-background-color: rgba(46, 204, 113, 0.8); -fx-background-radius: 10; -fx-padding: 10 20;");

        content.getChildren().add(winnerLabel);

        // Button to return to main menu
        ButtonType menuButton = new ButtonType("Main Menu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(menuButton);

        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait();
        
        // Return to main menu
        Platform.runLater(() -> {
            MainMenuController mainMenu = new MainMenuController();
            Scene menuScene = new Scene(mainMenu.getView(), 800, 600);
            UnoApp.changeScene(menuScene, "UNO Game - Main Menu");
        });
    }

    private void updateColorDisplay(String color) {
        // Convert color name to proper case for display
        String displayColor = color.substring(0, 1).toUpperCase() + color.substring(1).toLowerCase();
        colorIndicatorLabel.setText("Current Color: " + displayColor);
        
        String bgColor;
        switch (color.toUpperCase()) {
            case "RED": bgColor = "#e74c3c"; break;
            case "YELLOW": bgColor = "#f1c40f"; break;
            case "GREEN": bgColor = "#2ecc71"; break;
            case "BLUE": bgColor = "#3498db"; break;
            case "WILD": bgColor = "#333333"; break;
            default: bgColor = "#333333";
        }
        
        colorIndicatorPane.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-background-radius: 30; " +
            "-fx-padding: 15 40; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);"
        );
        colorIndicatorPane.setVisible(true);
    }

    private void showColorIndicator(String color) {
        // Update to use the common updateColorDisplay method
        updateColorDisplay(color);
    }

    private Image getCardImage(Map<String, Object> card) {
        String basePath = "src/main/resources/Colors/";
        String filename = "";
        String color = ((String) card.get("color")).toLowerCase();
        String type = (String) card.get("type");
        int number = (int) card.get("number");

        // Handle special cards and number cards based on actual file naming convention
        switch (type) {
            case "NUMBER":
                filename = color + number + ".png";
                break;
            case "REVERSE":
                filename = color + "10.png";  // Reverse is card 10
                break;
            case "SKIP":
                filename = color + "11.png";  // Skip is card 11
                break;
            case "DRAW_TWO":
                filename = color + "12.png";  // Draw Two is card 12
                break;
            case "WILD":
                filename = "wild13.png";  // Wild is card 13
                break;
            case "WILD_DRAW_FOUR":
                filename = "wild14.png";  // Wild Draw Four is card 14
                break;
        }

        // First try the resources directory
        File file = new File(basePath + filename);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        // If not found in resources, try the classpath
        String classPathResource = "/Colors/" + filename;
        try {
            return new Image(getClass().getResourceAsStream(classPathResource));
        } catch (Exception e) {
            System.err.println("Could not load card image: " + filename);
            // Return the card back image as fallback
            return getCardBackImage();
        }
    }

    private Image getCardBackImage() {
        String basePath = "src/main/resources/Colors/";
        String filename = "back.png";

        // First try the resources directory
        File file = new File(basePath + filename);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        // If not found in resources, try the classpath
        String classPathResource = "/Colors/" + filename;
        try {
            return new Image(getClass().getResourceAsStream(classPathResource));
        } catch (Exception e) {
            System.err.println("Could not load card back image");
            return null;
        }
    }

    private boolean canPlayCard(Map<String, Object> card, Map<String, Object> topCard) {
        if (!isMyTurn || gameEnded) return false;
        if (topCard == null) return true;

        String cardColor = (String) card.get("color");
        String cardType = (String) card.get("type");
        
        // Wild cards can always be played
        if (cardType.equals("WILD") || cardType.equals("WILD_DRAW_FOUR")) {
            return true;
        }

        // Get the active color (either from currentColor or from the top card)
        String activeColor = (String) topCard.get("currentColor");
        if (activeColor == null) {
            activeColor = (String) topCard.get("color");
        }

        // Match by color
        if (cardColor.equals(activeColor)) {
            return true;
        }

        String topType = (String) topCard.get("type");
        
        // Match by number
        if (cardType.equals("NUMBER") && topType.equals("NUMBER")) {
            int cardNumber = (int) card.get("number");
            int topNumber = (int) topCard.get("number");
            if (cardNumber == topNumber) {
                return true;
            }
        }

        // Match by type for special cards
        return cardType.equals(topType) && !cardType.equals("NUMBER");
    }

    private void handlePlayCard(Map<String, Object> card) {
        if (!isMyTurn || gameEnded) {
            messageLabel.setText("It's not your turn!");
            return;
        }

        String type = (String) card.get("type");
        if (type.equals("WILD") || type.equals("WILD_DRAW_FOUR")) {
            // For WILD_DRAW_FOUR, first check if it's a legal play
           
            createColorSelectionDialog(card);
        } else {
            sendPlayCard(card, null);
        }
    }

    private boolean isWildDrawFourLegal() {
        if (gameState == null || !isMyTurn) return false;

        Map<String, Object> topCard = (Map<String, Object>) gameState.get("topCard");
        if (topCard == null) return true;

        // Get the current active color
        String activeColor = (String) topCard.get("currentColor");
        if (activeColor == null) {
            activeColor = (String) topCard.get("color");
        }

        // Check player's hand for any cards matching the current color
        Map<String, List<Map<String, Object>>> playerHands = 
            (Map<String, List<Map<String, Object>>>) gameState.get("playerHands");
        List<Map<String, Object>> myHand = playerHands.get(username);

        if (myHand != null) {
            for (Map<String, Object> card : myHand) {
                String cardColor = (String) card.get("color");
                String cardType = (String) card.get("type");
                
                // Skip wild cards in this check
                if (cardType.equals("WILD") || cardType.equals("WILD_DRAW_FOUR")) {
                    continue;
                }

                // If we find a card matching the current color, Wild Draw Four is illegal
                if (cardColor.equals(activeColor)) {
                    return false;
                }
            }
        }

        // If we found no matching colors, Wild Draw Four is legal
        return true;
    }

    private void createColorSelectionDialog(Map<String, Object> card) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Select a color for your wild card");

        // Create a custom dialog content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");

        // Create color buttons with custom styling
        String[] colors = {"RED", "YELLOW", "GREEN", "BLUE"};
        String[] colorHex = {"#e74c3c", "#f1c40f", "#2ecc71", "#3498db"};
        String[] colorNames = {"Red", "Yellow", "Green", "Blue"};

        for (int i = 0; i < colors.length; i++) {
            final String color = colors[i];
            Button colorBtn = new Button(colorNames[i]);
            colorBtn.setStyle(
                "-fx-background-color: " + colorHex[i] + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-min-width: 150px; " +
                "-fx-min-height: 50px; " +
                "-fx-background-radius: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
            );

            // Hover effect
            colorBtn.setOnMouseEntered(e -> 
                colorBtn.setStyle(colorBtn.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
            colorBtn.setOnMouseExited(e -> 
                colorBtn.setStyle(colorBtn.getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1;", "")));

            colorBtn.setOnAction(e -> {
                dialog.setResult(color);
                dialog.close();
            });

            content.getChildren().add(colorBtn);
        }

        // Style the dialog
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");
        dialog.getDialogPane().getButtonTypes().clear(); // Remove default buttons

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(color -> {
            // Set the chosen color as the current color in the card
            card.put("currentColor", color);
            
            // Send the play card action with the chosen color
            sendPlayCard(card, color);
            
            // Show the color indicator
            showColorIndicator(color);
            
            // Update the message label
            String type = (String) card.get("type");
            if (type.equals("WILD_DRAW_FOUR")) {
                messageLabel.setText("Wild Draw Four! Next player must draw 4 cards. Color changed to " + color);
            } else {
                messageLabel.setText("Wild card played! Color changed to " + color);
            }
        });
    }

    private void sendPlayCard(Map<String, Object> card, String chosenColor) {
        try {
            Map<String, Object> action = new HashMap<>();
            action.put("gameId", gameId);
            action.put("type", "PLAY_CARD");
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", username);
            data.put("card", card);
            if (chosenColor != null) {
                data.put("chosenColor", chosenColor);
            }
            action.put("data", data);
            
            String url = ApiService.getBaseUrl() + "/game/action";
            ObjectMapper objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(action);
            Request request = Request.post(url)
                    .bodyString(body, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            request.execute();

            // Update the UI immediately with the played card
            Platform.runLater(() -> {
                // Update top card box with the played card
                topCardBox.getChildren().clear();
                ImageView topImg = new ImageView(getCardImage(card));
                topImg.setFitWidth(120);
                topImg.setFitHeight(180);
                topImg.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
                topCardBox.getChildren().add(topImg);

                // Show color indicator for wild cards
                if (chosenColor != null) {
                    showColorIndicator(chosenColor);
                    
                    // Show message for Wild Draw Four
                    String cardType = (String) card.get("type");
                    if (cardType.equals("WILD_DRAW_FOUR")) {
                        messageLabel.setText("Wild Draw Four! Next player must draw 4 cards.");
                    }
                }
            });

            pollGameState();
        } catch (Exception e) {
            messageLabel.setText("Error playing card: " + e.getMessage());
        }
    }

    private void sendDrawCard() {
        if (!isMyTurn || gameEnded) {
            return;
        }

        try {
            Map<String, Object> action = new HashMap<>();
            action.put("gameId", gameId);
            action.put("type", "DRAW_CARD");
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", username);
            action.put("data", data);
            
            String url = ApiService.getBaseUrl() + "/game/action";
            ObjectMapper objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(action);
            Request request = Request.post(url)
                    .bodyString(body, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            request.execute();

            // Animate the card draw
            animateDrawCard(() -> {
                messageLabel.setText("You drew a card");
                pollGameState();
            });
            
        } catch (Exception e) {
            messageLabel.setText("Error drawing card: " + e.getMessage());
        }
    }

    private void animateDrawCard(Runnable onFinished) {
        Platform.runLater(() -> {
            // Find the draw pile and player's hand
            HBox piles = (HBox)((VBox)((BorderPane)root.getCenter()).getCenter()).getChildren().get(0);
            ImageView drawPile = (ImageView)piles.getChildren().get(0);
            VBox handBox = handBoxes[0]; // Player's hand is always at index 0
            
            // Find the cards container in the hand box
            VBox cardsRows = null;
            for (javafx.scene.Node node : handBox.getChildren()) {
                if (node instanceof VBox) {
                    cardsRows = (VBox) node;
                    break;
                }
            }
            
            if (cardsRows == null || cardsRows.getChildren().isEmpty()) {
                if (onFinished != null) onFinished.run();
                return;
            }

            // Get the last row of cards
            HBox lastRow = (HBox) cardsRows.getChildren().get(cardsRows.getChildren().size() - 1);

            // Create animated card
            ImageView animCard = new ImageView(getCardBackImage());
            animCard.setFitWidth(70);
            animCard.setFitHeight(105);
            ((BorderPane)root.getCenter()).getChildren().add(animCard);

            // Calculate start and end positions
            double startX = drawPile.localToScene(drawPile.getBoundsInLocal()).getMinX();
            double startY = drawPile.localToScene(drawPile.getBoundsInLocal()).getMinY();
            double endX = lastRow.localToScene(lastRow.getBoundsInLocal()).getMinX() + lastRow.getChildren().size() * 20;
            double endY = lastRow.localToScene(lastRow.getBoundsInLocal()).getMinY();

            // Position the animated card
            animCard.setTranslateX(startX - ((BorderPane)root.getCenter()).localToScene(0,0).getX());
            animCard.setTranslateY(startY - ((BorderPane)root.getCenter()).localToScene(0,0).getY());

            // Create and play animation
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), animCard);
            tt.setToX(endX - ((BorderPane)root.getCenter()).localToScene(0,0).getX());
            tt.setToY(endY - ((BorderPane)root.getCenter()).localToScene(0,0).getY());
            
            tt.setOnFinished(e -> {
                ((BorderPane)root.getCenter()).getChildren().remove(animCard);
                if (onFinished != null) {
                    onFinished.run();
                }
            });
            
            tt.play();
        });
    }

    private void drawForcedCards(int count, Runnable onFinished) {
        if (count <= 0) {
            if (onFinished != null) onFinished.run();
            return;
        }

        animateDrawCard(() -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> drawForcedCards(count - 1, onFinished));
            pause.play();
        });
    }

    private void handleForcedDraw(int count) {
        messageLabel.setText("Drawing " + count + " cards...");
        drawForcedCards(count, () -> {
            messageLabel.setText("Drew " + count + " cards");
            pollGameState();
        });
    }

    private void sendSayUno() {
        try {
            Map<String, Object> action = new HashMap<>();
            action.put("gameId", gameId);
            action.put("type", "SAY_UNO");
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", username);
            action.put("data", data);
            
            String url = ApiService.getBaseUrl() + "/game/action";
            ObjectMapper objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(action);
            Request request = Request.post(url)
                    .bodyString(body, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            request.execute();
            
            pollGameState();
        } catch (Exception e) {
            messageLabel.setText("Error calling UNO: " + e.getMessage());
        }
    }

    private void pollGameState() {
        if (gameEnded) return;
        
        try {
            String url = ApiService.getBaseUrl() + "/game/" + gameId + "/updates?since=" + lastTimestamp + "&humanOnly=true";
            Request request = Request.get(url)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            
            String response = request.execute().returnContent().asString();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(response, new TypeReference<>() {});
            
            if (result.containsKey("timestamp")) {
                lastTimestamp = ((Number) result.get("timestamp")).longValue();
            }

            if (result.containsKey("gameState") && result.get("gameState") != null) {
                Map<String, Object> newGameState = (Map<String, Object>) result.get("gameState");
                
                // Filter out any CPU players from the game state
                List<String> playerIds = (List<String>) newGameState.get("playerIds");
                if (playerIds != null) {
                    // Keep only human players
                    int originalSize = playerIds.size();
                    playerIds.removeIf(playerId -> playerId == null || playerId.startsWith("CPU"));
                    
                    // If we removed any CPU players, update the UI accordingly
                    if (playerIds.size() < originalSize) {
                        Platform.runLater(() -> {
                            messageLabel.setText("Waiting for more players to join...");
                            statusLabel.setText("Game will start with 2 players");
                        });
                        return; // Skip this update while waiting for human players
                    }
                    
                    // Update the game state to reflect only human players
                    Map<String, List<Map<String, Object>>> playerHands = 
                        (Map<String, List<Map<String, Object>>>) newGameState.get("playerHands");
                    if (playerHands != null) {
                        playerHands.keySet().removeIf(playerId -> playerId == null || playerId.startsWith("CPU"));
                    }
                    
                    // If current player is CPU or null, wait for next update
                    String currentPlayer = (String) newGameState.get("currentPlayer");
                    if (currentPlayer == null || currentPlayer.startsWith("CPU")) {
                        Platform.runLater(() -> {
                            messageLabel.setText("Waiting for players to take their turn...");
                        });
                        return; // Skip this update
                    }

                    // Verify we have valid player count
                    if (playerIds.isEmpty()) {
                        Platform.runLater(() -> {
                            messageLabel.setText("Waiting for players to join...");
                            statusLabel.setText("Game will start with 2 players");
                        });
                        return;
                    }
                }
                
                gameState = newGameState;
                
                Platform.runLater(() -> {
                    try {
                        // Update player names and UI elements
                        updatePlayerDisplay(playerIds);
                        
                        // Update the game board
                        updateUI();
                        
                        // Process game messages
                        processGameUpdates(result);
                        
                    } catch (Exception e) {
                        System.err.println("Error updating UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error polling game state: " + e.getMessage());
            e.printStackTrace();
            
            Platform.runLater(() -> {
                messageLabel.setText("Connection error. Retrying...");
            });
        }
    }

    private void updatePlayerDisplay(List<String> playerIds) {
        if (playerIds == null) return;
        
        // Clear all player displays first
        for (int i = 0; i < handBoxes.length; i++) {
            VBox wrapper = (VBox) handBoxes[i].getParent();
            HBox playerInfo = (HBox) wrapper.getChildren().get(0);
            Label nameLabel = (Label) ((HBox) playerInfo).getChildren().get(1);
            nameLabel.setText("Waiting for player...");
            wrapper.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15;");
        }
        
        // Update only valid human players
        for (int i = 0; i < Math.min(2, playerIds.size()); i++) {
            String playerId = playerIds.get(i);
            if (playerId != null && !playerId.startsWith("CPU")) {
                VBox wrapper = (VBox) handBoxes[i].getParent();
                HBox playerInfo = (HBox) wrapper.getChildren().get(0);
                Label nameLabel = (Label) ((HBox) playerInfo).getChildren().get(1);
                
                if (playerId.equals(username)) {
                    nameLabel.setText(playerId + " (You)");
                } else {
                    nameLabel.setText(playerId);
                }
            }
        }
    }

    private void processGameUpdates(Map<String, Object> result) {
        if (result.containsKey("updates")) {
            List<Map<String, Object>> updates = (List<Map<String, Object>>) result.get("updates");
            if (updates != null && !updates.isEmpty()) {
                // Filter out CPU updates and null data
                updates.removeIf(update -> {
                    if (update == null) return true;
                    Map<String, Object> data = (Map<String, Object>) update.get("data");
                    if (data == null || !data.containsKey("playerId")) return true;
                    String playerId = (String) data.get("playerId");
                    return playerId == null || playerId.startsWith("CPU");
                });
                
                if (!updates.isEmpty()) {
                    Map<String, Object> lastUpdate = updates.get(updates.size() - 1);
                    String message = formatGameMessage(lastUpdate);
                    if (message != null && !message.isEmpty()) {
                        messageLabel.setText(message);
                    }
                }
            }
        }
        
        // Check for game end (only for human winners)
        String winner = (String) gameState.get("winner");
        if (winner != null && !gameEnded && !winner.startsWith("CPU")) {
            gameEnded = true;
            timer.cancel();
            showGameOverDialog(winner);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (timer != null) {
            timer.cancel();
        }
        super.finalize();
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public BorderPane getView() {
        return root;
    }

    private void drawUntilPlayableHuman() {
        if (!isMyTurn || gameEnded) {
            return;
        }

        try {
            Map<String, Object> action = new HashMap<>();
            action.put("gameId", gameId);
            action.put("type", "DRAW_CARD");
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", username);
            action.put("data", data);
            
            String url = ApiService.getBaseUrl() + "/game/action";
            ObjectMapper objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(action);
            Request request = Request.post(url)
                    .bodyString(body, ContentType.APPLICATION_JSON)
                    .addHeader("Authorization", "Bearer " + ApiService.getToken());
            request.execute();

            // Animate the card draw
            animateDrawCard(() -> {
                messageLabel.setText("You drew a card");
                pollGameState();
            });
            
        } catch (Exception e) {
            messageLabel.setText("Error drawing card: " + e.getMessage());
        }
    }
} 