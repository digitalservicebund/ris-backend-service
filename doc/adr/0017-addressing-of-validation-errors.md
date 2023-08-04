# 17. Addressing of Validation Errors

Date: 2023-08-03

## Status

Accepted

## Context

For a good user experience it is important to show validation errors where they
occur and allow to address them directly. This needs a data structure that
provides such information. This is especially important as it is necessary to
validate large data structures altogether to establish cross-dependency
validations. This means a validation for single field is not always possible or
feasible without the context. Independent at which data structure level something
gets validated, it is necessary to associate errors with the related field.
Although it seems like a single field of a data structure could be validated on
its own, there can be errors caused by its context. Thereby it doesn't matter if
a service validates the complete data structure or a single field of it. It must
either know for which field to return errors or it must associate each error of
the whole structure with a field.

## Decision

While [ADR 0014](./0014-error-handling-concept.md) proposed the name `attribute`
for this property, we follow now the IETF RFC 7807 ("Problem Details for HTTP
APIs") and use the naming `instance`. The value of this property MUST be an URI.
The exact chosen schema is not part of this ADR and up to the separate domains.
The option of an URL SHOULD be chosen over an URN as it proves scoping
mechanisms. Any chosen option should be unique across domains.
The property SHOULD be required for an error response object that represent
a validation error to avoid and implicit interpretation for the addressing.

### Example

Example error response object based on [ADR 0014](./0014-error-handling-concept.md) plus
this ADR. The exact URI schemas are just early ideas.

```
[
  {
    "code": "ERROR_CODE_FOO",
    "message": "something is not foo",
    "instance":
"norm/8a4ebde6-0a52-40e9-a491-2971314cfccd/metadata/3d2ad4b5-71c6-47ca-a3ab-a4f8af946cb4"
  },
  {
    "code": "ERROR_CODE_BAR",
    "message": "required bar is missing",
    "instance": "documentunit/XXRE202322913"
  },
  {
    "code": "ERROR_CODE_BAZ",
    "message": "incorrect baz format",
    "instance": "eli/bgbl-1/2023/s3/para-2a_abs-1_inhalt-2"
  }
]
```

## Consequences

- Validation error objects can be safely linked to data fields and used by the
  user facing application (frontend).

- Each domain needs to develop is URI schema. The schema must be owed by the
  backend service, but understood by the frontend. The backend needs to (be able
  to) provide the `instance` property for validation errors.

- The identification on a global level including scoping allows for
  simplifications yet powerful operations in the usage across domains.
