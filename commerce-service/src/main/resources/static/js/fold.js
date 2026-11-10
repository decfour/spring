// 접기 영역별 초기 설정
document.querySelectorAll('[data-fold]').forEach(box => {
    const text = box.querySelector('.fold-text');
    const button = box.querySelector('[data-fold-toggle]');
    if (!text || !button) return;

    // 내용이 넘칠 때만 버튼 표시
    function refresh() {
        const expanded = box.dataset.expanded === 'true';
        button.hidden = !expanded && text.scrollHeight <= text.clientHeight + 1;
    }

    // 펼침 상태·접근성 문구 갱신
    button.addEventListener('click', () => {
        const expanded = box.dataset.expanded !== 'true';
        box.dataset.expanded = String(expanded);
        button.setAttribute('aria-expanded', String(expanded));
        const label = expanded ? '접기' : '펼치기';
        button.setAttribute('aria-label', label);
        button.title = label;
        if (!button.querySelector('svg')) button.textContent = label;
        refresh();
    });

    // 크기 변경 감지·초기 표시
    new ResizeObserver(refresh).observe(text);
    refresh();
});
