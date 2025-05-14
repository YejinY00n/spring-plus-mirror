package org.example.expert.domain.user.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.image.entity.Image;
import org.example.expert.domain.image.repository.ImageRepository;
import org.example.expert.domain.image.service.S3Service;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    public List<UserResponse> findByNickname(String nickname) {
        List<User> users = userRepository.findAllByNickname(nickname)
            .orElseThrow(() -> new InvalidRequestException(nickname+ " : user does not exists"));;
        return users.stream()
            .map((u) -> new UserResponse(u.getId(), u.getEmail(), u.getNickname()))
            .toList();
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }
    @Transactional
    public String uploadProfile(CustomUserDetails userDetails, MultipartFile image) {
        String s3url;

        try {
            s3url = s3Service.uploadFile(image);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        // image 엔티티에 경로 정보 저장
        String fileName = s3url.substring(s3url.lastIndexOf(".com/") + 5);
        Image img = new Image(s3url, fileName);
        imageRepository.save(img);

        // user 에 연관 관계 설정
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다."));
        user.setProfile(img);

        return s3url;
    }

    public String getProfile(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다."));

        return Optional.of(user.getProfile().getPath()).orElseThrow(
            () -> new InvalidRequestException("프로필 이미지가 없습니다."));
    }

    @Transactional
    public String updateProfile(CustomUserDetails userDetails, MultipartFile image) {
        deleteProfile(userDetails);
        return uploadProfile(userDetails, image);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteProfile(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다."));

        // S3 에서 삭제
        String fileName = Optional.of(user.getProfile().getPath()).orElseThrow(
            () -> new InvalidRequestException("프로필 이미지가 없습니다."));

        if(!s3Service.deleteFile(fileName)) {
            throw new RuntimeException("업로드 파일 삭제애 실패하였습니다.");
        }

        // user DB 연관관계 에서 삭제
        user.removeProfile();
    }
}
