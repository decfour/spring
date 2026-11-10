// 지도 설정
const map = new kakao.maps.Map(
    document.getElementById('map'),
    {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
    }
);

let marker = null;
let route = null;

// 선택 코스 표시
function showRoute(card) {
    try {
        const routeData = JSON.parse(card.dataset.route);
        const startLat = parseFloat(card.dataset.lat);
        const startLng = parseFloat(card.dataset.lng);
        const startPosition = new kakao.maps.LatLng(startLat, startLng);

        map.setCenter(startPosition);

        if (route) {
            route.setMap(null);
        }

        if (marker) {
            marker.setMap(null);
        }

        marker = new kakao.maps.Marker({
            position: startPosition,
            map: map
        });

        const path = routeData.map(point => new kakao.maps.LatLng(point[1], point[0]));

        route = new kakao.maps.Polyline({
            path: path,
            strokeWeight: 5,
            strokeColor: '#1d1d1f',
            strokeOpacity: 0.85,
            strokeStyle: 'solid'
        });

        route.setMap(map);
    } catch (e) {
        console.error('코스 데이터 파싱 실패:', e);
    }
}

// 코스 선택 이벤트
document.querySelectorAll('.course-card').forEach(card => {
    card.addEventListener('click', function() {
        document.querySelectorAll('.course-card').forEach(c => c.classList.remove('selected'));
        this.classList.add('selected');
        showRoute(this);
    });
});
