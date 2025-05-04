package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;

public class TodoRepositoryImpl implements TodoRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  public TodoRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
    this.queryFactory = jpaQueryFactory;
  }

  // Lv8 : QueryDSL 로 변경, N+1 문제 해결
  @Override
  public Optional<Todo> findByIdWithUser(Long todoId) {
    QTodo todo = QTodo.todo;
    QUser user = QUser.user;

    return Optional.ofNullable(
        queryFactory
        .selectFrom(todo)
        .leftJoin(todo.user, user).fetchJoin()
        .where(todo.id.eq(todoId))
        .fetchOne());
  }
}
