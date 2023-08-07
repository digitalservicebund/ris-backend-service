import { defineStore } from "pinia"
import { computed, ref } from "vue"

export type InstanceId = string

export type GlobalValidationError = {
  code: string
  message: string
  instance: InstanceId
}

export const useGlobalValidationErrorStore = defineStore(
  "validation-errors",
  () => {
    const validationErrors = ref<GlobalValidationError[]>([])

    function add(...newValidationErrors: GlobalValidationError[]) {
      validationErrors.value = [
        ...validationErrors.value,
        ...newValidationErrors,
      ]
    }

    function getByInstance(instance: InstanceId) {
      return computed(() =>
        validationErrors.value.filter((error) => error.instance === instance),
      )
    }

    function children(instance: InstanceId) {
      return computed(() =>
        validationErrors.value.filter((error) =>
          error.instance.startsWith(instance + "/"),
        ),
      )
    }

    function getByScope(instance: InstanceId) {
      return computed(() => [
        ...getByInstance(instance).value,
        ...children(instance).value,
      ])
    }

    function removeByScope(instance: InstanceId) {
      validationErrors.value = validationErrors.value.filter(
        (error) => !error.instance.startsWith(instance),
      )
    }

    function reset() {
      validationErrors.value = []
    }

    return {
      validationErrors,
      add,
      getByInstance,
      children,
      getByScope,
      removeByScope,
      reset,
    }
  },
)
