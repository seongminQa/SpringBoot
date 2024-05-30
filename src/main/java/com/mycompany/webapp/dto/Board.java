package com.mycompany.webapp.dto;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Board {
	   private int bno;
	   private String btitle;
	   private String bcontent;
	   private String bwriter;
	   private Date bdate;
	   private int bhitcount;
	   private MultipartFile battach;	// JSON으로 변환이 안됨.
	   private String battachoname;
	   private String battachsname;
	   private String battachtype;
	   private byte[] battachdata;	// JSON으로 변환할 필요가 없음. (파일 데이터 / binary data)
}
