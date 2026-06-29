package com.kdj.commerce.domain.item;

import com.kdj.commerce.exception.NotEnoughStockException;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 100, message = "가격은 최소 100원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "수량은 필수입니다.")
    @Max(value = 9999, message = "수량은 최대 9,999개까지만 등록 가능합니다.")
    private Integer stock;

    private String description;
    private boolean open;
    private boolean deleted = false;

    @NotNull(message = "상품 종류를 선택하세요")
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @NotNull(message = "배송 방식을 선택하세요")
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @NotNull
    private Long createdBy;

    private String uploadFileName;          // 유저가 업로드한 파일명
    private String storeFileName;           // 서버가 관리하는 파일명

    // 재고 증가 (주문 취소)
    public void addStock(int quantity) {
        this.stock += quantity;
    }

    // 재고 감소 (주문 완료)
    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException("재고가 부족 (현재 : " + this.stock + "개)");
        }
        this.stock = restStock;
    }

    public void delete() {
        this.deleted = true;
        this.open = false;
    }

    public void restore() {
        this.deleted = false;
        this.open = true;
    }

    public Item() {
    }

    public static Item createItem(String name, Integer price, Integer stock,
                                  String description, boolean open,
                                  ItemType itemType, DeliveryType deliveryType, Long createdBy,
                                  String uploadFileName, String storeFileName) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setStock(stock);
        item.setDescription(description);
        item.setOpen(open);
        item.setItemType(itemType);
        item.setDeliveryType(deliveryType);
        item.setCreatedBy(createdBy);
        item.setUploadFileName(uploadFileName);
        item.setStoreFileName(storeFileName);

        return item;
    }
}
