package zerobase.stock.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.stock.service.MemberService;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final SecretKey secretKey;

    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
    private static final String KEY_ROLES = "roles";

    private final MemberService memberService;

    public String generateToken(String username, List<String> roles) {

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(username)
                .claim(KEY_ROLES, roles)
                .issuedAt(now) // 토큰 생성 시간
                .expiration(expiredDate) // 토큰 만료 시간
                .signWith(secretKey) // 사용할 암호화 알고리즘, 비밀키
                .compact();

    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

         return !parseClaims(token).getExpiration().before(new Date());

    }

    private Claims parseClaims(String token) {

        try {
            return  Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = memberService.loadUserByUsername(getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

}
