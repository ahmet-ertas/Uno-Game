package com.group12.uno.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final boolean isHuman;
    private final List<Card> hand = new ArrayList<>();

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
    }

    public String getName() { return name; }
    public boolean isHuman() { return isHuman; }
    public List<Card> getHand() { return hand; }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public int getHandSize() {
        return hand.size();
    }
}