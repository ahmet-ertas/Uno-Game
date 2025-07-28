package com.group12.uno.model;

import java.util.*;
import static com.group12.uno.model.Card.*;

public class Deck {
    private final Stack<Card> drawPile = new Stack<>();
    private final Stack<Card> discardPile = new Stack<>();

    public Deck() {
        List<Card> cards = new ArrayList<>();
        // Add number cards (0-9, two of each except 0)
        for (Color color : Color.values()) {
            if (color == Color.WILD) continue;
            cards.add(new Card(color, Type.NUMBER, 0));
            for (int i = 1; i <= 9; i++) {
                cards.add(new Card(color, Type.NUMBER, i));
                cards.add(new Card(color, Type.NUMBER, i));
            }
            // Add two of each action card
            for (int i = 0; i < 2; i++) {
                cards.add(new Card(color, Type.SKIP));
                cards.add(new Card(color, Type.REVERSE));
                cards.add(new Card(color, Type.DRAW_TWO));
            }
        }
        // Add wild cards
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Color.WILD, Type.WILD));
            cards.add(new Card(Color.WILD, Type.WILD_DRAW_FOUR));
        }
        Collections.shuffle(cards);
        drawPile.addAll(cards);
    }

    public Card draw() {
        if (drawPile.isEmpty()) {
            // Reshuffle discard pile
            Card top = discardPile.pop();
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
            discardPile.push(top);
        }
        return drawPile.pop();
    }

    public void discard(Card card) {
        discardPile.push(card);
    }

    public Card getTopDiscard() {
        return discardPile.peek();
    }

    public void startWith(Card card) {
        discardPile.push(card);
    }

    public boolean isEmpty() {
        return drawPile.isEmpty();
    }

    public void reshuffle() {
        if (!discardPile.isEmpty()) {
            Card top = discardPile.pop();
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
            discardPile.push(top);
        }
    }
}