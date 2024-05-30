package com.mycompany.webapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;

import lombok.extern.slf4j.Slf4j;

@RestController  // @ResponseBody 필요없이 모든 리턴값은 JSON형식으로 응답 바디로 반환됨
@Slf4j
@RequestMapping("/board")
public class BoardController {
	@GetMapping("/list")
	public List<Board> list() {
		List<Board> list = new ArrayList<>();
		
		for(int i=1; i<=10; i++) {
			Board board = new Board();
			board.setBno(i);
			board.setBtitle("제목" + i);
			board.setBcontent("내용" + i);
			board.setBwriter("글쓴이" + i);
			list.add(board);
		}
		
		return list;
	}
	
	@GetMapping("/read/{bno}")  // http://localhost/board/read/3
	public Board read(@PathVariable int bno) {  // @PathVariable 경로상 주어진 변수
		Board board = new Board();
		board.setBno(bno);
		board.setBtitle("제목" + bno);
		board.setBcontent("내용" + bno);
		board.setBwriter("글쓴이" + bno);
		
		return board;  // 자동적으로 ResponseBody에 들어간다. JSON으로 변환되어서
	}
	
	// 양식을 저장
	@PostMapping("/create")
	public Board create(Board board) {
		log.info(board.toString());
		MultipartFile mf = board.getBattach();  // JSON으로 표현이 되지 않는다. // 때문에 에러가 났던 것.
		board.setBattachoname(mf.getOriginalFilename());
		board.setBattachtype(mf.getContentType());
		
		// Service를 통해서 게시물을 저장
		board.setBattach(null);
		return board;
	}
	
	@PutMapping("/update")
	public Board update(@RequestBody Board board) {
		log.info(board.toString());
		// Service를 통해서 게시물 수정
		return board;
	}
	
}
