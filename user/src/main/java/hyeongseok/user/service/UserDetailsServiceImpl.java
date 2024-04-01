package hyeongseok.user.service;

import hyeongseok.user.entity.CustomUserDetails;
import hyeongseok.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // username (email) 이 DB에 존재하는지 확인
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 시큐리티 세션에 유저 정보 저장
        return new CustomUserDetails(userRepository.findUserByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("사용자가 존재하지 않습니다.")));
    }
}