import { ref } from "vue"
import { ValidationError } from "@/shared/components/input/types"

type ValidationStore<T> = {
  getByField: (field: T) => ValidationError | undefined
  getByMessage: (message: string) => ValidationError[]
  add: (message: string, field: T) => void
  remove: (field: T) => void
  reset: () => void
}

export function useValidationStore<T extends string>(): ValidationStore<T> {
  const validationErrors = ref<ValidationError[]>([])

  function add(message: string, field: T) {
    remove(field)
    validationErrors.value?.push({ defaultMessage: message, field })
  }

  function getByField(field: T): ValidationError | undefined {
    return validationErrors.value.find((error) => error.field == field)
  }

  function getByMessage(message: string): ValidationError[] {
    return validationErrors.value.filter(
      (error) => error.defaultMessage == message,
    )
  }

  function remove(field: T) {
    if (getByField(field))
      validationErrors.value.splice(
        validationErrors.value.findIndex((error) => error.field == field),
        1,
      )
  }

  function reset() {
    validationErrors.value = []
  }

  return {
    getByField,
    getByMessage,
    add,
    remove,
    reset,
  }
}
