import { ref, readonly } from "vue"
import { ValidationError } from "@/shared/components/input/types"

export function useValidations<T extends string>() {
  const validationErrors = ref<ValidationError[]>([])

  function addValidationError(message: string, field: T) {
    if (!errorExists(message, field))
      validationErrors.value?.push({ defaultMessage: message, field })
  }

  function removeValidationError(message: string, field: T) {
    if (errorExists(message, field))
      validationErrors.value.splice(
        validationErrors.value.findIndex(
          (error) => error.defaultMessage == message && error.field == field,
        ),
        1,
      )
  }

  function getValidationErrors(field: T): ValidationError | undefined {
    return validationErrors.value.find((error) => error.field == field)
  }

  function errorExists(message: string, field: T): boolean {
    return validationErrors.value?.some(
      (error) => error.defaultMessage == message && error.field == field,
    )
  }

  function resetValidations() {
    validationErrors.value = []
  }

  return {
    validationErrors: readonly(validationErrors),
    addValidationError,
    removeValidationError,
    getValidationErrors,
    resetValidations,
  }
}
