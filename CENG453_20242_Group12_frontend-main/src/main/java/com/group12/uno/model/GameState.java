
package com.group12.uno.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {
    private final List<Player> players = new ArrayList<>();
    private final Deck deck = new Deck();
    private int currentPlayer = 0;
    private int direction = -1; // 1 for clockwise, -1 for counterclockwise
    private boolean gameEnded = false;
    private final Random random = new Random();
    private int pendingDraw = 0; // Number of cards the next player must draw
    private Card.Color forcedColor = null; // For wild cards
    // Track if the last card played was a Skip or Reverse (for correct skipping)
    private boolean skipNext = false;

    public GameState(String humanName) {
        // Initialize players in the correct order for clockwise movement
        // Player 0: Bottom (Human)
        // Player 1: Right
        // Player 2: Top
        // Player 3: Left
        players.add(new Player(humanName, true)); // Bottom
        players.add(new Player("CPU 1", false));  // Right
        players.add(new Player("CPU 2", false));  // Top
        players.add(new Player("CPU 3", false));  // Left

        // Deal 7 cards to each player
        for (int i = 0; i < 7; i++) {
            for (Player p : players) {
                p.addCard(deck.draw());
            }
        }
        // Start with a non-wild card
        Card first;
        do {
            first = deck.draw();
        } while (first.getType() != Card.Type.NUMBER);
        deck.startWith(first);
    }

    public List<Player> getPlayers() { return players; }
    public Deck getDeck() { return deck; }
    public int getCurrentPlayer() { return currentPlayer; }
    public int getDirection() { return direction; }
    public boolean isGameEnded() { return gameEnded; }
    public Card getTopCard() { return deck.getTopDiscard(); }
    public int getPendingDraw() { return pendingDraw; }
    public Card.Color getForcedColor() { return forcedColor; }

    public Player getCurrentPlayerObj() { return players.get(currentPlayer); }

    public boolean canPlay(Card card) {
        Card top = getTopCard();

        // If there's a pending draw, can only play matching Draw Two/Four
        if (pendingDraw > 0) {
            if (card.getType() == Card.Type.DRAW_TWO && top.getType() == Card.Type.DRAW_TWO) {
                return true;
            }
            if (card.getType() == Card.Type.WILD_DRAW_FOUR && top.getType() == Card.Type.WILD_DRAW_FOUR) {
                return true;
            }
            return false;
        }

        // If there's a forced color (from wild card), must match that color
        if (forcedColor != null) {
            return card.getColor() == forcedColor || card.getColor() == Card.Color.WILD;
        }

        // Normal play rules
        return card.getColor() == top.getColor() ||
                (card.getType() == Card.Type.NUMBER && top.getType() == Card.Type.NUMBER && card.getNumber() == top.getNumber()) ||
                card.getColor() == Card.Color.WILD ||
                (card.getType() == top.getType() && card.getType() != Card.Type.NUMBER);
    }

    public boolean playCard(Player player, Card card, Card.Color chosenColor) {
        if (!canPlay(card)) return false;
        player.removeCard(card);
        deck.discard(card);
        forcedColor = null;
        // Handle special cards
        switch (card.getType()) {
            case SKIP:
                skipNext = true;
                break;
            case REVERSE:
                direction *= -1;
                if (players.size() == 2) {
                    skipNext = true; // In 2-player, Reverse acts as Skip
                }
                break;
            case DRAW_TWO:
                pendingDraw = 2; // force next player to draw 2 and skip
                skipNext = true;
                break;
            case WILD:
                forcedColor = chosenColor;
                break;
            case WILD_DRAW_FOUR:
                pendingDraw = 4; // force next player to draw 4 and skip
                skipNext = true;
                forcedColor = chosenColor;
                break;
            default:
                break;
        }
        // Check win
        if (player.getHandSize() == 0) {
            gameEnded = true;
        }
        return true;
    }

    public void drawCard(Player player) {
        if (deck.isEmpty()) {
            deck.reshuffle();
        }
        player.addCard(deck.draw());
    }

    private boolean isDeckExhausted() {
        return deck.isEmpty();
    }

    public void nextPlayer() {
        // Handle forced draws first
        if (pendingDraw > 0) {
            currentPlayer = (currentPlayer + direction + players.size()) % players.size();
            skipNext = false; // Reset skip after forced draw
            for (int i = 0; i < pendingDraw; i++) {
                getCurrentPlayerObj().addCard(deck.draw());
            }
            pendingDraw = 0;
            // Skip this player's turn after drawing

            return;
        }

        // Normal turn progression
        currentPlayer = (currentPlayer + direction + players.size()) % players.size();

        // Handle skip (from Skip, Draw Two, or Wild Draw Four)
        if (skipNext) {
            currentPlayer = (currentPlayer + direction + players.size()) % players.size();
            skipNext = false;
        }
    }

    // Simple CPU logic: play first valid card, else draw
    public void cpuPlay() {
        Player cpu = getCurrentPlayerObj();

        // If forced to draw, do nothing (handled in nextPlayer)
        if (pendingDraw > 0) {
            return;
        }

        // Draw until a playable card is found, then play it
        while (true) {
            boolean found = false;
            for (Card card : new ArrayList<>(cpu.getHand())) {
                if (canPlay(card)) {
                    Card.Color chosen = card.getColor();
                    if (card.getType() == Card.Type.WILD || card.getType() == Card.Type.WILD_DRAW_FOUR) {
                        // Choose most common color in hand
                        int[] colorCounts = new int[4];
                        for (Card c : cpu.getHand()) {
                            if (c.getColor() != Card.Color.WILD) {
                                colorCounts[c.getColor().ordinal()]++;
                            }
                        }
                        int maxCount = 0;
                        for (int i = 0; i < 4; i++) {
                            if (colorCounts[i] > maxCount) {
                                maxCount = colorCounts[i];
                                chosen = Card.Color.values()[i];
                            }
                        }
                    }
                    playCard(cpu, card, chosen);
                    found = true;
                    break;
                }
            }
            if (found) break;
            drawCard(cpu);
        }
    }
}
