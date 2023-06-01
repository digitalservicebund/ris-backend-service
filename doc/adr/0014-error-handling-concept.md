# 14. Concept for handling REST API errors

Date: 2023-05-11

## Status

Accepted

## Context

We want our REST API to properly handle errors and provide meaningful error codes and messages, as this helps
our Frontend to respond to issues correctly. When designing the error handling for our REST API we want to make sure to
follow standards and best practices.

## Decision

We decided for the following:

* For each error the API will return an error code and an optional human-readable message in english that is meant to
  help developers.
* Error codes are strings and not numbers in order to give them some meaning and readability.
* The creation of human-readable message (and their translations) for users has to be done by the frontend.
* The status code for validation errors is 422.
* The reponse will structured like this:

```
header: <status>
body: {

    "errors": [

        {"code": <code>, "message": <message>, "attribute": <attribute>}

    ]

}
```
### Examples
Be aware: The error codes in the following examples still need to be defined and are not final.
#### 404 error - Response:
```
header - status: 404

body:  {

    "errors": [
        {"code": "not_found", "message": "The requested data could no be found", "attribute": ""}
    ]

}
```
#### 422 error - Response (Validation error):
```
header - status: 422
body:  {
    "errors": [
        {"code": "required", "message":"", "attribute": "username" },
        {"code": "valid_email_required", "message":"", "attribute": "email"},
        {"code": "end_date_before_start_date", "message":"", "attribute": "non_field_error" }
    ]
}
```

## Consequences
* We need to define error codes and messages for all status codes and validation errors in the backend
* We need to add a translation json and maybe a i18n-library to our frontend code that maps error codes to translated strings.
* We need to adapt the backend API code to return the correct response.
* We need to adapt the frontend to deal with the new API response.

## References
* [Best Practices for REST API Error Handling | Baeldung](https://www.baeldung.com/rest-api-error-handling-best-practices)
* [ControllerAdvice (Spring Framework 6.0.9 API)](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html)
* [Spring Web and WebFlux exception handling best practices](https://medium.com/codex/spring-web-and-webflux-exception-handling-best-practices-b2c3cd7e3acf)
* [Complete Guide to Exception Handling in Spring Boot](https://reflectoring.io/spring-boot-exception-handling/)
