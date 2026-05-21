package com.kdj.commerce.domain.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity // Database Table과 매칭되는 엔티티
@Data
public class Item {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")   // 실제 테이블 칼럼명
    private Long id;

    @NotBlank(message = "상품 이름은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 100, message = "가격은 최소 100원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "수량은 필수입니다.")
    @Max(value = 9999, message = "수량은 최대 9,999개까지만 등록 가능합니다.")
    private Integer quantity;

    private String description;

    private boolean open;

    @NotNull(message = "상품 종류를 선택해주세요.")
    @Enumerated(EnumType.STRING)    // enum을 문자 그대로 저장
    private ItemType itemType;

    private String delivery;

    public Item() {
    }
}
