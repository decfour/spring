package com.kdj.commerce.service;

import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.domain.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public Long saveNotice(Notice notice) {
        Notice savedNotice = noticeRepository.save(notice);

        return savedNotice.getId();
    }

    public Notice findOne(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID=" + id));
    }

    public List<Notice> findNotices() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional
    public void updateNotice(Long id, String title, String content) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다. ID=" + id));

        notice.setTitle(title);
        notice.setContent(content);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다. ID = " + id));

        noticeRepository.delete(notice);
    }
}
