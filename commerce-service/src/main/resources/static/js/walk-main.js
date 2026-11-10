// 지도 위치 (이전)
const savedLat = sessionStorage.getItem('walkLat');
const savedLng = sessionStorage.getItem('walkLng');

// 지도 위치 (초기)
const initialLat = savedLat ? parseFloat(savedLat) : 37.5665;
const initialLng = savedLng ? parseFloat(savedLng) : 126.9780;

// 지도 객체
const map = new kakao.maps.Map(
    document.getElementById('map'),
    {
        center: new kakao.maps.LatLng(initialLat, initialLng),
        level: 3
    }
);

// 현재 선택 (마커, 경로, 코스, 코스ID)
let marker = null;
let route = null;
let selectedCard = null;
let selectedId = null;

// 현재 위치 버튼, 현재 상태
const locationBtn = document.getElementById('locationBtn');
const status = document.getElementById('status');

// 코스 목록 (목록, 로딩, 빈 상태, 페이징)
const courseList = document.getElementById('courseList');
const courseLoading = document.getElementById('courseLoading');
const courseEmpty = document.getElementById('courseEmpty');
const pagination = document.getElementById('pagination');

// 현재 위치
let currentLat = savedLat ? parseFloat(savedLat) : null;
let currentLng = savedLng ? parseFloat(savedLng) : null;

// 현재 위치 설정·근처 코스 탐색
function selectLocation(lat, lng, statusText) {
    currentLat = lat;
    currentLng = lng;

    sessionStorage.setItem('walkLat', lat);
    sessionStorage.setItem('walkLng', lng);

    const position = new kakao.maps.LatLng(lat, lng);

    // 기존 마커 제거
    if (marker) {
        marker.setMap(null);
    }

    // 기존 경로 제거
    if (route) {
        route.setMap(null);
        route = null;
    }

    // 선택 코스 카드, ID 초기화
    selectedCard = null;
    selectedId = null;

    marker = new kakao.maps.Marker({
        position: position,
        map: map
    });

    status.textContent = statusText;

    loadCourses(lat, lng, 0);
}

// 지도 위치 복원
function restoreLocation() {
    if (currentLat === null || currentLng === null) {
        return;
    }

    const position = new kakao.maps.LatLng(
        currentLat,
        currentLng
    );

    map.setCenter(position);

    marker = new kakao.maps.Marker({
        position: position,
        map: map
    });

    status.textContent = '📍 선택한 위치 기준';

    loadCourses(currentLat, currentLng, 0);
}

// 현재 위치 클릭 이벤트
function locate() {
    if (!navigator.geolocation) {
        alert('이 브라우저에서는 위치 정보를 사용할 수 없습니다.');
        return;
    }

    locationBtn.disabled = true;
    locationBtn.innerText = '위치 확인 중..';

    navigator.geolocation.getCurrentPosition(
        function(position) {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            map.setCenter(new kakao.maps.LatLng(lat, lng));

            selectLocation(
                lat,
                lng,
                '📍 내 현재 위치 기준'
            );

            locationBtn.disabled = false;
            locationBtn.innerText = '📍 현재 위치';
        },
        function(error) {
            console.error(error);

            alert(
                '현재 위치를 가져오지 못했습니다.\n위치 권한을 확인해주세요.'
            );

            locationBtn.disabled = false;
            locationBtn.innerText = '📍 현재 위치';
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        }
    );
}

// 지도 클릭 이벤트
function selectPoint(mouseEvent) {
    const position = mouseEvent.latLng;

    selectLocation(
        position.getLat(),
        position.getLng(),
        '📍 선택한 위치 기준'
    );
}

// 근처 코스 탐색
function loadCourses(lat, lng, page) {
    courseEmpty.style.display = 'none';
    courseLoading.style.display = 'block';

    courseList.innerHTML = '';
    pagination.innerHTML = '';

    fetch(
        `/walk/course/nearby?lat=${lat}&lng=${lng}&page=${page}&size=3`
    )
        .then(response => {
            if (!response.ok) {
                throw new Error('산책 코스 조회 실패');
            }

            return response.json();
        })
        .then(data => {
            courseLoading.style.display = 'none';

            renderCourses(data.content);
            renderPagination(data);
        })
        .catch(error => {
            console.error(error);

            courseLoading.style.display = 'none';
            courseEmpty.style.display = 'block';
            courseEmpty.innerText =
                '주변 산책 코스를 불러오지 못했습니다.';
        });
}

// 근처 코스 표시
function renderCourses(courses) {
    courseList.innerHTML = '';

    if (!courses || courses.length === 0) {
        courseEmpty.style.display = 'block';
        courseEmpty.innerText = '주변에 산책 코스가 없습니다';

        selectedCard = null;

        return;
    }

    courseEmpty.style.display = 'none';

    courses.forEach(course => {
        const card = createCard(course);

        courseList.appendChild(card);

        if (course.id === selectedId) {
            card.classList.add('selected');
            selectedCard = card;
        }
    });
}

// 코스 카드 생성
function createCard(course) {
    const card = document.createElement('div');

    card.className = 'course-card';

    card.innerHTML = `
        <div class="course-body">
            <div class="course-name">
                <a href="/walk/course/${course.id}" class="course-link" title="코스 상세보기">
                    <span class="course-title">${escapeHtml(course.title)}</span>
                    <span class="course-arrow" aria-hidden="true">↗</span>
                </a>
            </div>
            <div class="course-meta">
                <span>${formatDistance(course.distance)}</span>
                <span>•</span>
                <span class="course-rating" aria-live="polite">${course.ratingCount > 0 ? '★ ' + Number(course.averageRating).toFixed(1) + ' · 평가 ' + course.ratingCount : '평가 없음'}</span>
            </div>
        </div>
        <div class="course-actions">
            
            <a href="/walk/course/${course.id}/chat" class="btn-circle course-chat"
               title="코스 채팅 홈" aria-label="코스 채팅 홈">
                <svg width="14" height="14" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <use href="/images/icons/chat.svg#chat"/>
                </svg>
            </a>
        </div>
    `;
    // 코스 상세 페이지 이동 이벤트
    const courseName = card.querySelector('.course-link');

    courseName.addEventListener('click', function(event) {
        event.stopPropagation();
    });

    card.querySelector('.course-chat').addEventListener('click', event => event.stopPropagation());

    // 코스 선택 이벤트
    card.addEventListener('click', function() {
        selectCourse(card, course);
    });

    return card;
}

// 코스 선택
function selectCourse(card, course) {
    if (selectedCard) {
        selectedCard.classList.remove('selected');
    }

    card.classList.add('selected');

    selectedCard = card;
    selectedId = course.id;

    showRoute(course);
}

// 선택 코스 표시
function showRoute(course) {
    const routeData = JSON.parse(course.routeData);

    const startPosition = new kakao.maps.LatLng(
        course.startLat,
        course.startLng
    );

    map.setCenter(startPosition);

    // 기존 경로 제거
    if (route) {
        route.setMap(null);
    }

    const path = routeData.map(
        point => new kakao.maps.LatLng(point[1], point[0])
    );

    route = new kakao.maps.Polyline({
        path: path,
        strokeWeight: 5,
        strokeColor: '#1d1d1f',
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
    });

    route.setMap(map);
}

// 페이징
function renderPagination(pageData) {
    pagination.innerHTML = '';

    if (!pageData.content || pageData.totalPages <= 1) {
        return;
    }

    const currentPage = pageData.number;
    const totalPages = pageData.totalPages;
    const maxButtons = 5;

    let startPage = Math.max(
        0,
        currentPage - Math.floor(maxButtons / 2)
    );

    let endPage = startPage + maxButtons - 1;

    if (endPage >= totalPages) {
        endPage = totalPages - 1;

        startPage = Math.max(
            0,
            endPage - maxButtons + 1
        );
    }

    // 이전 버튼
    const prevBtn = createPageButton(
        '‹',
        pageData.first,
        function() {
            loadCourses(
                currentLat,
                currentLng,
                currentPage - 1
            );
        }
    );

    pagination.appendChild(prevBtn);

    // 페이지 번호
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPageButton(
            (i + 1).toString(),
            false,
            function() {
                if (i !== currentPage) {
                    loadCourses(
                        currentLat,
                        currentLng,
                        i
                    );
                }
            }
        );

        if (i === currentPage) {
            pageBtn.classList.add('active');
            pageBtn.setAttribute('aria-current', 'page');
        }

        pagination.appendChild(pageBtn);
    }

    // 다음 버튼
    const nextBtn = createPageButton(
        '›',
        pageData.last,
        function() {
            loadCourses(
                currentLat,
                currentLng,
                currentPage + 1
            );
        }
    );

    pagination.appendChild(nextBtn);
}

// 페이징 버튼 생성
function createPageButton(text, disabled, onClick) {
    const button = document.createElement('button');

    button.className = 'btn-page';
    button.type = 'button';
    button.innerText = text;
    button.disabled = disabled;

    button.addEventListener('click', onClick);

    return button;
}

// 거리 변환
function formatDistance(distance) {
    if (distance == null) {
        return '';
    }

    return distance >= 1000
        ? (distance / 1000).toFixed(1) + ' km'
        : distance + ' m';
}

// HTML 이스케이프
function escapeHtml(value) {
    if (value == null) {
        return '';
    }

    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// 지도 클릭 이벤트
kakao.maps.event.addListener(
    map,
    'click',
    selectPoint
);

// 현재 위치 버튼 이벤트
locationBtn.addEventListener(
    'click',
    locate
);

// 저장된 기준점이 있으면 복원
restoreLocation();
