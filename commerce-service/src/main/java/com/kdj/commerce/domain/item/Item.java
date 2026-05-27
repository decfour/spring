package com.kdj.commerce.domain.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Item {

    // 상품 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    // 상품 이름
    @NotBlank(message = "상품 이름은 필수입니다.")
    private String name;

    // 가격
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 100, message = "가격은 최소 100원 이상이어야 합니다.")
    private Integer price;

    // 수량
    @NotNull(message = "수량은 필수입니다.")
    @Max(value = 9999, message = "수량은 최대 9,999개까지만 등록 가능합니다.")
    private Integer stock;

    // 설명
    private String description;

    // 판매 유무
    private boolean open;

    // 상품 종류
    @NotNull(message = "상품 종류를 선택해주세요.")
    @Enumerated(EnumType.STRING)    // enum을 문자 그대로 저장
    private ItemType itemType;

    // 배송 방식
    private String delivery;


    // 재고 증가 (주문 취소)
    public void addStock(int quantity) {

        this.stock += quantity;
    }

    // 재고 감소 (주문 완료)
    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new IllegalStateException("주문 가능한 재고가 부족합니다.");
        }
        this.stock = restStock;
    }

    public Item() {
    }
}
