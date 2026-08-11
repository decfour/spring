package com.kdj.tarot.domain.reading;

import com.kdj.tarot.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String question;

    @Enumerated(EnumType.STRING)
    private SpreadType spreadType;

    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingCard> cards = new ArrayList<>();

    private String result;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected Reading() {
    }

    public void addCard(ReadingCard readingCard) {
        cards.add(readingCard);
    }
}
