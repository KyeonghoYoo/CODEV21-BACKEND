package com.j2kb.codev21.global.config;

import com.j2kb.codev21.global.jwt.JwtAccessDeniedHandler;
import com.j2kb.codev21.global.jwt.JwtAuthenticationEntryPoint;
import com.j2kb.codev21.global.jwt.JwtSecurityConfig;
import com.j2kb.codev21.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
        RedisTemplate<String, Object> redisTemplate,
        JwtTokenProvider tokenProvider,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
            .disable() // rest api ????????? ???????????? ????????????.
            // token??? ???????????? ???????????? ????????? csrf
            .csrf().disable() //csrf ??????


            .exceptionHandling()
            //?????? ?????? ????????? ????????? ?????? Exception ??????
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)


            // enable h2-console
            .and()
            .headers()
            .frameOptions()
            .sameOrigin()

            // ????????? ???????????? ?????? ????????? STATELESS??? ??????
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()
            //.antMatchers("/**").permitAll()
            //.antMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
            .antMatchers("/**").permitAll() //????????? ??????
            .antMatchers( "/api/v1/login").permitAll() //?????????
            .antMatchers( "/api/v1/users").permitAll() //????????????
            .antMatchers( "/api/v1/admin/**").permitAll() //?????????
            .antMatchers( "/api/v1/oauth/**").permitAll() //???????????????
            .antMatchers( "/api/v1/boards").permitAll() //???????????????

            //.antMatchers(HttpMethod.GET, "/api/v1/admin/users").permitAll()
//            .antMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
//            .antMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
//            .antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
//            .antMatchers(HttpMethod.GET, "/api/v1/auth/**").permitAll()
            //.antMatchers("/api/v1/members/**").permitAll()

            //????????? ????????? ??????????????????, ??????(USER ??????)??? ?????? ???????????? ?????? ??????
            //.antMatchers("/api/v1/users/**").hasAnyAuthority(Role.USER.getCode())

            .anyRequest().authenticated()


            .and()
            //???????????? ?????? ????????? JWT ????????? ???????????? ??????
            .apply(new JwtSecurityConfig(tokenProvider, redisTemplate));
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        //?????? ????????? ???????????? Spring Security??? ????????? ??? ????????? ??????

        // h2-console??? ???????????? ????????????
        web
            .ignoring()
            .antMatchers(
                "/h2-console/**",
                "/favicon.ico"

            );
    }
}

