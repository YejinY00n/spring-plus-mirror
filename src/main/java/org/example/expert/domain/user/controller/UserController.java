package org.example.expert.domain.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.common.annotation.ExecutionLoggable;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 닉네임 일치하는 유저 목록 조회
    @ExecutionLoggable
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> findByNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.findByNickname(nickname));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(userDetails.getId(), userChangePasswordRequest);
    }

    @PostMapping("/users/profile")
    public ResponseEntity<String> uploadProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart MultipartFile image) {
        return ResponseEntity.ok(userService.uploadProfile(userDetails, image));
    }

    @GetMapping("/users/profile")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails));
    }

    @PatchMapping("/users/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart MultipartFile image) {
        return ResponseEntity.ok(userService.updateProfile(userDetails, image));
    }

    @DeleteMapping("/users/profile")
    public void deleteProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteProfile(userDetails);
    }
}
