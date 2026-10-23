package com.decfour.dontsell.domain.chat;

import com.decfour.dontsell.domain.stock.Stock;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ChatRoomTests {
    @Autowired
    private EntityManager entityManager;

    @Test
    void roomWithoutStockCannotBeSaved() {
        ChatRoom room = new ChatRoom("종목 없는 방", null);

        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            entityManager.persist(room);
            entityManager.flush();
        });
    }

    @Test
    void multipleRoomsCanReferToSameStock() {
        Stock stock = new Stock("ROOM_TEST", "Chat Room Test", "NASDAQ");
        entityManager.persist(stock);

        ChatRoom first = new ChatRoom("장기 투자", stock);
        ChatRoom second = new ChatRoom("단기 매매", stock);
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        ChatRoom savedFirst = entityManager.find(ChatRoom.class, first.getId());
        ChatRoom savedSecond = entityManager.find(ChatRoom.class, second.getId());
        assertNotNull(savedFirst);
        assertNotNull(savedSecond);
        assertNotEquals(savedFirst.getId(), savedSecond.getId());
        assertEquals(stock.getId(), savedFirst.getStock().getId());
        assertEquals(stock.getId(), savedSecond.getStock().getId());
    }
}
