package com.kdj.tarot.domain.reading;

import com.kdj.tarot.domain.card.Card;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class ReadingCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reading reading;

    @ManyToOne(fetch = FetchType.LAZY)
    private Card card;

    private Integer position;

    private boolean reversed;

    protected ReadingCard() {
    }
}
