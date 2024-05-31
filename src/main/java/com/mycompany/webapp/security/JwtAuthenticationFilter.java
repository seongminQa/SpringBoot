package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//			throws IOException, ServletException {
//		// AccessToken 얻기
//		String accessToken = null;
//		
//		HttpServletRequest httpServletRequest = (HttpServletRequest)request; // 이 request를 가지고 GetHeader라는 메소드를 사용할 수 없다. 따라서 타입변환을 해주는 것이다.
//		String headerValue = httpServletRequest.getHeader("Authorization");
//		if(headerValue != null && headerValue.startsWith("Bearer")) {
//			accessToken = headerValue.substring(7);
//			log.info(accessToken);
//		}
//		
//		// AccessToken 유효성 검사
////		Jws<Claims> jws = 
//		
//		// 다음 필터를 실행
//		chain.doFilter(request, response);
//	}
	
	@Autowired
	private JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// AccessToken 얻기
			String accessToken = null;
			
			HttpServletRequest httpServletRequest = (HttpServletRequest)request; // 이 request를 가지고 GetHeader라는 메소드를 사용할 수 없다. 따라서 타입변환을 해주는 것이다.
			String headerValue = httpServletRequest.getHeader("Authorization");
			if(headerValue != null && headerValue.startsWith("Bearer")) {
				accessToken = headerValue.substring(7);
				log.info(accessToken);
			}
			
			// AccessToken 유효성 검사
			Jws<Claims> jws = jwtProvider.validateToken(accessToken);
			if(jws != null) {
				// 유효한 경우
				log.info("AccessToken이 유효함");
				String userId = jwtProvider.getUserId(jws);
				log.info("userId : " + userId);
			} else {
				// 유효하지 않은 경우
				log.info("AccessToken이 유효하지 않음");
			}
			
			// 다음 필터를 실행
			filterChain.doFilter(request, response);
	}

}
