import errors from "./errors.json"

/**
 * Typeguard that validates if a given string points to an entry in the list
 * of error codes.
 */
export function isErrorCode(maybe: string): maybe is keyof typeof errors {
  return maybe in errors
}
