package hello.servlet.basic.request;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 1. 파라미터 전송 : GET(쿼리 파라미터), POST(HTML Form) 지원
// 2. 복수 파라미터 : 하나의 키에 여러 값이 올 경우 처리
// 3. 주의: JSON 데이터를 읽을 수 없음
@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. 전체 파라미터 조회
        System.out.println("[전체 파라미터 조회] - START");
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName ->
                        System.out.println(paramName + "=" + request.getParameter(paramName)));
        System.out.println("[전체 파라미터 조회] - END\n");

        // 2. 단일 파라미터 조회
        System.out.println("[단일 파라미터 조회]");
        String username = request.getParameter("username"); // 값이 없으면 null 반환
        String age = request.getParameter("age");
        System.out.println("username = " + username);
        System.out.println("age = " + age);
        System.out.println();

        // 3. 동일한 이름의 복수 파라미터 조회 ---
        // getParameter()는 중복 시 '첫 번째 값'만 반환, 전체 조회는 getParameterValues() 사용
        System.out.println("[복수 파라미터 조회]");
        String[] usernames = request.getParameterValues("username");
        if (usernames != null) {
            for (String name : usernames) {
                System.out.println("username (multi) = " + name);
            }
        }

        response.getWriter().write("ok");
    }
}