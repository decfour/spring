package com.kdj.tarot.domain.card;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String name;

    private CardType type;

    private String imageUrl;

    @Column(length = 2000)
    private String uprightMeaning;

    @Column(length = 2000)
    private String reversedMeaning;

    protected Card() {
    }
}
