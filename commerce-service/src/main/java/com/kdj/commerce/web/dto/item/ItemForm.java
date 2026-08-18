package com.kdj.commerce.web.dto.item;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.ItemType;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemForm {
        private Long id;

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 30, message = "이름은 최대 30자 입니다.")
        private String name;

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 100, message = "가격은 최소 100원입니다.")
        @Max(value = 10000000, message = "가격은 최대 1천만원입니다.")
        private Integer price;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 0, message = "수량은 최소 0개입니다.")
        @Max(value = 9999, message = "수량은 최대 9,999개입니다.")
        private Integer stock;

        private String description;
        private boolean open = true;
        private boolean deleted = false;

        @NotNull(message = "상품 종류를 선택해주세요.")
        private ItemType itemType;

        @NotNull(message = "배송 방식을 선택해주세요.")
        private DeliveryType deliveryType;

        private MultipartFile imageFile;
}
