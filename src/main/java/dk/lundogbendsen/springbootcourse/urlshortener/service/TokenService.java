package dk.lundogbendsen.springbootcourse.urlshortener.service;

import dk.lundogbendsen.springbootcourse.urlshortener.model.Token;
import dk.lundogbendsen.springbootcourse.urlshortener.model.User;
import dk.lundogbendsen.springbootcourse.urlshortener.service.exceptions.*;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private HashMap<String, Token> tokens = new HashMap<>();


    public List<Token> listUserTokens(User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        final List<Token> userTokens = this.tokens.values().stream().filter(token -> token.getUser().getUsername().equals(user.getUsername())).collect(Collectors.toUnmodifiableList());
        return userTokens;
    }

    public void deleteTokens(User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        tokens.values().removeIf(token -> token.getUser().getUsername().equals(user.getUsername()));
    }

    public Token create(String theToken, String targetUrl, String protectToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        if (theToken.equals("token")) {
            throw new IllegalTokenNameException();
        }
        if (tokens.containsKey(theToken)) {
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
        tokens.put(theToken, token);
        return token;
    }

    public Token update(String theToken, String targetUrl, String protectToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        final Token token = tokens.get(theToken);
        if (token == null) {
            throw new TokenNotFoundException();
        }
        if (!token.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException();
        }
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
        return token;
    }

    public void deleteToken(String theToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        final Token token = tokens.get(theToken);
        if (!token.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException();
        }
        tokens.remove(theToken);
    }

    public String resolveToken(String theToken, String protectToken) {
        final Token token = tokens.get(theToken);
        if (token == null) {
            throw new TokenNotFoundException();
        }
        if (token.getProtectToken() != null && !token.getProtectToken().equals(protectToken)) {
            throw new AccessDeniedException();
        }

        return token.getTargetUrl();
    }

    public Token getToken(String theToken, User user) {
        if (user == null) {
            throw new AccessDeniedException();
        }
        final Token token = tokens.get(theToken);
        if (!token.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException();
        }
        return token;
    }
}
