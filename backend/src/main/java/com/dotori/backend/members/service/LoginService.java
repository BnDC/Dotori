package com.dotori.backend.members.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dotori.backend.members.Repository.MemberRepository;
import com.dotori.backend.members.model.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다."));

		return org.springframework.security.core.userdetails.User.builder()
			.username(member.getEmail())
			.roles(member.getRole().name())
			.build();
	}
}