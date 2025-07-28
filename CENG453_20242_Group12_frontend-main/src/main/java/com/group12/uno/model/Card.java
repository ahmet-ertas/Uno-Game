package com.group12.uno.model;

public class Card {
    public enum Color { RED, YELLOW, GREEN, BLUE, WILD }
    public enum Type { NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR }

    private final Color color;
    private final Type type;
    private final int number; // Only used for NUMBER cards

    public Card(Color color, Type type, int number) {
        this.color = color;
        this.type = type;
        this.number = number;
    }

    public Card(Color color, Type type) {
        this(color, type, -1);
    }

    public Color getColor() { return color; }
    public Type getType() { return type; }
    public int getNumber() { return number; }

    @Override
    public String toString() {
        if (type == Type.NUMBER) {
            return color + " " + number;
        } else if (type == Type.WILD || type == Type.WILD_DRAW_FOUR) {
            return type.name();
        } else {
            return color + " " + type.name();
        }
    }
}