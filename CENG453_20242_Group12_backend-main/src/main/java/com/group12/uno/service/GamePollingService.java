package com.group12.uno.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class GamePollingService {
    // Store game states
    private final Map<String, GameState> gameStates = new ConcurrentHashMap<>();
    // Store game updates with timestamps
    private final Map<String, List<GameUpdate>> gameUpdates = new ConcurrentHashMap<>();

    public Map<String, Object> getUpdates(String gameId, String since) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            return Map.of("error", "Game not found");
        }

        long sinceTimestamp = Long.parseLong(since);
        List<GameUpdate> updates = gameUpdates.getOrDefault(gameId, new ArrayList<>());
        
        // Filter updates that are newer than the since timestamp
        List<GameUpdate> newUpdates = updates.stream()
            .filter(update -> update.timestamp > sinceTimestamp)
            .collect(Collectors.toList());

        // Create a map of player card counts
        Map<String, Integer> playerCardCounts = new HashMap<>();
        for (String playerId : gameState.getPlayerIds()) {
            playerCardCounts.put(playerId, gameState.getPlayerHand(playerId).size());
        }

        return Map.of(
            "gameState", gameState,
            "updates", newUpdates,
            "timestamp", System.currentTimeMillis(),
            "playerCardCounts", playerCardCounts
        );
    }

    public Map<String, Object> processAction(Map<String, Object> action) {
        String gameId = (String) action.get("gameId");
        String actionType = (String) action.get("type");
        Object actionData = action.get("data");

        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            return Map.of("error", "Game not found");
        }

        // Process the action and update game state
        processGameAction(gameState, actionType, actionData);

        // Create and store the update
        GameUpdate update = new GameUpdate(
            System.currentTimeMillis(),
            actionType,
            actionData
        );
        gameUpdates.computeIfAbsent(gameId, k -> new CopyOnWriteArrayList<>())
            .add(update);

        return Map.of(
            "success", true,
            "gameState", gameState,
            "timestamp", System.currentTimeMillis()
        );
    }

    public Map<String, Object> createGame(String gameId, List<String> playerIds) {
        GameState gameState = new GameState(playerIds);
        gameStates.put(gameId, gameState);
        gameUpdates.put(gameId, new CopyOnWriteArrayList<>());
        // Do NOT deal cards yet; wait for startGame
        return Map.of(
            "success", true,
            "gameState", gameState,
            "timestamp", System.currentTimeMillis()
        );
    }

    public Map<String, Object> joinGame(String gameId, String playerId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            return Map.of("error", "Game not found");
        }
        if (gameState.playerIds.contains(playerId)) {
            return Map.of("error", "Player already in game");
        }
        if (gameState.started) {
            return Map.of("error", "Game already started");
        }
        gameState.playerIds.add(playerId);
        gameState.playerHands.put(playerId, new ArrayList<>());
        gameState.unoCalled.put(playerId, false);

        // Add a game update for the new player joining
        GameUpdate update = new GameUpdate(
            System.currentTimeMillis(),
            "PLAYER_JOINED",
            Map.of("playerId", playerId)
        );
        gameUpdates.computeIfAbsent(gameId, k -> new CopyOnWriteArrayList<>())
            .add(update);

        return Map.of(
            "success", true,
            "gameState", gameState,
            "timestamp", System.currentTimeMillis()
        );
    }

    public Map<String, Object> startGame(String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            return Map.of("error", "Game not found");
        }
        if (gameState.started) {
            return Map.of("error", "Game already started");
        }
        if (gameState.playerIds.size() != 2) {
            return Map.of("error", "Exactly 2 players required to start");
        }
        
        // No CPU players - just deal cards and start
        dealInitialCards(gameState);
        gameState.started = true;
        return Map.of(
            "success", true,
            "gameState", gameState,
            "timestamp", System.currentTimeMillis()
        );
    }

    private void processGameAction(GameState gameState, String actionType, Object actionData) {
        Map<String, Object> data = (Map<String, Object>) actionData;
        String playerId = (String) data.get("playerId");
        
        // Verify it's a human player
        if (playerId == null || playerId.startsWith("CPU")) {
            throw new IllegalStateException("Invalid player");
        }
        
        Card.Color chosenColor = null;
        if (data.containsKey("chosenColor")) {
            chosenColor = Card.Color.valueOf((String) data.get("chosenColor"));
        }
        
        switch (actionType) {
            case "PLAY_CARD":
                Card card = Card.fromMap((Map<String, Object>) data.get("card"));
                // If pendingDraw > 0, only allow stacking same type
                if (gameState.pendingDraw > 0) {
                    Card top = gameState.topCard;
                    boolean isStack = (card.getType() == Card.Type.DRAW_TWO && top.getType() == Card.Type.DRAW_TWO) ||
                                      (card.getType() == Card.Type.WILD_DRAW_FOUR && top.getType() == Card.Type.WILD_DRAW_FOUR);
                    if (!isStack) {
                        // Not a stack, must draw
                        for (int i = 0; i < gameState.pendingDraw; i++) {
                            drawCard(gameState, playerId);
                        }
                        gameState.pendingDraw = 0;
                        // Don't skip turn after drawing
                        return;
                    }
                }
                playCard(gameState, playerId, card, chosenColor);
                nextTurn(gameState);
                break;
            case "DRAW_CARD":
                // Draw exactly one card, regardless of circumstances
                Card drawnCard = gameState.drawFromDeck();
                gameState.addCardToPlayer(playerId, drawnCard);
                break;
            case "SKIP_TURN":
                skipTurn(gameState);
                break;
            case "SAY_UNO":
                sayUno(gameState, playerId);
                break;
            case "CHALLENGE_UNO":
                challengeUno(gameState, playerId);
                break;
        }
    }

    private void dealInitialCards(GameState gameState) {
        for (String playerId : gameState.getPlayerIds()) {
            for (int i = 0; i < 7; i++) {
                drawCard(gameState, playerId);
            }
        }
        
        // Draw first card for the discard pile
        Card firstCard;
        do {
            firstCard = gameState.drawFromDeck();
        } while (firstCard.isSpecialCard());
        
        gameState.setTopCard(firstCard);
        gameState.currentColor = firstCard.getColor();
        gameState.setCurrentPlayer(gameState.getPlayerIds().get(0));
    }

    private void playCard(GameState gameState, String playerId, Card card, Card.Color chosenColor) {
        if (!gameState.canPlayCard(playerId, card)) {
            throw new IllegalStateException("Invalid card play");
        }
        gameState.removeCardFromPlayer(playerId, card);
        gameState.setTopCard(card);
        gameState.forcedColor = null;
        // Handle special cards
        switch (card.getType()) {
            case SKIP:
                gameState.skipNext = true;
                break;
            case REVERSE:
                gameState.direction *= -1;
                if (gameState.playerIds.size() == 2) {
                    gameState.skipNext = true; // In 2-player, Reverse acts as Skip
                }
                break;
            case DRAW_TWO:
                gameState.pendingDraw = gameState.pendingDraw == 0 ? 2 : gameState.pendingDraw + 2;
                // Remove skip after drawing
                break;
            case WILD:
                if (chosenColor == null) {
                    throw new IllegalStateException("Color must be chosen for wild card");
                }
                gameState.forcedColor = chosenColor;
                gameState.currentColor = chosenColor;
                break;
            case WILD_DRAW_FOUR:
                if (chosenColor == null) {
                    throw new IllegalStateException("Color must be chosen for wild draw four card");
                }
                gameState.pendingDraw = gameState.pendingDraw == 0 ? 4 : gameState.pendingDraw + 4;
                // Remove skip after drawing
                gameState.forcedColor = chosenColor;
                gameState.currentColor = chosenColor;
                break;
            default:
                gameState.currentColor = card.getColor();
                break;
        }
        if (gameState.getPlayerHand(playerId).isEmpty()) {
            gameState.setWinner(playerId);
            gameState.gameEnded = true;
            return;
        }
    }

    private void drawCard(GameState gameState, String playerId) {
        if (gameState.isDeckEmpty()) {
            gameState.reshuffleDeck();
        }
        Card card = gameState.drawFromDeck();
        gameState.addCardToPlayer(playerId, card);
    }

    private void skipTurn(GameState gameState) {
        nextTurn(gameState);
    }

    private void sayUno(GameState gameState, String playerId) {
        if (gameState.getPlayerHand(playerId).size() == 1) {
            gameState.setUnoCalled(playerId, true);
        }
    }

    private void challengeUno(GameState gameState, String playerId) {
        String challengedPlayer = gameState.getPreviousPlayer();
        if (!gameState.isUnoCalled(challengedPlayer)) {
            drawCard(gameState, challengedPlayer);
            drawCard(gameState, challengedPlayer);
        }
    }

    private void nextTurn(GameState gameState) {
        // Handle forced draws first
        if (gameState.pendingDraw > 0) {
            gameState.setCurrentPlayer(gameState.getNextPlayer());
            gameState.skipNext = false; // Reset skip after forced draw
            for (int i = 0; i < gameState.pendingDraw; i++) {
                gameState.addCardToPlayer(gameState.currentPlayer, gameState.drawFromDeck());
            }
            gameState.pendingDraw = 0;
            // Skip this player's turn after drawing
            gameState.setCurrentPlayer(gameState.getNextPlayer());
            return;
        }
        // Normal turn progression
        int dir = gameState.direction;
        List<String> ids = gameState.playerIds;
        int idx = ids.indexOf(gameState.currentPlayer);
        int nextIdx = (idx + dir + ids.size()) % ids.size();
        gameState.setCurrentPlayer(ids.get(nextIdx));
        // Handle skip (from Skip, Draw Two, or Wild Draw Four)
        if (gameState.skipNext) {
            idx = ids.indexOf(gameState.currentPlayer);
            nextIdx = (idx + dir + ids.size()) % ids.size();
            gameState.setCurrentPlayer(ids.get(nextIdx));
            gameState.skipNext = false;
        }
    }

    public Map<String, Object> getLobby(String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            return Map.of("error", "Game not found");
        }
        return Map.of(
            "players", gameState.playerIds,
            "started", gameState.started
        );
    }

    private boolean isCpuPlayer(String playerId) {
        return false; // No CPU players allowed
    }

    public static class GameState {
        public final List<String> playerIds;
        public final Map<String, List<Card>> playerHands;
        public final List<Card> deck;
        private final Map<String, Boolean> unoCalled = new HashMap<>();
        public boolean started = false;
        private Card topCard;
        private String currentPlayer;
        private String winner;
        private int direction = -1; // 1 for clockwise, -1 for counterclockwise
        private boolean gameEnded = false;
        private Card.Color forcedColor = null; // For wild cards
        private int pendingDraw = 0; // Number of cards the next player must draw
        private boolean skipNext = false;
        private Card.Color currentColor;

        public GameState(List<String> playerIds) {
            // Verify no CPU players
            if (playerIds.stream().anyMatch(id -> id == null || id.startsWith("CPU"))) {
                throw new IllegalArgumentException("CPU players are not allowed");
            }
            
            this.playerIds = new ArrayList<>(playerIds);
            this.playerHands = new HashMap<>();
            this.deck = new ArrayList<>();
            initializeDeck();
            
            for (String playerId : playerIds) {
                playerHands.put(playerId, new ArrayList<>());
                unoCalled.put(playerId, false);
            }
        }

        private void initializeDeck() {
            // Add number cards (0-9) for each color
            for (Card.Color color : Card.Color.values()) {
                if (color != Card.Color.BLACK) {
                    // Add one zero card
                    deck.add(new Card(color, Card.Type.NUMBER, 0));
                    // Add two of each number 1-9
                    for (int number = 1; number <= 9; number++) {
                        deck.add(new Card(color, Card.Type.NUMBER, number));
                        deck.add(new Card(color, Card.Type.NUMBER, number));
                    }
                    // Add two of each action card
                    deck.add(new Card(color, Card.Type.SKIP, -1));
                    deck.add(new Card(color, Card.Type.SKIP, -1));
                    deck.add(new Card(color, Card.Type.REVERSE, -1));
                    deck.add(new Card(color, Card.Type.REVERSE, -1));
                    deck.add(new Card(color, Card.Type.DRAW_TWO, -1));
                    deck.add(new Card(color, Card.Type.DRAW_TWO, -1));
                }
            }
            // Add wild cards
            for (int i = 0; i < 4; i++) {
                deck.add(new Card(Card.Color.BLACK, Card.Type.WILD, -1));
                deck.add(new Card(Card.Color.BLACK, Card.Type.WILD_DRAW_FOUR, -1));
            }
            Collections.shuffle(deck);
        }

        public List<String> getPlayerIds() {
            return Collections.unmodifiableList(playerIds);
        }

        public List<Card> getPlayerHand(String playerId) {
            List<Card> hand = playerHands.get(playerId);
            if (hand == null) return Collections.emptyList();
            return Collections.unmodifiableList(hand);
        }

        public boolean isPlayerTurn(String playerId) {
            return currentPlayer != null && currentPlayer.equals(playerId);
        }

        public boolean canPlayCard(String playerId, Card card) {
            if (!isPlayerTurn(playerId)) return false;
            List<Card> hand = playerHands.get(playerId);
            if (hand == null || !hand.contains(card)) return false;
            if (topCard == null) return false;
            // If there's a pending draw, can only stack same type
            if (pendingDraw > 0) {
                if (card.getType() == Card.Type.DRAW_TWO && topCard.getType() == Card.Type.DRAW_TWO) return true;
                if (card.getType() == Card.Type.WILD_DRAW_FOUR && topCard.getType() == Card.Type.WILD_DRAW_FOUR) return true;
                return false;
            }
            // If there's a forced color (from wild card), must match that color
            if (forcedColor != null) {
                return card.getColor().name().equals(forcedColor.name()) || card.getColor() == Card.Color.BLACK;
            }
            // Normal play rules
            return card.getColor() == topCard.getColor() ||
                    (card.getType() == Card.Type.NUMBER && topCard.getType() == Card.Type.NUMBER && card.getNumber() == topCard.getNumber()) ||
                    card.getColor() == Card.Color.BLACK ||
                    (card.getType() == topCard.getType() && card.getType() != Card.Type.NUMBER);
        }

        public Card drawFromDeck() {
            return deck.remove(deck.size() - 1);
        }

        public void addCardToPlayer(String playerId, Card card) {
            playerHands.get(playerId).add(card);
        }

        public void removeCardFromPlayer(String playerId, Card card) {
            playerHands.get(playerId).remove(card);
        }

        public void setTopCard(Card card) {
            this.topCard = card;
        }

        public void setCurrentPlayer(String playerId) {
            this.currentPlayer = playerId;
        }

        public String getNextPlayer() {
            int currentIndex = playerIds.indexOf(currentPlayer);
            int nextIndex;
            if (direction == 1) {
                nextIndex = (currentIndex + 1) % playerIds.size();
            } else {
                nextIndex = (currentIndex - 1 + playerIds.size()) % playerIds.size();
            }
            return playerIds.get(nextIndex);
        }

        public String getPreviousPlayer() {
            int currentIndex = playerIds.indexOf(currentPlayer);
            int prevIndex;
            if (direction == 1) {
                prevIndex = (currentIndex - 1 + playerIds.size()) % playerIds.size();
            } else {
                prevIndex = (currentIndex + 1) % playerIds.size();
            }
            return playerIds.get(prevIndex);
        }

        public void reverseDirection() {
            direction *= -1;
        }

        public boolean isDeckEmpty() {
            return deck.isEmpty();
        }

        public void reshuffleDeck() {
            Card currentTop = topCard;
            deck.addAll(playerHands.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()));
            Collections.shuffle(deck);
            topCard = currentTop;
        }

        public void setWinner(String playerId) {
            this.winner = playerId;
        }

        public String getWinner() {
            return winner;
        }

        public void setUnoCalled(String playerId, boolean called) {
            unoCalled.put(playerId, called);
        }

        public boolean isUnoCalled(String playerId) {
            return unoCalled.getOrDefault(playerId, false);
        }

        public boolean isGameEnded() {
            return gameEnded;
        }

        public Card getTopCard() { return topCard; }
        public String getCurrentPlayer() { return currentPlayer; }
        public int getDirection() { return direction; }
        public Map<String, Boolean> getUnoCalled() { return unoCalled; }
        public Card.Color getCurrentColor() { return currentColor; }
        public int getPendingDraw() { return pendingDraw; }
        public Card.Color getForcedColor() { return forcedColor; }
        public boolean getSkipNext() { return skipNext; }
    }

    public static class Card {
        private final Color color;
        private final Type type;
        private final int number;

        public Card(Color color, Type type, int number) {
            this.color = color;
            this.type = type;
            this.number = number;
        }

        public static Card fromMap(Map<String, Object> map) {
            Color color = Color.valueOf((String) map.get("color"));
            Type type = Type.valueOf((String) map.get("type"));
            int number = ((Number) map.get("number")).intValue();
            return new Card(color, type, number);
        }

        public Color getColor() {
            return color;
        }

        public Type getType() {
            return type;
        }

        public int getNumber() {
            return number;
        }

        public boolean isSpecialCard() {
            return type != Type.NUMBER;
        }

        public enum Color {
            RED, BLUE, GREEN, YELLOW, BLACK
        }

        public enum Type {
            NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Card card = (Card) o;
            return number == card.number && color == card.color && type == card.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, type, number);
        }
    }

    public static class GameUpdate {
        private final long timestamp;
        private final String type;
        private final Object data;

        public GameUpdate(long timestamp, String type, Object data) {
            this.timestamp = timestamp;
            this.type = type;
            this.data = data;
        }

        public long getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public Object getData() { return data; }
    }
} 