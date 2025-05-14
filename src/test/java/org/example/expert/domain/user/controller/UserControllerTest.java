package org.example.expert.domain.user.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserControllerTest {
  User user;
  CustomUserDetails userDetails;

  @Autowired
  UserController userController;
  @Autowired
  UserRepository userRepository;

  @BeforeEach
  void setup() {
    user = User.builder()
        .email("email@email.com")
        .id(1000555L)
        .userRole(UserRole.USER)
        .nickname("testnick")
        .password("password")
        .build();
    userDetails = new CustomUserDetails(user);
  }

  @Test
  @DisplayName("조회 시간 측정")
  void findByNickname() {
    // given
    Long size = userRepository.count();
    Long randInt = ThreadLocalRandom.current().nextLong(1L, size+1); // origin ~ bound 미만
    User randUser = userRepository.findById(randInt)
        .orElseThrow(() -> new RuntimeException("User Not Found"));

    // when
    ResponseEntity<List<UserResponse>> response = userController.findByNickname(randUser.getNickname());

    // then
    List<UserResponse> users = response.getBody();
    users.forEach(
        (u) -> assertEquals(randUser.getNickname(), u.getNickname()));

    users.forEach(
        (u) -> System.out.println("[TEST RESULT] Keyword: "+randUser.getNickname()
            + "\n Id: "+randInt
            + "\n Find: "+u.getNickname()));
  }
}