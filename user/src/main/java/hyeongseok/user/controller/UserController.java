package hyeongseok.user.controller;

import hyeongseok.user.dto.TokenDto;
import hyeongseok.user.dto.UserDto;
import hyeongseok.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final AuthService authService;

    /**
     * 회원가입
     * @param userDto
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserDto userDto) {
        authService.join(userDto);
        return ResponseEntity.status(HttpStatus.OK).body("회원가입 완료");
    }

    /**
     * 로그인
     * @param userDto
     * @return Access Token
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(userDto));
    }

    @GetMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization-refresh");
        String refreshToken = "";

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            refreshToken = bearerToken.substring(7);
        }

        String newAccessToken = authService.reissueAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(new TokenDto(newAccessToken, ""));
    }

    @GetMapping("/test")
    public ResponseEntity<String> accessTokenTest() {
        return ResponseEntity.status(HttpStatus.OK).body("인증 성공");
    }
}
