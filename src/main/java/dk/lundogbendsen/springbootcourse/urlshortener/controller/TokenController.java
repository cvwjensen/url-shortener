package dk.lundogbendsen.springbootcourse.urlshortener.controller;

import dk.lundogbendsen.springbootcourse.urlshortener.controller.security.SecurityContext;
import dk.lundogbendsen.springbootcourse.urlshortener.model.Token;
import dk.lundogbendsen.springbootcourse.urlshortener.service.TokenService;
import dk.lundogbendsen.springbootcourse.urlshortener.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/token")
public class TokenController {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserService userService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Token> list() {
        return tokenService.listUserTokens(SecurityContext.getUser());
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Token create(@RequestBody Map<String, String> body) {
        final String token = body.get("token");
        final String targetUrl = body.get("targetUrl");
        final String protectToken = body.get("protectToken");
        return tokenService.create(token, targetUrl, protectToken, SecurityContext.getUser());
    }

    @PutMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Token update(@PathVariable String token, @RequestBody Map<String, String> body) {
        final String targetUrl = body.get("targetUrl");
        final String protectToken = body.get("protectToken");
        return tokenService.update(token, targetUrl, protectToken, SecurityContext.getUser());
    }

    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String token) {
        tokenService.deleteToken(token, SecurityContext.getUser());
    }

    @PutMapping("/{token}/protect")
    @ResponseStatus(HttpStatus.CREATED)
    public Token protect(@PathVariable("token") String theToken, @RequestBody Map<String, String> body) {
        final Token token = tokenService.getToken(theToken, SecurityContext.getUser());
        String protectToken = body.get("protectToken");
        return tokenService.update(theToken, token.getTargetUrl(), protectToken, SecurityContext.getUser());
    }
}
