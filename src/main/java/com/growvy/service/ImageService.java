package com.growvy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {


    // EC2에 실제 저장될 위치
    private static final String UPLOAD_DIR = "/data/uploads/images";
    public String saveJobPostImage(
            Long postId,
            MultipartFile file,
            int order
    ) {

        System.out.println("===== ImageService 호출 =====");
        System.out.println("UPLOAD_DIR = " + UPLOAD_DIR);

        try {

            Path postDir = Paths.get(
                    UPLOAD_DIR,
                    String.valueOf(postId)
            );

            Files.createDirectories(postDir);

            String originalFilename = file.getOriginalFilename();

            String extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    );

            String fileName = order + extension;

            Path targetPath = postDir.resolve(fileName);

            System.out.println("저장 경로 = " + targetPath);

            file.transferTo(targetPath.toFile());

            return "/images/" + postId + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}