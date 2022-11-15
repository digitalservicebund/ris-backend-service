import { ref, watch } from "vue"

interface InputModelProps<T> {
  readonly value?: T
  readonly modelValue?: T
}

interface InputModelEmits<T> {
  (event: "update:modelValue", value: T | undefined): void
  (event: "input", value: Event): void
}

export function useInputModel<
  T,
  P extends InputModelProps<T>,
  E extends InputModelEmits<T>
>(props: P, emit: E) {
  const inputValue = ref<T>()

  watch(props, () => (inputValue.value = props.modelValue ?? props.value), {
    immediate: true,
  })

  watch(inputValue, () => {
    emit(
      "update:modelValue",
      inputValue.value === "" ? undefined : inputValue.value
    )
  })

  function emitInputEvent(event: Event): void {
    emit("input", event)
  }

  return { inputValue, emitInputEvent }
}
