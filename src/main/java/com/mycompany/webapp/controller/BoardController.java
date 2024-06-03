package com.mycompany.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	
//	@Secured("ROLE_ADMIN") // 게시물을 로그인한 유저만 볼 수 있도록 어노테이션을 붙일 수 있다.
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
	
	// @Secured("ROLE_USER")  // 로그인한 유저만 게시글 생성 가능하도록 설정  // 몇몇 스프링 버전에 따라 버그가 일어날 수 있다는 얘기가 있다. 따라서 밑의 @PreAuthorize 를 추가한 것이다.
	@PreAuthorize("hasAuthority('ROLE_USER')")  // @Secured와 비슷하게 사용할 수 있는 어노테이션 // 다양한 표현식까지 사용할 수 있다.
	@PostMapping("/create")
	public Board create(Board board, Authentication authentication) {  // 로그인한 유저의 아이디를 얻기 위하여 Authentication 추가
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
//		board.setBwriter("user");  // 처음 테스트를 위해 user라는 고정값을 줌
		board.setBwriter(authentication.getName());  // authentication.getName() --> 로그인한 유저의 아이디 얻기 
		boardService.insert(board);
		// JSON으로 변환되지 않는 필드는 null 처리를 해야 한다. // 자동적으로 JSON으로 변환되어 값이 들어가기 때문에 해줌
		board.setBattach(null);
		board.setBdate(null);
		
		return board;	// {"bno":1, "btitle":"xxx", ... }
	}
	
	/*@GetMapping("/read")	// http://localhost/read?bno=5 --> 쿼리 스트링 방식
	public Board read(int bno) {

	}*/
	
	@GetMapping("/read/{bno}")	// http://localhost/read/5 --> PathVariable 방식
	public Board read(@PathVariable int bno) {
		// bno에 해당하는 Board 객체 얻기
		Board board = boardService.getBoard(bno);
		// JSON으로 변환되지 않는 필드는 null처리
		board.setBattachdata(null);
		return board;
	}
	
//	@Secured("ROLE_USER")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@PutMapping("/update")
	//public Board update(@RequestBody Board board) {
	public Board update(Board board) {  // 첨부가 넘어왔을 경우 @RequestBody를 빼야한다.
		
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
		
		// board 수정하기
		boardService.update(board);
		//return board; // 완전한 데이터가 들어가지 않음. 수정할 내용만 들어가있다.
		// 따라서 현재 해당 bno에 대한 board(수정한 보드)를 얻어 리턴한다.
		// 수정된 내용의 Board 객체 얻기
		board = boardService.getBoard(board.getBno());
		// JSON으로 변환되지 않는 필드는 null처리
		board.setBattach(null);
		board.setBattachdata(null);
		
		return board;
	}
	
	// 게시물 삭제
//	@Secured("ROLE_USER")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@DeleteMapping("/delete/{bno}")
	public void delete(@PathVariable int bno) {
		boardService.delete(bno);
	}
	
	// 첨부 파일 다운로드
//	@Secured("ROLE_USER")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		// 해당 게시물 가져오기
		Board board = boardService.getBoard(bno);
		
		// 크롬이나 사파리, 엣지도 포함해서
		// 첨부 파일의 이름이 '한글'일 경우에는 문자가 깨질 수 있다.
		// 파일 이름이 한글일 경우, 브라우저에서 한글 이름으로 다운로드 받기 위해 헤더에 추가할 내용
		/*String fileName;
		try {
			// 첨부가 있는 경우에만 요청 할 수 있다. 없는 경우에는 이 요청 자체를 할 수가 없다.
			fileName = new String(board.getBattachoname().getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		} catch (UnsupportedEncodingException e) {
		}
		
		// 파일 타입을 헤더의 Content 부분에 추가
		response.setContentType(board.getBattachtype());
		// 응답 바디에 파일 데이터를 출력
		try {
			OutputStream os = response.getOutputStream();  // 하나의 실행동작이기 때문에 위의 try구문에 넣어도 가능할 것이다.
			os.write(board.getBattachdata());
			os.flush();
			os.close();
		} catch (IOException e) {
		}*/
		
		try {
			// 첨부가 있는 경우에만 요청 할 수 있다. 없는 경우에는 이 요청 자체를 할 수가 없다.
			String fileName = new String(board.getBattachoname().getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			// 파일 타입을 헤더의 Content 부분에 추가
			response.setContentType(board.getBattachtype());
			OutputStream os = response.getOutputStream();
			os.write(board.getBattachdata());
			os.flush();
			os.close();
		} catch (IOException e) {
			log.error(e.toString());
		}
		
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
