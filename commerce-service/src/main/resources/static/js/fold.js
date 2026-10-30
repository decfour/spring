document.querySelectorAll('[data-fold]').forEach(box => {
    const text = box.querySelector('.fold-text');
    const button = box.querySelector('[data-fold-toggle]');
    if (!text || !button) return;

    function refresh() {
        const expanded = box.dataset.expanded === 'true';
        button.hidden = !expanded && text.scrollHeight <= text.clientHeight + 1;
    }

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

    new ResizeObserver(refresh).observe(text);
    refresh();
});
