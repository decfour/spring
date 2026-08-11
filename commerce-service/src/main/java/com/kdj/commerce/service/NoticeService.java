package com.kdj.commerce.service;

import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.domain.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public Notice findOne(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID=" + id));
    }

    public Page<Notice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Transactional
    public Long save(String title, String content) {
        Notice savedNotice = noticeRepository.save(Notice.create(title, content));

        return savedNotice.getId();
    }

    @Transactional
    public void update(Long id, String title, String content) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID=" + id));
        notice.update(title, content);
    }

    @Transactional
    public void delete(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID = " + id));
        noticeRepository.delete(notice);
    }
}
