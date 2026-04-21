# Spring MVC 실습

## 실습 목표
서블릿에서 스프링 MVC로 넘어가는 설계를 직접 코딩하며, `DispatcherServlet`의 내부 동작 원리와 **어댑터 패턴(Adapter Pattern)**의 필요성을 이해.

## Tech Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Library**: Servlet, Lombok, Thymeleaf
- **Build**: Gradle

## 주요 구현 및 발전 과정 (V1 ~ V5)

서블릿 호출에서 시작하여, 유연한 프레임워크 구조로 리팩토링하는 과정을 담고 있다.

- **V1: 프론트 컨트롤러 도입**
    - 모든 요청을 하나의 서블릿(Front Controller)으로 집중시켜 공통 로직 처리의 기반을 마련.
- **V2: View 분리**
    - 반복되는 뷰 렌더링 로직을 `MyView` 객체로 공통화하여 컨트롤러의 책임을 줄였다.
- **V3: Model 추가 및 서블릿 종속성 제거**
    - 컨트롤러가 서블릿 기술(HttpServletRequest/Response)을 몰라도 동작할 수 있도록 `ModelView`를 도입, 순수 자바 코드로 테스트가 가능하게 개선.
- **V4: 실용적인 컨트롤러 인터페이스**
    - 개발자가 `ModelView`를 직접 생성하지 않고 `Map`에 값을 담아 반환하는 직관적인 구조로 개선.
- **V5: 유연한 어댑터 패턴 적용 (최종)**
    - **핵심 단계**: 서로 다른 인터페이스를 가진 컨트롤러(V3, V4)를 하나의 프론트 컨트롤러에서 처리할 수 있도록 `HandlerAdapter`를 도입.

## 핵심 학습 포인트
- **Front Controller**: 중앙 집중식 요청 처리를 통한 유지보수성 향상
- **Adapter Pattern**: 다형성을 활용하여 기존 코드를 수정하지 않고 새로운 컨트롤러 형식을 확장