# 25. define domain exceptions handling

Date: 2025-11-14

## Status

Proposed

## Context

We want to handle our domain exceptions in a centralized way that is establishing rules for how our exceptions are defined and what data they carry. This should also make handling of our exceptions easier since they will follow a pattern where all exceptions relay data uniformly.

The idea is simply that since all HTTP exceptions fall into well known categories (400, 401, 403, 404, 500, etc.) one or more of our domain exceptions should be mapped to these categories.
By defining a common interface for all of our domain exceptions we ensure:
- uniform representation of every error
- handlers that are easier to read (example below)
- easier mapping of data

### Current state

Project already uses @ControllerAdvice annotation to handle exceptions. The setup at this moment is to define a handler for each domain exception.
Example below is from our code:
```java
@ExceptionHandler({ImportApiKeyException.class})
public ResponseEntity<Object> handleImportApiKeyException(ImportApiKeyException ex) {
  ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
  return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
}

@ExceptionHandler({LdmlTransformationException.class})
public ResponseEntity<Object> handleLdmlTransformationException(LdmlTransformationException ex) {
  ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
  return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
}
```

### Proposed state

Common interface that every domain exception implements:
```java
interface NeuRisException() {
    public String getExceptionName();
    public Map<String, String> getExceptionDetails()
    public Doble getVersion();
}
```

The above interface would ensure we could write handlers in the following way:

```java
@ExceptionHandler({
    ImportApiKeyException.class,
    LdmlTransformationException.class
})
public ResponseEntity<ErrorDTO> handleAsBadRequest(NeuRisException ex) {
  return ResponseEntity
      .badRequest()
      .body(Mapper.mapToErrorDto(ex))
      .build();
}
```

This way every system interacting with ours can know that we always deliver errors in the same format, and they don't need to introduce more complexity than necessary.

### Suggestions for refactoring

Due to tech debt always being a tough topic to negotiate time for in almost every project this approach requires somewhat more effort in the beginning, but still a relatively small effort, by implementing:
- interface
- error DTO
- mapper for the above

And alongside this, if project time and schedule allows, we can refactor only one of the domain exceptions to adhere to the new style and also write a handler with the correct naming for this refactored exception.
Then everyone can at any later stage, whenever they have time or the task they work on is touching the domain exception that is not refactored can include in their work a refactor.
This way refactoring can really be done in small incremental steps.

## Decision

<The change that we're proposing or have agreed to implement.>

## Consequences

- our Frontend will need to adapt and possibly recognise the format of the error and then handle it accordingly since we will still have two possible return formats.
- improved readability
- faster implementation of a new domain exception
- developer doesn't need to think much about what data to provide as interface is suggesting that
- potentially faster decision on how to handle the exception
