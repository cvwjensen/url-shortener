# Exercises for build an API on Url-shortener

At this point, the service layer does not have true persistence. When the application shuts down, all data are gone.

These exercises will bring persistence to MySql.


### Exercise 1: Add data jpa and mysql connector

- Add to pom.xml two dependencies:
    1. the data-jpa springboot starter (gives us Repositories and JPA annotations)
    2. the mysql-connector-java dependency (Add a Mysql datasource).

Hint: you can use a shortcut in the pom for making Intellij find the dependency: ctrl-n (or right-click and "generate") 
    
#### Solution
pom.xml:
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```


### Exercise 2: Change the Domain model to an Entity Model
- Add JPA annotations to the Token and User classes thereby turning them into Entities.
- Add @Entity on both classes
- Add a field `Long id` to the Token class and annotate it as the Id.
- Use the username as an id by annotating it as the Id.

#### Solution
Token:
```java
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    String token;
    String protectToken;
    String targetUrl;
    @ManyToOne
    User user;
}
```

User:
```java
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    String username;
    String password;
}
```


### Exercise 3: Create Repositories
Repositories are access point to the database. We need one per Entity.

- Create a class `UserRepository` for the User entity.
- Create a class `TokenRepository` for the Token entity.

#### Solution
UserRepository:
```java
public interface UserRepository extends JpaRepository<User, String> {
}
```

TokenRepository:
```java
public interface TokenRepository extends JpaRepository<Token, String> {
}
```



### Exercise 4: Refactor UserService to use a Repository
It is easy to just delete the field users and then fix the compilation errors by added the UserRepository and the update the code.
- Add UserRepository as a dependency.
- Delete the field users. This makes the compiler complain.
- Update all the methods to use repository for CRUD operations on User.
- Update the unit test for the Service Layer
- Run the unit test for the UserService 

#### Solution

UserService:
```java
@Service
public class UserService {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;

    public User create(String userName, String password) {
        if (userRepository.existsById(userName)) {
            throw new UserExistsException();
        }
        final User user = User.builder().username(userName).password(password).build();
        userRepository.save(user);
        return user;
    }

    public void delete(String userName) {
        final Optional<User> user = userRepository.findById(userName);
        if (user.isPresent()) {
            tokenService.deleteTokens(user.get());
            userRepository.delete(user.get());
        }
    }

    public User getUser(String userName) {
        return userRepository.findById(userName).orElseThrow(UserNotFoundException::new);
    }
}
```

UserServiceTest:
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    TokenService tokenService;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;

    @Test
    public void createUserTest() {
        final User user = userService.create("user1", "password1");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void getUserTest() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(User.builder().username("fakeuser").password("password1").build()));
        final User fakeUser = userService.getUser("user1");
        assertEquals("password1", fakeUser.getPassword());
    }

}
```


### Exercise 5: Refactor TokenService to use a Repository
It is easy to just delete the field tokens and then fix the compilation errors by added the TokenRepository and the update the code.
- Add TokenRepository as a dependency.
- Delete the field tokens. This makes the compiler complain.
- Update all the methods to use repository for CRUD operations on Token.
- Update the unit test for the Service Layer
- Run the unit test for the TokenService 

#### Solution
TokenService:
```java
@Service
public class TokenService {
    @Autowired
    TokenRepository tokenRepository;



    public List<Token> listUserTokens(User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        final List<Token> userTokens = tokenRepository.findAllByUser(user);
        return userTokens;
    }

    public void deleteTokens(User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        tokenRepository.deleteAllByUser(user);
    }

    public Token create(String theToken, String targetUrl, String protectToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        if (theToken.equals("token")) {
            throw new IllegalTokenNameException();
        }
        Optional<Token> existingToken = tokenRepository.findById(theToken);
        if (existingToken.isPresent()) {
            throw new TokenAlreadyExistsException();
        }
        if (targetUrl == null) {
            throw new TokenTargetUrlIsNullException();
        }
        if (targetUrl.contains("localhost")) {
            throw new IllegalTargetUrlException();
        }
        try {
            new URL(targetUrl);
        } catch (MalformedURLException e) {
            throw new InvalidTargetUrlException();
        }

        final Token token = Token.builder().token(theToken).targetUrl(targetUrl).protectToken(protectToken).user(user).build();
        tokenRepository.save(token);
        return token;
    }

    public Token update(String theToken, String targetUrl, String protectToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }

        final Optional<Token> tokenOptional = tokenRepository.findByTokenAndUser(theToken, user);
        if (!tokenOptional.isPresent()) {
            throw new TokenNotFoundException();
        }

        final Token token = tokenOptional.get();
        if (targetUrl == null) {
            targetUrl = token.getTargetUrl();
        }
        if (targetUrl.contains("localhost")) {
            throw new IllegalTargetUrlException();
        }
        try {
            new URI(targetUrl);
        } catch (URISyntaxException e) {
            throw new InvalidTargetUrlException();
        }

        token.setTargetUrl(targetUrl);
        token.setProtectToken(protectToken);
        return tokenRepository.save(token);
    }

    public void deleteToken(String theToken, User user) {
        tokenRepository.deleteByUser(theToken, user);
    }

    public String resolveToken(String theToken, String protectToken) {
        final Optional<Token> token = tokenRepository.findByTokenAndProtectToken(theToken, protectToken);
        if (!token.isPresent()) {
            throw new TokenNotFoundException();
        }
        return token.get().getTargetUrl();
    }

    public Token getToken(String theToken, User user) {
        final Optional<Token> token = tokenRepository.findByTokenAndUser(theToken, user);
        if (!token.isPresent()) {
            throw new TokenNotFoundException();
        }
        return token.get();
    }
}

```


TokenServiceTest:
```java
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    TokenRepository tokenRepository;
    @InjectMocks
    TokenService tokenService;
    private User user = User.builder().username("username").password("password").build();

    @Test
    @DisplayName("create token with the name 'token' (fails)")
    public void testCreateTokenWithTheNameToken() {
        try {
            tokenService.create("token", "https://dr.dk", null, user);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("create token that already exists (fails)")
    public void testCreateTokenThatAlreadExists() {
        try {
            when(tokenRepository.findById("token1")).thenReturn(Optional.of(Token.builder().build()));
            tokenService.create("token1", "https://dr.dk", null, user);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("create token without a targetUrl (fails)")
    public void testCreateTokenWithoutTargetUrl() {
        try {
            tokenService.create("token1", null, null, user);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("create token with an invalid targetUrl (fails)")
    public void testCreateTokenWithInvalidTargetUrl() {
        try {
            tokenService.create("token1", "htt", null, user);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("create token with a targetUrl containing localhost (fails)")
    public void testCreateTokenWithTargetUrlContainingLocalhost() {
        try {
            tokenService.create("token1", "http://localhost:8080/abc", null, user);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("create token with a legal targetUrl (success)")
    public void testCreateTokenWithLocalTargetUrl() {
        tokenService.create("abc", "https://dr.dk", "pt1", user);
    }
}
```