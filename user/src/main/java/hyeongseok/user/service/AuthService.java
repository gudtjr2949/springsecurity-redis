package hyeongseok.user.service;

import hyeongseok.user.config.jwt.JwtTokenProvider;
import hyeongseok.user.dto.UserDto;
import hyeongseok.user.entity.User;
import hyeongseok.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;


    public void join(UserDto userDto) {
        User user = userDto.toUser(passwordEncoder);
        userRepository.save(user);
    }

    @Transactional
    public UserDto login(UserDto userDto) {
        // 로그인할 때 입력한 이메일과 비밀번호로 인증용 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = userDto.toAuthentication();

        // 인증용 토큰으로 실제 존재하는 유저인지 체크
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 생성된 authentication 객체를 사용해 JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        jwtTokenProvider.generateRefreshToken(authentication);

        return new UserDto(userDto.getUserName(), userDto.getEmail(), accessToken);
    }

    /*
     * Access Token 이 만료되었으므로, Redis 에 있는 Refresh Token 조회
     * 만약 Refresh Token 까지 만료되었으면 재로그인 해야 함
     * 하지만 Refresh Token 이 만료되지 않았으면, Access Token 재발급
     */
    public String reissueAccessToken(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

            if (refreshToken.equals(redisTemplate.opsForValue().get(authentication.getName())))
                return jwtTokenProvider.generateAccessToken(authentication);
            else {
                throw new RuntimeException("요청한 Refresh Token 불일치");
            }
        } else {
            throw new RuntimeException("Access Token && Refresh Token 만료. 재로그인 필요");
        }
    }
}