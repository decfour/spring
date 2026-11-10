// 지도 설정
const map = new kakao.maps.Map(
    document.getElementById('map'),
    {
        center: new kakao.maps.LatLng(course.startLat, course.startLng),
        level: 3
    }
);

// 코스 경로 표시
function showCourseOnMap() {
    let routeData;
    try {
        routeData = typeof course.routeData === 'string'
            ? JSON.parse(course.routeData)
            : course.routeData;
    } catch (error) {
        console.error(error);
        return;
    }

    if (!routeData || routeData.length === 0) return;

    const path = routeData.map(point => new kakao.maps.LatLng(point[1], point[0]));

    const polyline = new kakao.maps.Polyline({
        path: path,
        strokeWeight: 5,
        strokeColor: '#1d1d1f',
        strokeOpacity: 0.8,
        strokeStyle: 'solid'
    });

    polyline.setMap(map);

    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(position => bounds.extend(position));
    map.setBounds(bounds, 40, 40, 40, 40);
}

// 별점 저장
async function saveRating(score) {
    const buttons = document.querySelectorAll('#ratingModal .btn-action');
    const errorMessage = document.getElementById('ratingError');
    buttons.forEach(button => button.disabled = true);
    errorMessage.textContent = '';
    try {
        const response = await fetch(`/walk/course/${course.id}/rating`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({score})
        });
        if (response.status === 401 || response.redirected) {
            location.href = '/member/sign-in?redirectURL=' + encodeURIComponent(location.pathname);
            return;
        }
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '별점 저장에 실패했습니다.');
        }
        location.reload();
    } catch (error) {
        errorMessage.textContent = error.message;
    } finally {
        buttons.forEach(button => button.disabled = false);
    }
}

// 태그 추가
function addTag(tagCode) {
    fetch(`/walk/course/${course.id}/tag`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ tag: tagCode })
    })
    .then(response => {
        if (response.status === 401 ||
            (response.redirected && new URL(response.url).pathname === '/member/sign-in')) {
            const returnUrl = location.pathname + location.search + location.hash;
            location.href = '/member/sign-in?redirectURL=' + encodeURIComponent(returnUrl);
            return;
        }
        if (!response.ok) {
            const contentType = response.headers.get('Content-Type') || '';
            if (contentType.includes('application/json')) {
                return response.json().then(error => {
                    throw new Error(error.message);
                });
            }
            return response.text().then(msg => {
                throw new Error(msg);
            });
        }
        location.reload();
    })
    .catch(error => {
        alert(error.message || '태그 추가 실패');
    });
}

// 거리·시간 표시
function formatDistance(distance) {
    if (distance == null) return '';
    return distance >= 1000
        ? (distance / 1000).toFixed(1) + ' km'
        : distance + ' m';
}

function formatDuration(duration) {
    if (duration == null) return '';
    return Math.round(duration / 60) + '분';
}

// 이벤트 연결·초기 표시
document.getElementById('backBtn').addEventListener('click', function () {
    history.back();
});

document.getElementById('distance').innerText = formatDistance(course.distance);
document.getElementById('duration').innerText = formatDuration(course.duration);

showCourseOnMap();

document.querySelectorAll('[data-score]').forEach(button => {
    button.addEventListener('click', () => saveRating(Number(button.dataset.score)));
});
