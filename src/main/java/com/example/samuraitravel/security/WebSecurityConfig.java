package com.example.samuraitravel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// ★ 追加：HTTP メソッド指定用
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // 静的リソースなどは従来どおり許可
                .requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**").permitAll()
                .requestMatchers("/", "/signup/**").permitAll()

                // 一覧/詳細などの閲覧は匿名OK（※ {id} は ant パターンで * にする）
                .requestMatchers("/houses").permitAll()
                .requestMatchers("/houses/*").permitAll()
                .requestMatchers("/houses/*/reviews").permitAll()
                .requestMatchers("/stripe/webhook").permitAll()

                // ★ 追加：民宿一覧ページの「空き検索」APIを匿名で許可
                .requestMatchers(HttpMethod.GET, "/api/houses/available").permitAll()

                // ★ 追加：民宿詳細ページで使う予約イベント取得(閲覧専用)も匿名で許可
                .requestMatchers(HttpMethod.GET, "/houses/*/calendar-events").permitAll()

                // 管理者/オーナー向けの保護ルート
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/owner/**").hasRole("OWNER")

                // 上記以外は要ログイン
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/?loggedIn")
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/?loggedOut")
                .permitAll()
            )
            // Stripe Webhook は CSRF 対象外
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/stripe/webhook")));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
