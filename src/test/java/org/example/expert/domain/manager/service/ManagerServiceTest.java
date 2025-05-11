package org.example.expert.domain.manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.log.repository.LogRepository;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ManagerServiceTest {

  @Autowired
  ManagerService managerService;

  @Autowired
  LogRepository logRepository;

  @Test
  @DisplayName("매니저 등록 로깅 트랜잭션 테스트")
  void saveManager() {
    // given : 매니저 등록을 할 때
    User user = User.builder()
        .email("email@email.com")
        .id(1L)
        .userRole(UserRole.USER)
        .nickname("nick")
        .password("password")
        .build();

    ManagerSaveRequest request = new ManagerSaveRequest(2L);
    CustomUserDetails userDetails = new CustomUserDetails(user);
    Long todoId = 1L;

    // when : 매니저 등록을 할 때
    try {
      managerService.saveManager(userDetails, todoId, request);
    }
    catch (InvalidRequestException e) {}

    // then : 매니저 등록 실패해도 로깅에 성공
    assertEquals(logRepository.count(), 1L);
  }
}