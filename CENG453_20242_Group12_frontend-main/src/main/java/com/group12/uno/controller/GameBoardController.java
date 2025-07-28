package com.group12.uno.controller;

import com.group12.uno.model.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.layout.CornerRadii;
import javafx.animation.TranslateTransition;
import javafx.stage.Stage;
import com.group12.uno.UnoApp;

public class GameBoardController {
    private BorderPane root;
    private GameState gameState;
    private Label statusLabel;
    private VBox[] handBoxes;
    private Label messageLabel;
    private Label directionLabel;
    private VBox topCardBox;
    // Color indicator for wild cards
    private StackPane colorIndicatorPane = new StackPane();
    private Label colorIndicatorLabel = new Label();
    private PauseTransition colorIndicatorFade = new PauseTransition(Duration.seconds(2));

    public GameBoardController(String playerName) {
        gameState = new GameState(playerName);
        root = new BorderPane();
        // Vibrant red/orange gradient background
        root.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff512f")),
                new Stop(1, Color.web("#dd2476"))),
            CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(20));

        // Status and direction at the top
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 10; -fx-padding: 10;");
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        statusLabel.setTextFill(Color.WHITE);
        directionLabel = new Label();
        directionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        updateStatus();
        updateDirection();
        topBox.getChildren().addAll(statusLabel, directionLabel);
        root.setTop(topBox);

        // Message area at the bottom
        messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 10; -fx-padding: 15;");
        bottomBox.getChildren().add(messageLabel);
        root.setBottom(bottomBox);

        // Center: main board
        BorderPane boardPane = new BorderPane();
        boardPane.setPadding(new Insets(20));
        boardPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 15;");

        // Initialize hand boxes
        handBoxes = new VBox[4];
        
        // Bottom (Player)
        handBoxes[0] = createHandBox(gameState.getPlayers().get(0), 0);
        boardPane.setBottom(wrapWithAvatarBox(handBoxes[0], 0));
        BorderPane.setAlignment(handBoxes[0], Pos.CENTER);

        // Right (CPU 1)
        handBoxes[1] = createHandBox(gameState.getPlayers().get(1), 1);
        boardPane.setRight(wrapWithAvatarBox(handBoxes[1], 1));
        BorderPane.setAlignment(handBoxes[1], Pos.CENTER_RIGHT);

        // Top (CPU 2)
        handBoxes[2] = createHandBox(gameState.getPlayers().get(2), 2);
        boardPane.setTop(wrapWithAvatarBox(handBoxes[2], 2));
        BorderPane.setAlignment(handBoxes[2], Pos.CENTER);

        // Left (CPU 3)
        handBoxes[3] = createHandBox(gameState.getPlayers().get(3), 3);
        boardPane.setLeft(wrapWithAvatarBox(handBoxes[3], 3));
        BorderPane.setAlignment(handBoxes[3], Pos.CENTER_LEFT);

        // Center: current card area
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.12); -fx-background-radius: 20; -fx-padding: 30;");
        // Draw pile (placeholder)
        HBox piles = new HBox(30);
        piles.setAlignment(Pos.CENTER);
        ImageView drawPile = new ImageView(getCardBackImage());
        drawPile.setFitWidth(80);
        drawPile.setFitHeight(120);
        drawPile.setRotate(-10);
        drawPile.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");
        topCardBox = new VBox();
        topCardBox.setAlignment(Pos.CENTER);
        updateTopCard();
        piles.getChildren().addAll(drawPile, topCardBox);
        // Color indicator setup
        colorIndicatorPane.setVisible(false);
        colorIndicatorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        colorIndicatorLabel.setTextFill(Color.WHITE);
        colorIndicatorPane.getChildren().add(colorIndicatorLabel);
        colorIndicatorPane.setStyle("-fx-background-radius: 30; -fx-padding: 10 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);");
        centerBox.getChildren().addAll(piles, colorIndicatorPane);
        boardPane.setCenter(centerBox);

        root.setCenter(boardPane);

        // Draw card button (for human)
        Button drawButton = new Button("Draw Card");
        drawButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        drawButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-background-radius: 12; -fx-padding: 16 36; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);");
        drawButton.setOnMouseEntered(e -> drawButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                                                           "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                                           "-fx-padding: 16 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 6);"));
        drawButton.setOnMouseExited(e -> drawButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                                          "-fx-font-weight: bold; -fx-background-radius: 12; " +
                                                          "-fx-padding: 16 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);"));
        drawButton.setDisable(!gameState.getCurrentPlayerObj().isHuman() || gameState.isGameEnded());
        drawButton.setOnAction(e -> {
            if (gameState.getCurrentPlayerObj().isHuman() && !gameState.isGameEnded()) {
                // Only allow drawing if there is no pending draw
                if (gameState.getPendingDraw() > 0) {
                    messageLabel.setText("You must wait for the forced draw to be resolved!");
                    return;
                }
                drawUntilPlayableHuman();
            }
        });
        bottomBox.getChildren().add(drawButton);

        colorIndicatorFade.setOnFinished(e -> colorIndicatorPane.setVisible(false));
    }

    public BorderPane getView() {
        return root;
    }

    private VBox createHandBox(Player p, int index) {
        VBox handBox = new VBox(10);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(15));
        handBox.setStyle("-fx-background-color: " + 
                (index == gameState.getCurrentPlayer() ? "rgba(46, 204, 113, 0.2)" : "rgba(255, 255, 255, 0.1)") + 
                "; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-width: 2;" +
                (index == gameState.getCurrentPlayer() ? "-fx-border-color: #2ECC71;" : "-fx-border-color: rgba(255, 255, 255, 0.2);"));

        Label nameLabel = new Label(p.getName() + (p.isHuman() ? " (You)" : ""));
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);

        // UNO indicator (badge)
        StackPane unoBadge = null;
        if (p.getHandSize() == 1) {
            Label unoLabel = new Label("UNO");
            unoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            unoLabel.setTextFill(Color.WHITE);
            unoLabel.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 20; -fx-padding: 4 12; -fx-border-color: white; -fx-border-width: 2;");
            unoBadge = new StackPane(unoLabel);
            unoBadge.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);");
        }

        // Multi-row hand layout for all players
        VBox cardsRows = new VBox(5);
        cardsRows.setAlignment(Pos.CENTER);
        int cardsPerRow = 7;
        int totalCards = p.getHand().size();
        for (int row = 0; row * cardsPerRow < totalCards; row++) {
            HBox cardsBox = new HBox(8);
            cardsBox.setAlignment(Pos.CENTER);
            int start = row * cardsPerRow;
            int end = Math.min(start + cardsPerRow, totalCards);
            for (int i = start; i < end; i++) {
                Card card = p.getHand().get(i);
                ImageView cardImg = new ImageView(getCardImage(card));
                cardImg.setFitWidth(70);
                cardImg.setFitHeight(105);
                Button cardBtn = new Button();
                cardBtn.setGraphic(cardImg);
                cardBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                cardBtn.setDisable(!gameState.canPlay(card) || !gameState.getCurrentPlayerObj().isHuman() || gameState.isGameEnded());
                // Add hover effect for playable cards
                if (gameState.canPlay(card)) {
                    cardBtn.setOnMouseEntered(e -> {
                        if (!cardBtn.isDisabled()) {
                            cardImg.setFitWidth(80);
                            cardImg.setFitHeight(120);
                            cardImg.setTranslateY(-20);
                        }
                    });
                    cardBtn.setOnMouseExited(e -> {
                        if (!cardBtn.isDisabled()) {
                            cardImg.setFitWidth(70);
                            cardImg.setFitHeight(105);
                            cardImg.setTranslateY(0);
                        }
                    });
                }
                cardBtn.setOnAction(e -> {
                    if (!gameState.canPlay(card)) {
                        messageLabel.setText("Illegal move! You can only play a card that matches the color, number, or is a wild card.");
                    } else {
                        if (card.getType() == Card.Type.WILD || card.getType() == Card.Type.WILD_DRAW_FOUR) {
                            createColorSelectionDialog(card, p);
                        } else {
                            boolean played = gameState.playCard(p, card, card.getColor());
                            if (!played) {
                                messageLabel.setText("You can't play this card!");
                            } else {
                                messageLabel.setText("");
                                gameState.nextPlayer();
                                nextTurn();
                            }
                            updateHands();
                        }
                    }
                });
                cardsBox.getChildren().add(cardBtn);
            }
            cardsRows.getChildren().add(cardsBox);
        }
        if (unoBadge != null) {
            handBox.getChildren().addAll(nameLabel, unoBadge, cardsRows);
        } else {
            handBox.getChildren().addAll(nameLabel, cardsRows);
        }
        return handBox;
    }

    private void updateStatus() {
        if (gameState.isGameEnded()) {
            // Find the winner (player with empty hand)
            Player winner = null;
            for (Player p : gameState.getPlayers()) {
                if (p.getHandSize() == 0) {
                    winner = p;
                    break;
                }
            }
            statusLabel.setText("Game Over! Winner: " + (winner != null ? winner.getName() : "Unknown"));
            // Show game over dialog
            Platform.runLater(this::showGameOverDialog);
        } else {
            statusLabel.setText("Turn: " + gameState.getCurrentPlayerObj().getName());
        }
    }

    private void updateDirection() {
        String arrow = gameState.getDirection() == -1 ? "→ Clockwise" : "← Counterclockwise";
        directionLabel.setText("Direction: " + arrow);
        directionLabel.setTextFill(gameState.getDirection() == 1 ? Color.GREEN : Color.BLUE);
    }

    private void updateTopCard() {
        Card top = gameState.getTopCard();
        topCardBox.getChildren().clear();
        if (top == null) {
            // If there's no top card yet, show a placeholder or empty space
            Label placeholder = new Label("Play a card to start");
            placeholder.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            placeholder.setTextFill(Color.WHITE);
            placeholder.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10; -fx-padding: 20;");
            topCardBox.getChildren().add(placeholder);
        } else {
            ImageView topImg = new ImageView(getCardImage(top));
            topImg.setFitWidth(100);
            topImg.setFitHeight(150);
            topImg.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
            topCardBox.getChildren().add(topImg);
        }
    }

    private void updateHands() {
        for (int i = 0; i < 4; i++) {
            Player p = gameState.getPlayers().get(i);
            VBox newHandBox = createHandBox(p, i);
            if (i == 0) {
                ((BorderPane)root.getCenter()).setBottom(newHandBox);
            } else if (i == 1) {
                ((BorderPane)root.getCenter()).setRight(newHandBox);
            } else if (i == 2) {
                ((BorderPane)root.getCenter()).setTop(newHandBox);
            } else if (i == 3) {
                ((BorderPane)root.getCenter()).setLeft(newHandBox);
            }
            handBoxes[i] = newHandBox;
        }
        // Update draw button state
        updateDrawButtonState();
    }

    private void updateDrawButtonState() {
        Button drawButton = (Button)((VBox)root.getBottom()).getChildren().get(1);
        drawButton.setDisable(!gameState.getCurrentPlayerObj().isHuman() || gameState.isGameEnded());
    }

    private void nextTurn() {
        updateHands();
        updateTopCard();
        updateDirection();
        if (!gameState.isGameEnded()) {
            updateStatus();
            // Forced draw for any player at the start of their turn
            if (gameState.getPendingDraw() > 0) {
                int toDraw = gameState.getPendingDraw();
                int playerIndex = gameState.getCurrentPlayer();
                drawForcedCardsAnimated(playerIndex, toDraw, () -> {
                    String who = gameState.getCurrentPlayerObj().getName();
                    messageLabel.setText(who + " drew " + toDraw + " cards!");
                    gameState.nextPlayer();
                    nextTurn();
                });
                return;
            }
            if (!gameState.getCurrentPlayerObj().isHuman()) {
                showCpuMovesSequentially();
            }
        } else {
            updateStatus();
        }
    }

    private void showCpuMovesSequentially() {
        if (gameState.isGameEnded() || gameState.getCurrentPlayerObj().isHuman()) {
            updateStatus();
            return;
        }

        Player currentPlayer = gameState.getCurrentPlayerObj();
        messageLabel.setText(currentPlayer.getName() + " is thinking...");

        // First pause for thinking
        PauseTransition thinkPause = new PauseTransition(Duration.seconds(1.5));
        thinkPause.setOnFinished(e -> {
            try {
                // If there's a pending draw, handle it
                if (gameState.getPendingDraw() > 0) {
                    int toDraw = gameState.getPendingDraw();
                    messageLabel.setText(currentPlayer.getName() + " must draw " + toDraw + " cards!");
                    drawForcedCardsAnimated(gameState.getCurrentPlayer(), toDraw, () -> {
                        messageLabel.setText(currentPlayer.getName() + " drew " + toDraw + " cards!");
                        gameState.nextPlayer();
                        nextTurn();
                    });
                    return;
                }
                // Check if player has any playable cards
                boolean canMakeMove = false;
                for (Card card : currentPlayer.getHand()) {
                    if (gameState.canPlay(card)) {
                        canMakeMove = true;
                        break;
                    }
                }
                if (!canMakeMove) {
                    messageLabel.setText(currentPlayer.getName() + " has no playable cards, drawing...");
                    drawUntilPlayableCPU(currentPlayer, () -> {
                        messageLabel.setText(currentPlayer.getName() + " played a card after drawing!");
                        updateHands();
                        updateTopCard();
                        updateDirection();
                        updateStatus();
                        if (!gameState.isGameEnded()) {
                            gameState.nextPlayer();
                            nextTurn();
                        }
                    });
                } else {
                    messageLabel.setText(currentPlayer.getName() + " is deciding which card to play...");
                    PauseTransition decidePause = new PauseTransition(Duration.seconds(1));
                    decidePause.setOnFinished(event -> {
                        // CPU plays a card
                        Card playedCard = null;
                        Card.Color chosenColor = null;
                        for (Card card : new ArrayList<>(currentPlayer.getHand())) {
                            if (gameState.canPlay(card)) {
                                playedCard = card;
                                chosenColor = card.getColor();
                                if (card.getType() == Card.Type.WILD || card.getType() == Card.Type.WILD_DRAW_FOUR) {
                                    int[] colorCounts = new int[4];
                                    for (Card c : currentPlayer.getHand()) {
                                        if (c.getColor() != Card.Color.WILD) {
                                            colorCounts[c.getColor().ordinal()]++;
                                        }
                                    }
                                    int maxCount = 0;
                                    for (int i = 0; i < 4; i++) {
                                        if (colorCounts[i] > maxCount) {
                                            maxCount = colorCounts[i];
                                            chosenColor = Card.Color.values()[i];
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        if (playedCard != null) {
                            boolean played = gameState.playCard(currentPlayer, playedCard, chosenColor);
                            if ((playedCard.getType() == Card.Type.WILD || playedCard.getType() == Card.Type.WILD_DRAW_FOUR) && played) {
                                showColorIndicator(chosenColor);
                            }
                        }
                        messageLabel.setText(currentPlayer.getName() + " played a card!");
                        updateHands();
                        updateTopCard();
                        updateDirection();
                        updateStatus();
                        if (!gameState.isGameEnded()) {
                            gameState.nextPlayer();
                            nextTurn();
                        }
                    });
                    decidePause.play();
                }
            } catch (Exception ex) {
                messageLabel.setText("An error occurred. Please try again.");
                System.err.println("Error in CPU move: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        thinkPause.play();
    }

    // Animate drawing cards from the draw pile to the player's hand
    private void animateDrawCards(int playerIndex, int count, Runnable onFinished) {
        if (count <= 0) {
            if (onFinished != null) onFinished.run();
            return;
        }
        Platform.runLater(() -> {
            // Find the draw pile location (center area)
            HBox piles = (HBox)((VBox)((BorderPane)root.getCenter()).getCenter()).getChildren().get(0);
            ImageView drawPile = (ImageView)piles.getChildren().get(0);
            // Find the hand box for the player
            VBox handBox = handBoxes[playerIndex];
            // Find the VBox of rows (cardsRows)
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
            // Find the last row (HBox)
            HBox lastRow = (HBox) cardsRows.getChildren().get(cardsRows.getChildren().size() - 1);
            // Create a card back image to animate
            ImageView animCard = new ImageView(getCardBackImage());
            animCard.setFitWidth(70);
            animCard.setFitHeight(105);
            ((BorderPane)root.getCenter()).getChildren().add(animCard);
            // Get start/end positions
            double startX = drawPile.localToScene(drawPile.getBoundsInLocal()).getMinX();
            double startY = drawPile.localToScene(drawPile.getBoundsInLocal()).getMinY();
            double endX = lastRow.localToScene(lastRow.getBoundsInLocal()).getMinX() + lastRow.getChildren().size() * 20;
            double endY = lastRow.localToScene(lastRow.getBoundsInLocal()).getMinY();
            animCard.setTranslateX(startX - ((BorderPane)root.getCenter()).localToScene(0,0).getX());
            animCard.setTranslateY(startY - ((BorderPane)root.getCenter()).localToScene(0,0).getY());
            // Animate
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), animCard);
            tt.setToX(endX - ((BorderPane)root.getCenter()).localToScene(0,0).getX());
            tt.setToY(endY - ((BorderPane)root.getCenter()).localToScene(0,0).getY());
            tt.setOnFinished(e -> {
                ((BorderPane)root.getCenter()).getChildren().remove(animCard);
                // Actually draw the card in the model after the animation
                gameState.drawCard(gameState.getPlayers().get(playerIndex));
                updateHands();
                if (count > 1) {
                    animateDrawCards(playerIndex, count - 1, onFinished);
                } else {
                    if (onFinished != null) onFinished.run();
                }
            });
            tt.play();
        });
    }

    // Map card to image file
    private Image getCardImage(Card card) {
        String basePath = "src/main/resources/Colors/";
        String filename = "";
        switch (card.getType()) {
            case NUMBER:
                filename = card.getColor().name().toLowerCase() + card.getNumber() + ".png";
                break;
            case REVERSE:
                filename = card.getColor().name().toLowerCase() + "10.png";
                break;
            case SKIP:
                filename = card.getColor().name().toLowerCase() + "11.png";
                break;
            case DRAW_TWO:
                filename = card.getColor().name().toLowerCase() + "12.png";
                break;
            case WILD:
                filename = "wild13.png";
                break;
            case WILD_DRAW_FOUR:
                filename = "wild14.png";
                break;
        }
        File file = new File(basePath + filename);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        } else {
            // fallback: blank or error image
            return new Image(file.toURI().toString(), 60, 90, true, true, true);
        }
    }

    private Image getCardBackImage() {
        String basePath = "src/main/resources/Colors/back.png";
        File file = new File(basePath);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        } else {
            // fallback: create a simple card back
            return new Image(file.toURI().toString(), 60, 90, true, true, true);
        }
    }

    private String cardToString(Card card) {
        if (card.getType() == Card.Type.NUMBER) {
            return card.getColor() + " " + card.getNumber();
        } else if (card.getType() == Card.Type.WILD) {
            return "WILD";
        } else if (card.getType() == Card.Type.WILD_DRAW_FOUR) {
            return "+4";
        } else if (card.getType() == Card.Type.SKIP) {
            return "Skip";
        } else if (card.getType() == Card.Type.REVERSE) {
            return "Reverse";
        } else if (card.getType() == Card.Type.DRAW_TWO) {
            return "+2";
        }
        return card.toString();
    }

    private String cardButtonStyle(Card card) {
        String color;
        switch (card.getColor()) {
            case RED: color = "#FF5555"; break;
            case YELLOW: color = "#FFEB3B"; break;
            case GREEN: color = "#4CAF50"; break;
            case BLUE: color = "#2196F3"; break;
            case WILD: color = "#333333"; break;
            default: color = "#FFFFFF";
        }
        String textColor = card.getColor() == Card.Color.YELLOW ? "black" : "white";
        return "-fx-background-color: " + color + "; -fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 16;";
    }

    private void createColorSelectionDialog(Card card, Player player) {
        Dialog<Card.Color> dialog = new Dialog<>();
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Select a color for your wild card");

        ButtonType redButton = new ButtonType("Red", ButtonBar.ButtonData.OK_DONE);
        ButtonType yellowButton = new ButtonType("Yellow", ButtonBar.ButtonData.OK_DONE);
        ButtonType greenButton = new ButtonType("Green", ButtonBar.ButtonData.OK_DONE);
        ButtonType blueButton = new ButtonType("Blue", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(redButton, yellowButton, greenButton, blueButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == redButton) return Card.Color.RED;
            if (buttonType == yellowButton) return Card.Color.YELLOW;
            if (buttonType == greenButton) return Card.Color.GREEN;
            if (buttonType == blueButton) return Card.Color.BLUE;
            return null;
        });

        Optional<Card.Color> result = dialog.showAndWait();
        result.ifPresent(color -> {
            boolean played = gameState.playCard(player, card, color);
            if (!played) {
                messageLabel.setText("You can't play this card!");
            } else {
                messageLabel.setText("");
                showColorIndicator(color);
                gameState.nextPlayer();
                nextTurn();
            }
            updateHands();
        });
    }

    // Helper to wrap handBox with avatar/info box
    private VBox wrapWithAvatarBox(VBox handBox, int index) {
        Player p = gameState.getPlayers().get(index);
        VBox wrapper = new VBox(5);
        wrapper.setAlignment(Pos.CENTER);
        // Placeholder avatar: colored circle with initial
        Circle avatar = new Circle(28, getAvatarColor(index));
        Label initial = new Label(p.getName().substring(0, 1));
        initial.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        initial.setTextFill(Color.WHITE);
        StackPane avatarStack = new StackPane(avatar, initial);
        avatarStack.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);");
        // Info box
        Label info = new Label(p.getName() + "  [" + p.getHandSize() + "]");
        info.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        info.setTextFill(Color.WHITE);
        info.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 8; -fx-padding: 4 12;");
        wrapper.getChildren().addAll(avatarStack, info, handBox);
        return wrapper;
    }

    // Helper to get a unique color for each avatar
    private Color getAvatarColor(int index) {
        Color[] colors = { Color.web("#f39c12"), Color.web("#2980b9"), Color.web("#8e44ad"), Color.web("#16a085") };
        return colors[index % colors.length];
    }

    // For human: draw one by one until a playable card is found
    private void drawUntilPlayableHuman() {
        int playerIndex = gameState.getCurrentPlayer();
        Player currentPlayer = gameState.getCurrentPlayerObj();
        
        // First draw a card
        gameState.drawCard(currentPlayer);
        updateHands();
        
        // Check if the drawn card is playable
        boolean found = false;
        for (Card card : currentPlayer.getHand()) {
            if (gameState.canPlay(card)) {
                found = true;
                break;
            }
        }
        
        if (found) {
            messageLabel.setText("You drew a playable card!");
        } else {
            messageLabel.setText("No playable card, drawing another...");
            // Add a small delay before drawing again
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> drawUntilPlayableHuman());
            pause.play();
        }
    }

    private static class CardChoice {
        Card card;
        Card.Color color;
        
        CardChoice(Card card, Card.Color color) {
            this.card = card;
            this.color = color;
        }
    }

    private void drawUntilPlayableCPU(Player cpu, Runnable onDone) {
        Player currentPlayer = gameState.getCurrentPlayerObj();
        
        // First draw a card
        gameState.drawCard(currentPlayer);
        updateHands();
        messageLabel.setText(currentPlayer.getName() + " drew a card...");
        
        // Add a delay to show the drawn card before checking if it's playable
        PauseTransition drawPause = new PauseTransition(Duration.seconds(1.5));
        drawPause.setOnFinished(e -> {
            // Check if the drawn card is playable
            CardChoice choice = findPlayableCard(currentPlayer);
            
            if (choice != null) {
                // Add another delay before playing the card
                PauseTransition playPause = new PauseTransition(Duration.seconds(1));
                playPause.setOnFinished(event -> {
                    gameState.playCard(currentPlayer, choice.card, choice.color);
                    messageLabel.setText(currentPlayer.getName() + " played a card!");
                    if (onDone != null) onDone.run();
                });
                playPause.play();
            } else {
                messageLabel.setText(currentPlayer.getName() + " drew a card but still has no playable cards...");
                // Add a small delay before drawing again
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(event -> drawUntilPlayableCPU(cpu, onDone));
                pause.play();
            }
        });
        drawPause.play();
    }

    private CardChoice findPlayableCard(Player player) {
        for (Card card : new ArrayList<>(player.getHand())) {
            if (gameState.canPlay(card)) {
                Card.Color chosenColor = card.getColor();
                if (card.getType() == Card.Type.WILD || card.getType() == Card.Type.WILD_DRAW_FOUR) {
                    int[] colorCounts = new int[4];
                    for (Card c : player.getHand()) {
                        if (c.getColor() != Card.Color.WILD) {
                            colorCounts[c.getColor().ordinal()]++;
                        }
                    }
                    int maxCount = 0;
                    for (int i = 0; i < 4; i++) {
                        if (colorCounts[i] > maxCount) {
                            maxCount = colorCounts[i];
                            chosenColor = Card.Color.values()[i];
                        }
                    }
                }
                return new CardChoice(card, chosenColor);
            }
        }
        return null;
    }

    // For forced draws (e.g. +2, +4): animate drawing cards one by one
    private void drawForcedCardsAnimated(int playerIndex, int count, Runnable onFinished) {
        if (count <= 0) {
            if (onFinished != null) onFinished.run();
            return;
        }
        animateDrawCards(playerIndex, 1, () -> {
            updateHands();
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> drawForcedCardsAnimated(playerIndex, count - 1, onFinished));
            pause.play();
        });
    }

    // Show color indicator for wild cards
    private void showColorIndicator(Card.Color color) {
        String colorName = color.name().substring(0,1) + color.name().substring(1).toLowerCase();
        colorIndicatorLabel.setText("New Color: " + colorName);
        String bgColor;
        switch (color) {
            case RED: bgColor = "#FF5555"; break;
            case YELLOW: bgColor = "#FFEB3B"; break;
            case GREEN: bgColor = "#4CAF50"; break;
            case BLUE: bgColor = "#2196F3"; break;
            default: bgColor = "#333333";
        }
        colorIndicatorPane.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 30; -fx-padding: 10 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);");
        colorIndicatorPane.setVisible(true);
        // Reset and restart the fade transition
        colorIndicatorFade.stop();
        colorIndicatorFade.playFromStart();
    }

    private void showGameOverDialog() {
        // Find the winner (player with empty hand)
        Player winner = null;
        for (Player p : gameState.getPlayers()) {
            if (p.getHandSize() == 0) {
                winner = p;
                break;
            }
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Game Over! " + (winner != null ? winner.getName() + " wins!" : "Unknown winner"));

        // Create a custom dialog content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        // Winner announcement with style
        Label winnerLabel = new Label(winner != null ? winner.getName() + " wins!" : "Game Over!");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        winnerLabel.setTextFill(Color.WHITE);
        winnerLabel.setStyle("-fx-background-color: rgba(46, 204, 113, 0.8); -fx-background-radius: 10; -fx-padding: 10 20;");

        // Final scores
        VBox scoresBox = new VBox(10);
        scoresBox.setAlignment(Pos.CENTER);
        for (Player p : gameState.getPlayers()) {
            Label scoreLabel = new Label(p.getName() + ": " + p.getHandSize() + " cards left");
            scoreLabel.setFont(Font.font("Arial", 16));
            scoreLabel.setTextFill(Color.WHITE);
            scoresBox.getChildren().add(scoreLabel);
        }

        content.getChildren().addAll(winnerLabel, scoresBox);

        // Buttons
        ButtonType replayButton = new ButtonType("Play Again", ButtonBar.ButtonData.OK_DONE);
        ButtonType menuButton = new ButtonType("Main Menu", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(replayButton, menuButton);

        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");
        dialog.getDialogPane().setContent(content);

        // Handle button actions
        Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == replayButton) {
                // Restart the game
                gameState = new GameState(gameState.getPlayers().get(0).getName());
                updateHands();
                updateTopCard();
                updateDirection();
                updateStatus();
            } else {
                // Return to main menu
                MainMenuController mainMenu = new MainMenuController();
                Scene menuScene = new Scene(mainMenu.getView(), 800, 600);
                UnoApp.changeScene(menuScene, "UNO Game - Main Menu");
            }
        });
    }
} 