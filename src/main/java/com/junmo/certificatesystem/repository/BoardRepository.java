package com.junmo.certificatesystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.junmo.certificatesystem.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
            SELECT b FROM Board b
            JOIN FETCH b.author
            WHERE b.deleted = false
            ORDER BY b.createdAt DESC
            """)
    List<Board> findAllActive();

    @Query("""
            SELECT b FROM Board b
            JOIN FETCH b.author
            WHERE b.id = :id AND b.deleted = false
            """)
    Optional<Board> findActiveById(@Param("id") Long id);
}
