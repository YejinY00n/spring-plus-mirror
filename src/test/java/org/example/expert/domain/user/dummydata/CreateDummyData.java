package org.example.expert.domain.user.dummydata;

import jakarta.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

// Lv13 : 100만건 생성 및 API 작성
@SpringBootTest
public class CreateDummyData {
  @Autowired
  JdbcTemplate jdbcTemplate;

  @Autowired
  EntityManager em;

  @Test
  @DisplayName("100만건 더미 테스트 데이터 생성")
  void batchCreateDummyData() {
    String INSERT_SQL = "INSERT INTO users (created_at, image_id, modified_at, email, nickname, password, user_role)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?)";
    int BATCH_SIZE = 1000;
    int TOTAL = 1000000;

    System.out.println("START INSERT !!!");

    for(int i=0; i<TOTAL/BATCH_SIZE; i++) {
      System.out.println("NOW BATCH : "+i);
      jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
          ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
          ps.setNull(2, Types.BIGINT);
          ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
          ps.setString(4, UUID.randomUUID()+"@email.com");
          ps.setString(5, "name"+UUID.randomUUID());
          ps.setString(6, "pw"+UUID.randomUUID());
          ps.setString(7, UserRole.USER.name());
        }

        @Override
        public int getBatchSize() {
          return BATCH_SIZE;
        }
      });
    }
  }
}
