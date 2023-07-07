package de.bund.digitalservice.ris.norms.exceptions.response
enum class ErrorCode(val message: String) {
    PARSE_ERROR("Parse error"),
    AUTHENTICATION_FAILED("Authentication failed"),
    NOT_AUTHENTICATED("not authenticated"),
    SERVER_ERROR("server error"),
    VALIDATION_ERROR(""),
    NOT_FOUND("not found"),
}
