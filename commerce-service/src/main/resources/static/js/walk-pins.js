(() => {
    const endpoint = `/walk/course/${course.id}/pins`;
    const addButton = document.getElementById('addPinBtn');
    const pinLabel = document.getElementById('pinLabel');
    const pinCount = document.getElementById('pinCount');
    const pinTotal = document.getElementById('pinTotal');
    const status = document.getElementById('pinStatus');
    const info = new kakao.maps.InfoWindow({removable: true, zIndex: 10});
    const markers = new Map();
    let picking = false;
    let draftMarker = null;
    let loading = false;

    function login() {
        location.href = '/member/sign-in?redirectURL=' + encodeURIComponent(location.pathname + location.search);
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {...options, headers: {Accept: 'application/json', ...options.headers}});
        if (response.status === 401 || (response.redirected && new URL(response.url).pathname === '/member/sign-in')) {
            login();
            throw new Error('로그인이 필요합니다.');
        }
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || '요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.');
        }
        return response.status === 204 ? null : response.json();
    }

    function setPicking(value) {
        picking = value;
        addButton.setAttribute('aria-pressed', String(value));
        pinLabel.textContent = value ? '선택' : '발견';
        pinTotal.hidden = value;
        addButton.setAttribute('aria-label', value ? '발견 장소 선택 취소' : '발견 장소 추가');
        map.setCursor(value ? 'crosshair' : '');
        status.textContent = value ? '경로에서 30m 이내의 장소를 지도에서 눌러주세요.' : '';
    }

    function clearDraft() {
        if (draftMarker) draftMarker.setMap(null);
        draftMarker = null;
    }

    function element(tag, text, className) {
        const node = document.createElement(tag);
        if (text !== undefined) node.textContent = text;
        if (className) node.className = className;
        if (tag === 'button') node.classList.add('btn-action');
        if (className === 'walk-pin-popup') {
            for (const type of ['click', 'mousedown', 'touchstart', 'dblclick']) {
                node.addEventListener(type, event => {
                    event.stopPropagation();
                    kakao.maps.event.preventMap();
                });
            }
        }
        return node;
    }

    function openPin(pin, marker) {
        if (loading) return;
        clearDraft();
        setPicking(false);
        const box = element('div', undefined, 'walk-pin-popup');
        box.append(element('h3', pin.title), element('small', `발견한 사람 · ${pin.creatorName}`), element('p', pin.content));
        if (pin.deletable) {
            const remove = element('button', '삭제');
            remove.type = 'button';
            const error = element('p', '', 'walk-pin-error');
            error.setAttribute('role', 'alert');
            remove.addEventListener('click', async () => {
                if (!confirm('이 발견 핀을 삭제할까요?')) return;
                remove.disabled = true;
                try {
                    await request(`${endpoint}/${pin.id}`, {method: 'DELETE'});
                    marker.setMap(null);
                    markers.delete(pin.id);
                    pinCount.textContent = markers.size;
                    info.close();
                    status.textContent = '발견 핀을 삭제했습니다.';
                } catch (e) { error.textContent = e.message; }
                finally { remove.disabled = false; }
            });
            box.append(remove, error);
        }
        info.setContent(box);
        info.open(map, marker);
    }

    function addMarker(pin) {
        if (markers.has(pin.id)) return markers.get(pin.id);
        const marker = new kakao.maps.Marker({map, position: new kakao.maps.LatLng(pin.latitude, pin.longitude), title: pin.title});
        markers.set(pin.id, marker);
        pinCount.textContent = markers.size;
        kakao.maps.event.addListener(marker, 'click', () => openPin(pin, marker));
        return marker;
    }

    function openForm(position) {
        clearDraft();
        info.close();
        draftMarker = new kakao.maps.Marker({map, position, opacity: 0.6});
        const box = element('form', undefined, 'walk-pin-popup');
        box.append(element('h3', '발견 장소 남기기'), element('small', '코스 경로에서 30m 이내에 남길 수 있어요.'));
        const title = element('input');
        title.id = 'walkPinTitle';
        title.name = 'title';
        title.required = true;
        title.maxLength = 80;
        title.placeholder = '예: 노을이 예쁜 벤치';
        const content = element('textarea');
        content.id = 'walkPinContent';
        content.name = 'content';
        content.required = true;
        content.maxLength = 1000;
        content.placeholder = '이 장소에서 무엇을 발견하셨나요?';
        const titleLabel = element('label', '장소 이름');
        titleLabel.htmlFor = title.id;
        const contentLabel = element('label', '장소 설명');
        contentLabel.htmlFor = content.id;
        const error = element('p', '', 'walk-pin-error');
        error.setAttribute('role', 'alert');
        const actions = element('div', undefined, 'walk-pin-actions');
        const save = element('button', '등록');
        save.type = 'submit';
        save.classList.add('btn-primary');
        const cancel = element('button', '취소');
        cancel.type = 'button';
        cancel.addEventListener('click', () => { if (!loading) { info.close(); clearDraft(); setPicking(false); } });
        actions.append(save, cancel);
        box.append(titleLabel, title, contentLabel, content, error, actions);
        box.addEventListener('submit', async event => {
            event.preventDefault();
            if (loading) return;
            if (!title.value.trim() || !content.value.trim()) { error.textContent = '장소 이름과 설명을 입력해주세요.'; return; }
            loading = true;
            save.disabled = cancel.disabled = addButton.disabled = true;
            error.textContent = '';
            try {
                const pin = await request(endpoint, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({
                    title: title.value.trim(), content: content.value.trim(), latitude: position.getLat(), longitude: position.getLng()
                })});
                clearDraft();
                loading = false;
                openPin(pin, addMarker(pin));
                status.textContent = '발견 장소를 등록했습니다. 다른 산책자도 이 핀을 볼 수 있어요.';
            } catch (e) { error.textContent = e.message; }
            finally { loading = false; save.disabled = cancel.disabled = addButton.disabled = false; }
        });
        info.setContent(box);
        info.open(map, draftMarker);
        title.focus();
    }

    addButton.setAttribute('aria-pressed', 'false');
    addButton.addEventListener('click', event => {
        event.stopPropagation();
        kakao.maps.event.preventMap();
        if (!pinMemberLoggedIn) { login(); return; }
        info.close();
        clearDraft();
        setPicking(!picking);
    });
    kakao.maps.event.addListener(map, 'click', event => {
        if (loading) return;
        if (picking) openForm(event.latLng);
        else info.close();
    });

    request(endpoint).then(pins => {
        pins.forEach(addMarker);
        pinCount.textContent = markers.size;
    }).catch(e => { status.textContent = e.message; });
})();
