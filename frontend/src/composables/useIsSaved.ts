import { computed } from "vue"

export function useIsSaved<T extends { id?: unknown }>(
  modelValue: T | undefined,
  modelValueList: T[] | undefined,
) {
  const isSaved = computed(() => {
    return (
      Array.isArray(modelValueList) &&
      modelValueList.length > 0 &&
      modelValueList.some((item) => modelValue && modelValue.id === item.id)
    )
  })

  return { isSaved }
}
