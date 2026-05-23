package com.kdj.commerce.session;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.web.session.SessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class SessionManagerTest {

    SessionManager sessionManager = new SessionManager();

    @Test
    void justTest() {
        Member member = new Member();
        MockHttpServletResponse response = new MockHttpServletResponse();
        sessionManager.createSession(member, response);
    }

}