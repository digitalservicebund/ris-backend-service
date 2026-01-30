<script lang="ts" setup>
import { ref } from "vue"
import { useInputModel } from "@/composables/useInputModel"

interface Props {
  id: string
  value?: FileList
  modelValue?: FileList
  ariaLabel: string
  accept?: string
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
  emit,
)

function onEnter() {
  fileInput.value?.click()
}
</script>

<template>
  <a href="javascript:void(0)" @keydown.enter="onEnter">
    <label :aria-label="ariaLabel" class="label" :for="id">
      <slot />

      <input
        :id="id"
        v-bind="inputValue"
        ref="fileInput"
        :accept="accept"
        class=""
        hidden="true"
        multiple="true"
        tabindex="-1"
        type="file"
        @change="emitInputEvent"
      />
    </label>
  </a>
</template>

<style scoped>
.button {
  margin-top: 1rem;
}

.label {
  display: flex;
  flex-flow: row nowrap;
  align-items: center;
  justify-content: center;

  &:hover {
    cursor: pointer;
  }
}
</style>
