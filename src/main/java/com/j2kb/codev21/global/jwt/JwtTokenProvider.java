package com.j2kb.codev21.global.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMillisecondsAccess;
    private final long tokenValidityInMillisecondsRefresh;

    private Key keyAccess;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token-validity-in-seconds-access}") long tokenValidityInSecondsAccess,
        @Value("${jwt.token-validity-in-seconds-refresh}") long tokenValidityInSecondsRefresh) {
        this.secret = secret;
        this.tokenValidityInMillisecondsAccess = tokenValidityInSecondsAccess * 1000;
        this.tokenValidityInMillisecondsRefresh = tokenValidityInSecondsRefresh * 1000;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyAccessByte = Decoders.BASE64.decode(secret);
        this.keyAccess = Keys.hmacShaKeyFor(keyAccessByte);
    }

    public String doGenerateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validityAccess = new Date(now + this.tokenValidityInMillisecondsAccess);

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(keyAccess, SignatureAlgorithm.HS512)
            .setExpiration(validityAccess) //??????????????????
            .compact();

    }

    public String doGenerateRefreshToken(String email) {

        long now = (new Date()).getTime();
        Date validityRefresh = new Date(now + this.tokenValidityInMillisecondsRefresh);

        return Jwts.builder()
            .setSubject(email)
            .signWith(keyAccess, SignatureAlgorithm.HS512)
            .setExpiration(validityRefresh) //??????????????????
            .compact();

//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//            .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
//            .signWith(SignatureAlgorithm.HS512, secret).compact();

    }

    //???????????? ?????? ????????? ?????? GET
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
            .parserBuilder()
            .setSigningKey(keyAccess)
            .build()
            .parseClaimsJws(token)
            .getBody();

        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(keyAccess).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("????????? JWT ???????????????.");
        } catch (ExpiredJwtException e) {
            log.info("????????? JWT ???????????????.");
        } catch (UnsupportedJwtException e) {
            log.info("???????????? ?????? JWT ???????????????.");
        } catch (IllegalArgumentException e) {
            log.info("JWT ????????? ?????????????????????.");
        }
        return false;
    }
//    //retrieve expiration date from jwt token
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    //for retrieveing any information from token we will need the secret key
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
//    }
//
//    //check if the token has expired
//    public Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }

    //?????? ?????????
    public Boolean validateExceptionToken(String token) throws ExpiredJwtException, io.jsonwebtoken.security.SecurityException,
        MalformedJwtException, UnsupportedJwtException, IllegalArgumentException {
        Jwts.parserBuilder().setSigningKey(keyAccess).build().parseClaimsJws(token);
        return true;
    }

}
