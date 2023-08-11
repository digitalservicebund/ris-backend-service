import { ref } from "vue"
import { ResponseError, ServiceResponse } from "@/services/httpClient"
import { ValidationError } from "@/shared/components/input/types"
import ERROR_MESSAGES from "@/shared/i18n/errors.json"

export function useValidateNormFrame(
  onBeforeValidation: () => void,
  validateCallback: () => Promise<ServiceResponse<ValidationError[]>>,
  onValidationDone: (errors: ValidationError[]) => void,
) {
  const validateIsInProgress = ref(false)
  const lastValidateError = ref<ResponseError | undefined>(undefined)

  async function triggerValidation() {
    if (validateIsInProgress.value) return

    lastValidateError.value = undefined
    validateIsInProgress.value = true
    try {
      onBeforeValidation()
      const response = await validateCallback()
      response.data && onValidationDone(response.data)
      lastValidateError.value = response.error
    } catch {
      lastValidateError.value = ERROR_MESSAGES.SERVER_ERROR
    } finally {
      setTimeout(() => {
        validateIsInProgress.value = false
      })
    }
  }

  return { validateIsInProgress, lastValidateError, triggerValidation }
}
