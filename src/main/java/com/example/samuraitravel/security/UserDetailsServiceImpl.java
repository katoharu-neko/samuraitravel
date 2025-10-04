package com.example.samuraitravel.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.UserRepository;

@Service
//UserDetailsServiceImplはSpringSecurityが提供するUserDetailsServiceインターフェースを実装したサービスクラス
public class UserDetailsServiceImpl implements UserDetailsService {
	//依存先のオブジェクトをfinalで宣言することにより、一度初期化されたあとは変更されません。これにより、安全性が向上します。
	private final UserRepository userRepository;
	
	public UserDetailsServiceImpl(UserRepository userRepository) {
	    // 依存性の注入（DI）を行う（コンストラクタインジェクション）
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		try {
			User user = userRepository.findByEmail(email);
			String userRoleName = user.getRole().getName();
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(userRoleName));
			return new UserDetailsImpl(user, authorities);
		} catch (Exception e) {
			throw new UsernameNotFoundException("ユーザーが見つかりませんでした。");
		}
	}

}
