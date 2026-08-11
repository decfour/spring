package com.kdj.commerce.domain.notice;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private LocalDateTime createdDate = LocalDateTime.now();

    public static Notice create(String title, String content) {
        Notice notice = new Notice();
        notice.title = title;
        notice.content = content;

        return notice;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
