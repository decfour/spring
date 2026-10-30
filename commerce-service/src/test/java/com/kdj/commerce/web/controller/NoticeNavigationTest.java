package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.NoticeService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NoticeNavigationTest {
    @Test
    void legacyListKeepsNoticePageWhenReturningToTalk() {
        var service=mock(NoticeService.class);
        assertThat(new NoticeController(service).list(null,PageRequest.of(2,10),new ExtendedModelMap())).isEqualTo("redirect:/community?noticePage=2");
        verifyNoInteractions(service);
    }
    @Test
    void deletionReturnsAdminToTalk() {
        var service=mock(NoticeService.class);var member=mock(Member.class);when(member.getMemberType()).thenReturn(MemberType.ADMIN);
        assertThat(new NoticeController(service).delete(member,10L)).isEqualTo("redirect:/community");
        verify(service).delete(10L);
    }
    @Test
    void unauthorizedDeletionDoesNotTouchNoticeAndReturnsToTalk() {
        var service=mock(NoticeService.class);
        assertThat(new NoticeController(service).delete(null,10L)).isEqualTo("redirect:/community");
        verifyNoInteractions(service);
    }
}
