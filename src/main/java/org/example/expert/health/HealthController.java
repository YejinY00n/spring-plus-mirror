package org.example.expert.health;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Lv12: Health Check API
@Controller
public class HealthController {
  @GetMapping("/health")
  ResponseEntity<Void> checkHealth() {
    return ResponseEntity.ok().build();
  }
}
