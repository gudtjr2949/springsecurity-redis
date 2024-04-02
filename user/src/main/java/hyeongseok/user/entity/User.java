package hyeongseok.user.entity;

import hyeongseok.user.dto.Authority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id @GeneratedValue
    private Long userId;
    private String userName;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Authority authority;
}
