# 18. Frontend Validation Error Storage

Date: 2023-08-04

## Status

Accepted

## Context

[ADR 0014](./0014-error-handling-concept.md) and [ADR 0017](./0017-addressing-of-validation-errors.md)
define the format of how validation errors are send to the frontend and how to
address them. The data must now somehow be made available in the frontend to all
the components that need to access it.

## Decision

A new state manager/store will be added to the frontend to mange the violation
errors. It is based on the concept of the global unique `instance` property and
it scoping mechanisms to access the data.

The already in use library for such stores is `pinia`. A first version of such
a store looks like that:

```typescript
// Imports omitted...

export const useValidationErrorStore = defineStore("validation-errors", () => {
  const validationErrors = ref<ValidationError[]>([] as ValidationError[]);

  function add(newValidationErrors: ValidationError[]) {
    validationErrors.value = [
      ...validationErrors.value,
      ...newValidationErrors,
    ];
  }

  const get(instance: Pick<ValidationError, "instance">) {
    return computed(() =>
        validationErrors.filter((error) => error.instance.startsWith(instance))
    )
  }

 function remove(instance: Pick<ValidationError, "instance">) {
    validationErrors.value = validationErrors.value.filter(
        (error) => !error.instance.startsWith(instance)
    )
 }

  return { add, get, remove };
});
```

The store does not take care of mechanisms like purging old validation errors of
a scope first before doing a new validation. This is the responsibility of the
store user.

## Consequences

- The store needs to be implemented for the frontend.
- Validation errors should only be managed by this store to avoid spreading.
