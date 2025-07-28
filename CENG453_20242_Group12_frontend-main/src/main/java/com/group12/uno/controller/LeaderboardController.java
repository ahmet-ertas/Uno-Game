package com.group12.uno.controller;

import com.group12.uno.UnoApp;
import com.group12.uno.model.LeaderboardEntry;
import com.group12.uno.service.ApiService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class LeaderboardController {

    private VBox root;
    private TableView<LeaderboardEntry> table;
    private ObservableList<LeaderboardEntry> data;

    public LeaderboardController() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("UNO Leaderboard");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Verdana", 28));

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button weeklyBtn = createButton("Weekly", "#ffb400");
        Button monthlyBtn = createButton("Monthly", "#1b6ca8");
        Button allTimeBtn = createButton("All-Time", "#4caf50");
        Button backBtn = createButton("Back", "#d7263d");

        weeklyBtn.setOnAction(e -> loadLeaderboard("weekly"));
        monthlyBtn.setOnAction(e -> loadLeaderboard("monthly"));
        allTimeBtn.setOnAction(e -> loadLeaderboard("all-time"));
        backBtn.setOnAction(e -> {
            MainMenuController main = new MainMenuController();
            UnoApp.changeScene(new Scene(main.getView(), 600, 400), "UNO Game - Main Menu");
        });

        buttonBox.getChildren().addAll(weeklyBtn, monthlyBtn, allTimeBtn, backBtn);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: white;");

        TableColumn<LeaderboardEntry, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUsername()));

        TableColumn<LeaderboardEntry, Number> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTotalScore()));

        TableColumn<LeaderboardEntry, Number> playedCol = new TableColumn<>("Games");
        playedCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getGamesPlayed()));

        TableColumn<LeaderboardEntry, Number> winsCol = new TableColumn<>("Wins");
        winsCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getGamesWon()));

        TableColumn<LeaderboardEntry, Number> rateCol = new TableColumn<>("Win Rate");
        rateCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getWinRate()));

        table.getColumns().addAll(nameCol, scoreCol, playedCol, winsCol, rateCol);

        data = FXCollections.observableArrayList();
        table.setItems(data);

        root.getChildren().addAll(title, buttonBox, table);

        // Açılışta weekly leaderboard getir
        loadLeaderboard("weekly");
    }

    public VBox getView() {
        return root;
    }

    private void loadLeaderboard(String type) {
        List<LeaderboardEntry> entries = ApiService.getLeaderboard(type);
        data.setAll(entries);
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font(14));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8;");
        return btn;
    }
}
