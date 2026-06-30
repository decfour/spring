package com.kdj.commerce.domain.cart;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Cart 1:1 Member
@Entity
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static Cart createCart(Member member) {
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }
}
