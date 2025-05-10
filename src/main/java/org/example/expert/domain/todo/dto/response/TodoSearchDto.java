package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TodoSearchDto {
  private String title;
  private int managersTotal;
  private int commentsTotal;

  @QueryProjection
  public TodoSearchDto(String title, int managersTotal, int commentsTotal) {
    this.title = title;
    this.managersTotal = managersTotal;
    this.commentsTotal = commentsTotal;
  }
}
