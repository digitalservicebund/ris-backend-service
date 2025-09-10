package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

public class UserMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private final UserService userService;

  public UserMethodArgumentResolver(UserService userService) {
    this.userService = userService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(User.class);
  }

  @Override
  public Object resolveArgument(
      @NotNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      @NotNull NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    OidcUser oidcUser =
        (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return userService
        .getUser(oidcUser)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Could not identify logged in user"));
  }
}
