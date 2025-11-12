// src/main/java/com/carrot/repository/BoardRepository.java
package com.carrot.repository;

import com.carrot.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByBoardTypeOrderByCreatedAtDesc(String boardType);
    List<Board> findByBoardTypeAndTitleContainingOrderByCreatedAtDesc(String boardType, String keyword);
}