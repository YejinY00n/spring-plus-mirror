package org.example.expert.domain.todo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE t.weather LIKE CONCAT('%', :weather, '%') "
        + "AND DATE(t.modifiedAt) <= :modifiedEnd "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByWeatherUntilModifiedAtAndOrder(
        Pageable pageable, @Param("weather") String weather, @Param("modifiedEnd") LocalDate modifiedEnd);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE t.weather LIKE CONCAT('%', :weather, '%') "
        + "AND DATE(t.modifiedAt) >= :modifiedStart "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByWeatherBeginModifiedAtAndOrder(
        Pageable pageable, @Param("weather") String weather, @Param("modifiedStart") LocalDate modifiedStart);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE t.weather LIKE CONCAT('%', :weather, '%') "
        + "AND DATE(t.modifiedAt) BETWEEN :modifiedStart AND :modifiedEnd "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByWeatherBetweenModifiedAtAndOrder(
        Pageable pageable, @Param("weather") String weather,
        @Param("modifiedStart") LocalDate modifiedStart, @Param("modifiedEnd") LocalDate modifiedEnd);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE DATE(t.modifiedAt) <= :modifiedEnd "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllUntilModifiedAtAndOrder(Pageable pageable, @Param("modifiedEnd") LocalDate modifiedEnd);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE DATE(t.modifiedAt) >= :modifiedStart "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllBeginModifiedAtAndOrder(Pageable pageable, @Param("modifiedStart") LocalDate modifiedStart);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE DATE(t.modifiedAt) BETWEEN :modifiedStart AND :modifiedEnd "
        + "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllBetweenModifiedAtAndOrder(Pageable pageable,
        LocalDate modifiedStart, @Param("modifiedEnd") LocalDate modifiedEnd);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
        + "WHERE t.weather LIKE CONCAT('%', :weather, '%') ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByWeatherAndOrder(Pageable pageable, @Param("weather") String weather);

//    @Query("SELECT t FROM Todo t " +
//            "LEFT JOIN t.user " +
//            "WHERE t.id = :todoId")
//    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

}
