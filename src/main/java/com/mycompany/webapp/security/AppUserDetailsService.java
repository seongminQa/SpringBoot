package com.mycompany.webapp.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mycompany.webapp.dao.MemberDao;
import com.mycompany.webapp.dto.Member;

@Service
public class AppUserDetailsService implements UserDetailsService {
   @Autowired
   private MemberDao memberDao;   
   
   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Member member = memberDao.selectByMid(username); 
      
      // 유저의 id가 없다면
      if(member == null) {
         throw new UsernameNotFoundException(username); // 예외를 발생시킴
      }
      
      // 권한을 리스트 형태로 관리
      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority(member.getMrole()));
      
      AppUserDetails userDetails = new AppUserDetails(member, authorities);
      return userDetails;
   }
}

