package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {
  private final LogRepository logRepository;

  // Lv11 : 로그 기록
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveManagerRegisterRequest(Long userId, Long managerId) {
    Log log = new Log(userId,
        "Manager register - Request ID: "+userId+", Manager ID: "+managerId);
    logRepository.save(log);
  }
}
