package com.kdj.commerce.web.form;

import com.kdj.commerce.domain.item.ItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemSaveForm {

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
    private ItemType itemType;

    // 배송 방식
    private String delivery;
}
