package com.decfour.dontsell.domain.holding;

import com.decfour.dontsell.domain.account.Account;
import com.decfour.dontsell.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "holding", uniqueConstraints = {
        @UniqueConstraint(name = "uk_holding_account_stock", columnNames = {"account_id", "stock_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "avg_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal avgPrice;

    public Holding(Account account, Stock stock, Long quantity, BigDecimal avgPrice) {
        this.account = account;
        this.stock = stock;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }
}
