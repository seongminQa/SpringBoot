package com.mycompany.webapp.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@RestController  // @ResponseBody 필요없이 모든 리턴값은 JSON형식으로 응답 바디로 반환됨
@Slf4j
@RequestMapping("/board")
public class BoardController {
	@Autowired
	private BoardService boardService;
	
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
		// 페이징 대상이 되는 전체 행 수 얻기
		int totalRows = boardService.getCount();
		// 페이저 객체 생성
		Pager pager = new Pager(10, 5, totalRows, pageNo);
		// 해당 페이지의 게시물 목록 가져오기
		List<Board> list = boardService.getList(pager);
		// 여러 객체를 리턴하기 위해 Map 객체 생성
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		
		return map;	// {"boards": [ ... ], "pager": [ ... ] } 이런 형식으로 전달된다.
	}
	
	@PostMapping("/create")
	public Board create(Board board) {
//		boardService.insert(board);
		
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			// 첨부파일이 넘어왔을 경우 처리
			MultipartFile mf = board.getBattach();
			// 파일 이름을 설정
			board.setBattachoname(mf.getOriginalFilename());
			// 파일 종류를 설정
			board.setBattachtype(mf.getContentType());
			try {
				// 파일 데이터를 설정
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
			}
		}
		// DB에 저장
		board.setBwriter("user");
		boardService.insert(board);
		// JSON으로 변환되지 않는 필드는 null 처리를 해야 한다.
		board.setBattach(null);
		board.setBdate(null);
		
		return board;	// {"bno":1, "btitle":"xxx", ... }
	}
	
	
	
	
//	@GetMapping("/read/{bno}")  // http://localhost/board/read/3
//	public Board read(@PathVariable int bno) {  // @PathVariable 경로상 주어진 변수
//		Board board = new Board();
//		board.setBno(bno);
//		board.setBtitle("제목" + bno);
//		board.setBcontent("내용" + bno);
//		board.setBwriter("글쓴이" + bno);
//		
//		return board;  // 자동적으로 ResponseBody에 들어간다. JSON으로 변환되어서
//	}
//	
//	// 양식을 저장
//	@PostMapping("/create")
//	public Board create(Board board) {
//		log.info(board.toString());
//		MultipartFile mf = board.getBattach();  // JSON으로 표현이 되지 않는다. // 때문에 에러가 났던 것.
//		board.setBattachoname(mf.getOriginalFilename());
//		board.setBattachtype(mf.getContentType());
//		
//		// Service를 통해서 게시물을 저장
//		board.setBattach(null);
//		return board;
//	}
//	
//	@PutMapping("/update")
//	public Board update(@RequestBody Board board) {
//		log.info(board.toString());
//		// Service를 통해서 게시물 수정
//		return board;
//	}
	
}
