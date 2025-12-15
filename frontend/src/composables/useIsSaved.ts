import { computed } from "vue"

export function useIsSaved<T extends { localId?: string }>(
  modelValue: T | undefined,
  modelValueList: T[] | undefined,
) {
  const isSaved = computed(() => {
    if (!modelValue?.localId) return false
    if (!Array.isArray(modelValueList)) return false

    return modelValueList.some((item) => item.localId === modelValue.localId)
  })

  return { isSaved }
}
