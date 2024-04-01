package hyeongseok.user.service;

import hyeongseok.user.dto.UserDto;
import hyeongseok.user.entity.User;
import hyeongseok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void join(UserDto userDto) {
        userRepository.save(User.builder()
                .userName(userDto.getUserName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .build());
    }
}
