# Introduce

Spring Security를 사용해 유저의 인증/인가 기능을 구현하고, Redis 를 사용해 Access Token 재발급 로직을 구현

</br>

# Install Spring Security, JWT, Redis

build.gradle 에 Spring Security, JWT, Redis 의존성을 설정함

```gradle

// Spring Security
implementation 'org.springframeworkboot:spring-boot-starter-security'

// Redis
implementation 'org.springframework.boot:spring-boot-starter-data-redis'

// JWT
implementation 'io.jsonwebtoken:jjwt:0.9.1'
implementation 'com.sun.xml.bind:jaxb-impl:4.0.1'
implementation 'com.sun.xml.bind:jaxb-core:4.0.1'

```

</br>

# resources/application.yml

- JWT 토큰(Access Token, Refresh Token) 암호화에 사용할 키, 토큰 별 만료기간을 설정
- Redis 연결을 위한 Host, Port 설정

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_URL}/userDB?serverTimezone=Asia/Seoul
    username: ${DB_USER}

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

  driver:
    path: chromedriver
  jwt:
    secret: ${JWT_KEY} # 토큰 암호화를 위한 암호화 키 설정
    token:
      access-expiration-time: 60000    # 1분
      refresh-expiration-time: 604800000   # 7일

  data:
    redis:
      host: localhost
      port: 6379
```

</br>

# JWT Token Provider

Access Token, Refresh Token 을 생성하고, Refresh Token은 Redis 에 저장함

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public void generateRefreshToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // redis 에 저장
        redisTemplate.opsForValue().set(
                authentication.getName(),
                refreshToken,
                refreshExpirationTime,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 토큰으로부터 Claims 을 만들고, 이를 통해 User 객체와 Authentication 객체 리턴
     */
    public Authentication getAuthentication(String token) {
        String email = Jwts.parser().
                setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * Access Token 검증
     */
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch(ExpiredJwtException e) {
            log.error("Token 만료");
            throw new RuntimeException("Token 만료. 재발급 필요");
        } catch(JwtException e) {
            log.error("잘못된 Access Token 타입");
            throw new RuntimeException("잘못된 Access Token 타입");
        } catch (IllegalArgumentException e) {
            log.error("헤더가 비어있음");
            throw new RuntimeException("헤더가 비어있음");
        }
    }
}

```

</br>

# Blog

https://velog.io/@gudtjr2949/Spring-Security-Access-Token-Refresh-Token-Redis
