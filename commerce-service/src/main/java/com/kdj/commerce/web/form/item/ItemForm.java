package com.kdj.commerce.web.form.item;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.ItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemForm {
        private Long id;

        @NotBlank(message = "상품 이름은 필수입니다.")
        private String name;

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 100, message = "가격은 최소 100원 이상이어야 합니다.")
        private Integer price;

        @NotNull(message = "수량은 필수입니다.")
        @Max(value = 9999, message = "수량은 최대 9,999개까지만 등록 가능합니다.")
        private Integer stock;

        private String description;

        private boolean open;

        private boolean deleted; // 등록 시 false, 수정 시 사용

        @NotNull(message = "상품 종류를 선택해주세요.")
        private ItemType itemType;

        @NotNull(message = "배송 방식을 선택해주세요.")
        private DeliveryType deliveryType;

        private MultipartFile imageFile;
}
