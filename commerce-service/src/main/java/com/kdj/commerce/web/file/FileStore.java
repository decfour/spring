package com.kdj.commerce.web.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public String storeFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        // 1. 회원이 올린 원래 파일명 꺼내기 (예: "nike.png")
        String originalFilename = multipartFile.getOriginalFilename();

        // 2. 서버 저장 고유 파일명 생성 (예: "uuid-1234.png")
        String storeFileName = createStoreFileName(originalFilename);

        // 3. 실제 하드디스크 경로에 파일 객체 생성 후 저장 처리
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        // DB에 저장할 수 있도록 '서버용 고유 파일명'을 반환
        return storeFileName;
    }

    // 확장자를 추출해서 UUID 뒤에 붙여주는 도우미 메서드
    private String createStoreFileName(String originalFilename) {

        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();

        return uuid + "." + ext; // 결과 예: "550e8400-e29b-41d4-a716-446655440000.png"
    }

    // 파일명에서 확장자만 쏙 빼내는 메서드 (예: "photo.jpg" -> "jpg")
    private String extractExt(String originalFilename) {

        int pos = originalFilename.lastIndexOf(".");

        return originalFilename.substring(pos + 1);
    }
}
