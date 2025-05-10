package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.example.expert.domain.todo.dto.response.QTodoSearchDto;
import org.example.expert.domain.todo.dto.response.TodoSearchDto;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

public class TodoRepositoryImpl implements TodoRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  public TodoRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
    this.queryFactory = jpaQueryFactory;
  }

  // Lv8 : QueryDSL 로 변경, N+1 문제 해결
  @Override
  public Optional<Todo> findByIdWithUser(Long todoId) {
    return Optional.ofNullable(
        queryFactory
        .selectFrom(todo)
        .leftJoin(todo.user, user).fetchJoin()
        .where(todo.id.eq(todoId))
        .fetchOne());
  }

  // Lv10 : QueryDSL 적용
  @Override
  public Page<TodoSearchDto> findAllByCondition(
      Pageable pageable, String keyword, String managerNickname,
      LocalDate createdStart, LocalDate createdEnd) {
    List<TodoSearchDto> todos = queryFactory
        .select(new QTodoSearchDto(
            todo.title,
            todo.managers.size(),
            todo.comments.size()))
        .from(todo)
        .leftJoin(todo.managers, manager)
        .leftJoin(manager.user, user)
        .where(
            keywordContains(keyword),
            createdAtBetween(createdStart, createdEnd),
            nicknameContains(managerNickname)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(todo.count())
        .from(todo)
        .where(
            keywordContains(keyword),
            createdAtBetween(createdStart, createdEnd),
            nicknameContains(managerNickname)
        );

    return PageableExecutionUtils.getPage(todos, pageable, countQuery::fetchOne);
  }

  private BooleanExpression keywordContains(String keyword) {
    return keyword != null? todo.title.containsIgnoreCase(keyword):null;
  }

  private BooleanExpression createdAtBetween(LocalDate createdStart, LocalDate createdEnd) {
    if(createdStart != null &&  createdEnd != null) {
      return todo.createdAt.between(createdStart.atStartOfDay(), createdEnd.atTime(LocalTime.MAX));
    }
    if(createdStart != null) {
      return todo.createdAt.goe(createdStart.atStartOfDay());
    }
    if(createdEnd != null) {
      return todo.createdAt.loe(createdEnd.atTime(LocalTime.MAX));
    }
    return null;
  }

  private BooleanExpression nicknameContains(String nickname) {
    return nickname != null? user.nickname.containsIgnoreCase(nickname) : null;
  }
}
