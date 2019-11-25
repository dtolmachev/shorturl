package com.dtolmachev.urlshortener.httpserver.util.routing;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
@Builder(toBuilder = true)
public class RouteSpec {

    @Singular
    Set<String> methods;

    @NonNull
    Pattern pattern;

    @NonNull
    BiFunction<RequestSpec, Matcher, String> converter;

    public boolean pathMatch(RequestSpec request) {
        Matcher matcher = pattern.matcher(request.getUri());
        return matcher.matches();
    }

    public Optional<String> methodMatch(RequestSpec request) {
        if (!methods.isEmpty() && !methods.contains(request.getMethod())) {
            return empty();
        }
        Matcher matcher = pattern.matcher(request.getUri());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return of(converter.apply(request, matcher));
    }
}
