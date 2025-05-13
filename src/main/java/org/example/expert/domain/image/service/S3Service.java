package org.example.expert.domain.image.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.image.entity.Image;
import org.example.expert.domain.image.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
  private final ImageRepository imageRepository;
  @Value("${file.upload-dir}")
  private String TEMP_RESOURCE_DIR;
  private final List<String> ALLOWED_FILE_TYPES = List.of(
      ".jpeg", ".jpg", ".png"
  );

  // 단일 파일 저장 후 이미지 반환
  @Transactional(rollbackFor = IOException.class)
  public Image uploadImage(MultipartFile image) {
    // 파일 타입 검사
    if (!isValidFileType(image.getOriginalFilename())) {
      throw new InvalidRequestException("지원하지 않는 파일 형식입니다.");
    }

    // 파일 이름 중복 방지
    String uuidFilename = UUID.randomUUID() + "_" + image.getOriginalFilename();

    // 파일 저장 경로 설정
    Path filePath = Paths.get(TEMP_RESOURCE_DIR + uuidFilename);
    try {
      Files.write(filePath, image.getBytes());
    } catch (IOException e) {
      log.info("FAILED TO WRITE FILE PATH: {}", filePath.toString());
      throw new InvalidRequestException("파일 쓰기에 실패하였습니다.");
    }

    // image 엔티티에 경로 등등 정보 저장
    Image img = new Image(filePath.toString());
    imageRepository.save(img);

    // 이미지 반환
    return img;
  }

  // 파일 확장자 검사
  private boolean isValidFileType(String uploadFileType) {
    for (String fileType : ALLOWED_FILE_TYPES) {
      if (uploadFileType.toLowerCase().endsWith(fileType)) {
        System.out.println("FILE TYPE: " + uploadFileType);
        return true;
      }
    }
    return false;
  }
}
