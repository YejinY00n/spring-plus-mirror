package org.example.expert.domain.todo.service;

import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.repository.TodoRepositoryImpl;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    // Lv3 : todo 검색 조건 기능 추가
    public Page<TodoResponse> getTodos(
        int page, int size,
        @Nullable String weather,
        @Nullable LocalDate modifiedStart, @Nullable LocalDate modifiedEnd) {
        // 날짜 유효 검증
        if(modifiedStart != null || modifiedEnd != null) {
            if(modifiedStart != null && modifiedStart.isAfter(LocalDate.now())) {
                throw new InvalidRequestException("현재 이전의 날짜부터 검색 가능합니다.");
            }
            if(modifiedEnd != null && modifiedEnd.isAfter(LocalDate.now())) {
                throw new InvalidRequestException("현재 이전의 날짜까지 검색 가능합니다.");
            }
            if(modifiedStart != null && modifiedEnd != null && modifiedStart.isAfter(modifiedEnd)) {
                throw new InvalidRequestException("검색 시작일은 검색 끝일보다 이전이어야 합니다.");
            }
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todos;

        if(weather != null && (modifiedStart != null || modifiedEnd != null)) {
            if(modifiedStart == null) {
                todos = todoRepository.findAllByWeatherUntilModifiedAtAndOrder(pageable, weather, modifiedEnd);
            }
            else if(modifiedEnd == null) {
                todos = todoRepository.findAllByWeatherBeginModifiedAtAndOrder(pageable, weather, modifiedStart);
            }
            else {
                todos = todoRepository.findAllByWeatherBetweenModifiedAtAndOrder(pageable, weather, modifiedStart, modifiedEnd);
            }
        }
        else if(weather != null) {
            todos = todoRepository.findAllByWeatherAndOrder(pageable, weather);
        }
        else if(modifiedStart != null || modifiedEnd != null) {
            if(modifiedStart == null) {
                todos = todoRepository.findAllUntilModifiedAtAndOrder(pageable, modifiedEnd);
            }
            else if(modifiedEnd == null) {
                todos = todoRepository.findAllBeginModifiedAtAndOrder(pageable, modifiedStart);
            }
            else {
                todos = todoRepository.findAllBetweenModifiedAtAndOrder(pageable, modifiedStart, modifiedEnd);
            }
        }
        else {
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
