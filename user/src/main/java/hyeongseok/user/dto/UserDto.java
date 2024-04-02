package hyeongseok.user.dto;

import hyeongseok.user.entity.User;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class UserDto {
    private String userName;
    private String email;
    private String password;
    private String accessToken;

    public UserDto(String userName, String email, String accessToken) {
        this.userName = userName;
        this.email = email;
        this.accessToken = accessToken;
    }

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .userName(userName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .authority(Authority.ROLE_USER)
                .build();
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }
}
