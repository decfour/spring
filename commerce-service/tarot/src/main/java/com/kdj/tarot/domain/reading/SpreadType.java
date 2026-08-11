package com.kdj.tarot.domain.reading;

public enum SpreadType {
    ONE_CARD(1),
    THREE_CARD(3),
    CELTIC_CROSS(10);

    private final int cardCount;

    SpreadType(int cardCount) {
        this.cardCount = cardCount;
    }

    public int getCardCount() {
        return cardCount;
    }
}
