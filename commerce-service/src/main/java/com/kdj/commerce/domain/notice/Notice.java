package com.kdj.commerce.domain.notice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob // 대용량 문자열
    private String content;

    private LocalDateTime createdDate = LocalDateTime.now();
}
