<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/TextButton.vue"
import { useInputModel } from "@/composables/useInputModel"

interface Props {
  id: string
  value?: FileList
  modelValue?: FileList
  ariaLabel: string
}

interface Emits {
  (event: "update:modelValue", value: FileList | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const fileInput = ref<HTMLInputElement>()

const { inputValue, emitInputEvent } = useInputModel<FileList, Props, Emits>(
  props,
  emit
)

function triggerFileInput() {
  fileInput.value?.click()
}
</script>

<template>
  <TextButton class="button" @click.self="triggerFileInput">
    <label :for="props.id" :aria-label="ariaLabel" class="label">
      <slot />

      <input
        :id="props.id"
        v-bind="inputValue"
        ref="fileInput"
        hidden="true"
        type="file"
        tabindex="-1"
        @change="emitInputEvent"
      />
    </label>
  </TextButton>
</template>

<style lang="scss" scoped>
.button {
  margin-top: 1rem;
}

.label {
  &:hover {
    cursor: pointer;
  }
}
</style>
