// src/main/java/com/carrot/service/BoardService.java
package com.carrot.service;

import com.carrot.entity.Board;
import com.carrot.entity.User;
import com.carrot.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    public List<Board> getBoardList(String boardType) {
        return boardRepository.findByBoardTypeOrderByCreatedAtDesc(boardType);
    }

    public Board getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setViewCount(board.getViewCount() + 1);
        return board;
    }

    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }

    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    public List<Board> searchBoard(String boardType, String keyword) {
        return boardRepository.findByBoardTypeAndTitleContainingOrderByCreatedAtDesc(boardType, keyword);
    }
}