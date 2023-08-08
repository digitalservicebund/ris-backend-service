import { defineStore } from "pinia"
import { computed, ref } from "vue"
import { ValidationError } from "@/shared/components/input/types"

export const INSTANCE_ID_SCOPE_SEPARATOR = "/"

export const useGlobalValidationErrorStore = defineStore(
  "validation-errors",
  () => {
    const validationErrors = ref<ValidationError[]>([])

    function getAll() {
      return computed(() => validationErrors.value)
    }

    function add(...newValidationErrors: ValidationError[]) {
      validationErrors.value = [
        ...validationErrors.value,
        ...newValidationErrors,
      ]
    }

    function getByInstance(instance: string) {
      return computed(() =>
        validationErrors.value.filter((error) => error.instance === instance),
      )
    }

    function children(instance: string) {
      return computed(() =>
        validationErrors.value.filter((error) =>
          isChild(error.instance, instance),
        ),
      )
    }

    function getByScope(scope: string) {
      return computed(() =>
        validationErrors.value.filter((error) =>
          isInScope(error.instance, scope),
        ),
      )
    }

    function removeByScope(scope: string) {
      validationErrors.value = validationErrors.value.filter(
        (error) => !isInScope(error.instance, scope),
      )
    }

    function reset() {
      validationErrors.value = []
    }

    function isChild(child: string, parent: string) {
      return child.startsWith(parent + INSTANCE_ID_SCOPE_SEPARATOR)
    }

    function isInScope(instance: string, scope: string) {
      return instance === scope || isChild(instance, scope)
    }

    return {
      getAll,
      add,
      getByInstance,
      children,
      getByScope,
      removeByScope,
      reset,
    }
  },
)
