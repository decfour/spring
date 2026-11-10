// 지도 설정
const map = new kakao.maps.Map(
    document.getElementById('map'),
    {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 4
    }
);

// 화면 요소
const locationBtn = document.getElementById('locationBtn');
const status = document.getElementById('status');
const submitBtn = document.getElementById('submitBtn');
const resetBtn = document.getElementById('resetBtn');

// 선택한 출발지·도착지
let startMarker = null;
let endMarker = null;
let route = null;

let startLat = null;
let startLng = null;
let endLat = null;
let endLng = null;

// 출발지·도착지 선택
function selectPoint(mouseEvent) {
    const latlng = mouseEvent.latLng;

    if (startLat === null) {
        startLat = latlng.getLat();
        startLng = latlng.getLng();

        startMarker = new kakao.maps.Marker({
            position: latlng,
            map: map
        });

        document.getElementById('startLat').value = startLat;
        document.getElementById('startLng').value = startLng;

        status.textContent = '📍 목적지를 선택하세요';

        return;
    }

    if (endLat === null) {
        endLat = latlng.getLat();
        endLng = latlng.getLng();

        endMarker = new kakao.maps.Marker({
            position: latlng,
            map: map
        });

        document.getElementById('endLat').value = endLat;
        document.getElementById('endLng').value = endLng;

        status.textContent = '⏳ 경로를 찾는 중...';

        findRoute();
    }
}

// 경로 조회·표시
function findRoute() {
    fetch('/walk/course/route', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            startLat: startLat,
            startLng: startLng,
            endLat: endLat,
            endLng: endLng
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('경로 조회 실패');
        }

        return response.json();
    })
    .then(data => {
        document.getElementById('routeData').value = data.routeData;
        document.getElementById('distance').value = data.distance;
        document.getElementById('duration').value = data.duration;

        const points = JSON.parse(data.routeData);

        const path = points.map(point =>
            new kakao.maps.LatLng(point[1], point[0])
        );

        route = new kakao.maps.Polyline({
            path: path,
            strokeWeight: 5,
            strokeColor: '#1d1d1f',
            strokeOpacity: 0.8,
            strokeStyle: 'solid'
        });

        route.setMap(map);

        status.textContent =
            `✅ 경로 조회 완료 (${data.distance}m / ${Math.round(data.duration / 60)}분)`;

        submitBtn.disabled = false;
    })
    .catch(error => {

        console.error(error);

        status.textContent = '❌ 경로 조회 실패';
        submitBtn.disabled = true;
    });
}

// 선택 초기화
function resetCourse() {
    if (startMarker) {
        startMarker.setMap(null);
    }

    if (endMarker) {
        endMarker.setMap(null);
    }

    if (route) {
        route.setMap(null);
    }

    startMarker = null;
    endMarker = null;
    route = null;

    startLat = null;
    startLng = null;
    endLat = null;
    endLng = null;

    document.getElementById('startLat').value = '';
    document.getElementById('startLng').value = '';
    document.getElementById('endLat').value = '';
    document.getElementById('endLng').value = '';
    document.getElementById('routeData').value = '';
    document.getElementById('distance').value = '';
    document.getElementById('duration').value = '';

    submitBtn.disabled = true;
    status.textContent = '📍 출발지를 선택하세요';
}

// 현재 위치 확인
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

            locationBtn.disabled = false;
            locationBtn.innerText = '📍 현재 위치';
        },
        function(error) {
            console.error(error);
            alert('현재 위치를 가져오지 못했습니다.\n위치 권한을 확인해주세요.');

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

// 이벤트 연결
kakao.maps.event.addListener(map, 'click', selectPoint);
locationBtn.addEventListener('click', locate);
resetBtn.addEventListener('click', resetCourse);
