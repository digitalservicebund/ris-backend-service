<script lang="ts" setup>
import { ref, watch } from "vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
}

interface Emits {
  (event: "update:modelValue", value?: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref(props.modelValue)
const YearPlaceHolder = "JJJJ"

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true }
)

const handlePaste = async (event: ClipboardEvent) => {
  const clipboardData = event.clipboardData
  if (clipboardData !== null) {
    const pastedText = clipboardData.getData("text/plain")
    if (/^\d+$/.test(pastedText.substring(0, 3))) {
      return
    } else {
      event.preventDefault()
    }
  }
}
function onlyAllowNumbers(event: KeyboardEvent) {
  const isNumber = /^\d+$/.test(event.key)
  const isControlKey = [
    "Backspace",
    "Delete",
    "ArrowLeft",
    "ArrowRight",
  ].includes(event.key)
  if (!isNumber && !isControlKey) {
    event.preventDefault()
  }
}
</script>

<template>
  <TextInput
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    maxlength="4"
    :placeholder="YearPlaceHolder"
    @keypress="onlyAllowNumbers"
    @paste="handlePaste"
  />
</template>
