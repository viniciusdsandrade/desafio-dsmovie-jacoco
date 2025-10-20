package com.devsuperior.dsmovie.restassured.config.customgrant;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

public record CustomPasswordAuthenticationProvider(
        OAuth2AuthorizationService authorizationService,
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
) implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    public CustomPasswordAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "TokenGenerator cannot be null");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        Assert.notNull(passwordEncoder, "PasswordEncoder cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        CustomPasswordAuthenticationToken custom =
                (CustomPasswordAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(custom);

        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        String username = custom.getUsername();
        String password = custom.getPassword();

        UserDetails user;
        try {
            user = userDetailsService().loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new OAuth2AuthenticationException("Invalid credentials");
        }

        if (!passwordEncoder().matches(password, user.getPassword())
                || !user.getUsername().equals(username)) {
            throw new OAuth2AuthenticationException("Invalid credentials");
        }

        Set<String> authorizedScopes = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(registeredClient.getScopes()::contains)
                .collect(Collectors.toSet());

        // ---- SecurityContext como no original
        OAuth2ClientAuthenticationToken oauth2ClientAuth =
                (OAuth2ClientAuthenticationToken) SecurityContextHolder
                        .getContext().getAuthentication();
        CustomUserAuthorities details = new CustomUserAuthorities(username, user.getAuthorities());
        oauth2ClientAuth.setDetails(details);

        var newCtx = SecurityContextHolder.createEmptyContext();
        newCtx.setAuthentication(oauth2ClientAuth);
        SecurityContextHolder.setContext(newCtx);

        // ---- Tokens (mesma lógica)
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(clientPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .authorizationGrant(custom);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .attribute(Principal.class.getName(), clientPrincipal)
                .principalName(clientPrincipal.getName())
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .authorizedScopes(authorizedScopes);

        OAuth2TokenContext tokenContext =
                tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = tokenGenerator().generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.",
                    ERROR_URI);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(),
                tokenContext.getAuthorizedScopes());

        if (generatedAccessToken instanceof ClaimAccessor ca) {
            authorizationBuilder.token(accessToken, meta ->
                    meta.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ca.getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        authorizationService().save(authorization);

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // Métodos estáticos continuam válidos em records
    private static OAuth2ClientAuthenticationToken
    getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class
                .isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
