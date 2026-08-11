package com.kdj.commerce.domain.item;

import com.kdj.commerce.exception.NotEnoughStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean open;

    @Column(nullable = false)
    private boolean deleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType;

    @Column(nullable = false)
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
            throw new NotEnoughStockException(
                    "재고 부족 (현재 : " + this.stock + "개)"
            );
        }

        this.stock = restStock;
    }

    public static Item createItem(
            String name,
            Integer price,
            Integer stock,
            String description,
            boolean open,
            boolean deleted,
            ItemType itemType,
            DeliveryType deliveryType,
            Long createdBy,
            String uploadFileName,
            String storeFileName) {
        Item item = new Item();
        item.name = name;
        item.price = price;
        item.stock = stock;
        item.description = description;
        item.open = open;
        item.deleted = deleted;
        item.itemType = itemType;
        item.deliveryType = deliveryType;
        item.createdBy = createdBy;
        item.uploadFileName = uploadFileName;
        item.storeFileName = storeFileName;

        return item;
    }

    public void update(
            String name,
            Integer price,
            Integer stock,
            String description,
            boolean open,
            ItemType itemType,
            DeliveryType deliveryType,
            String uploadFileName,
            String storeFileName) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.open = open;
        this.itemType = itemType;
        this.deliveryType = deliveryType;

        if(uploadFileName != null) {
            this.uploadFileName = uploadFileName;
            this.storeFileName = storeFileName;
        }
    }

    public void delete() {
        this.deleted = true;
        this.open = false;
    }

    public void restore() {
        this.deleted = false;
        this.open = true;
    }
}
