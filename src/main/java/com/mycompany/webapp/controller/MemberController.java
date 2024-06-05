package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.AppUserDetails;
import com.mycompany.webapp.security.AppUserDetailsService;
import com.mycompany.webapp.security.JwtProvider;
import com.mycompany.webapp.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController // @Controller + @ResponseBody
@RequestMapping("/member")
public class MemberController {
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	// GetMapping을 사용해도 상관없긴 하다.
	@PostMapping("/login")
	public Map<String, String> userLogin(String mid, String mpassword) {
		// 1. 사용자 상세 정보 얻기
		AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(mid); // AppUserDetailsService에서 유저의 아이디가 없다면 예외를 발생시키도록 했음.
		// 2. 비밀번호 체크하기  // DB에서 '{알고리즘} 암호화된 비밀번호' 형식을 기억해보자.
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		boolean checkResult = passwordEncoder.matches(
				mpassword, userDetails.getMember().getMpassword()); // 첫번째 매개값은 입력받은 Password이고, 두번째 매개값은 DB에서 암호화된 Password값을 얻는다.
		
		// Spring 시큐리티 인증 처리
		if(checkResult == true) {
			Authentication authentication = 
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		// 응답 생성
		Map<String, String> map = new HashMap<>();
		
		
		if(checkResult) {
			// true일 경우 AccessToken을 생성한다.
			String accessToken = jwtProvider.createAccessToken(mid, userDetails.getMember().getMrole());
			// JSON 응답 구성
			map.put("result", "success");
			map.put("mid", mid);
			map.put("accessToken", accessToken);
		} else {
			map.put("result", "fail");
		}
		return map;
	}
	
	// 회원가입 및 로그인 처리 -----------------------------------------------------------
	@Autowired
	private MemberService memberService;
	
    @PostMapping("/join")
    public Member join(@RequestBody Member member) {
       // 비밀번호 암호화 
       PasswordEncoder passwordEncoder = PasswordEncoderFactories
             .createDelegatingPasswordEncoder();
       // member의 비밀번호 인코더를 이용하여 세팅하기.
       member.setMpassword(passwordEncoder.encode(member.getMpassword()));
     
     
       // 아이디 활성화 설정
       member.setMenabled(true);
     
       // 권한 설정
       member.setMrole("ROLE_USER");
     
       // 회원가입 처리
       memberService.join(member);
     
       // 응답 json 형식에 password 제거
       // JSON형식으로 데이터를 보낼 때, 비밀번호는 보내지 않는다! // 비밀번호 제거
       member.setMpassword(null);
     
       return member;
    }
}
