package org.example.expert.domain.image.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.image.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
  private final ImageRepository imageRepository;
  @Value("${file.upload-dir}")
  private String TEMP_RESOURCE_DIR;
  private final List<String> ALLOWED_FILE_TYPES = List.of(
      ".jpeg", ".jpg", ".png"
  );

  private final AmazonS3 s3Client;
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  // 단일 파일 저장 후 이미지 반환
  public String uploadFile(MultipartFile file) throws IOException {
    // 파일 타입 검사
    if (!isValidFileType(file.getOriginalFilename())) {
      throw new InvalidRequestException("지원하지 않는 파일 형식입니다.");
    }

    // 파일 이름 중복 방지
    String uuidFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

    // Multipart -> File 로 변환 (로컬에 임시 저장)
    Path filePath = Paths.get(TEMP_RESOURCE_DIR + uuidFilename);
    File uploadFile = convert(file, filePath.toString())
        .orElseThrow(() -> new UncheckedIOException(new IOException("로컬 파일 저장에 실패하였습니다.")));

    // S3에 파일 업로드
    String url;
    try {
      url = putS3(uploadFile, uuidFilename);
    } catch (Exception e) {
      throw new RuntimeException("S3 파일 업로드에 실패하였습니다: ", e);
    } finally {
      deleteLocalTempFile(uploadFile);        // 실패 시 로컬 임시 파일 삭제
    }
    return url;
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

  // 파일을 로컬에 임시 업로드
  private Optional<File> convert(MultipartFile file, String filePath) {
    // 파일 임시 저장 경로 설정
    File convertFile = new File(filePath);

    // convertFile 에 작성
    try (FileOutputStream fos = new FileOutputStream(convertFile)) {
      fos.write(file.getBytes());
      return Optional.of(convertFile);
    }
    catch (IOException e) {
      log.error("FAILED TO WRITE FILE PATH: {}", filePath);
      return Optional.empty();
    }
  }

  //  S3 에 파일 업로드
  private String putS3(File uploadFile, String fileName) {
    s3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(
        CannedAccessControlList.PublicRead));   // 공개 읽기 권한을 부여하여 파일 업로드

    return s3Client.getUrl(bucket, fileName).toString();
  }

  // S3 에 업로드 된 파일 삭제
  public boolean deleteFile(String filename) {
    s3Client.deleteObject(new DeleteObjectRequest(bucket, filename));
    return !s3Client.doesObjectExist(bucket, filename);
  }

  // 로컬 임시 파일 삭제: 비동기 실행
  // 실패 시 최대 3회 실행
  @Async
  @Retryable(value = IOException.class, maxAttempts = 3)
  public void deleteLocalTempFile(File tempFile) throws IOException{
    if(tempFile.delete()) {
      log.info("로컬 임시 파일 삭제에 성공하였습니다 : "+tempFile.getPath());
    } else {
      log.error("로컬 임시 파일 삭제에 실패하였습니다 : "+tempFile.getPath());
      throw new IOException("로컬 임시 파일 삭제에 실패하였습니다 : \"+tempFile.getPath()");
    }
  }
}
