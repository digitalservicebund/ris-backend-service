<template>
  <v-btn
    :rounded="0"
    :ripple="false"
    :flat="true"
    color="blue800"
    @click.self="triggerFileInput"
  >
    <label :for="props.identifier" :aria-label="ariaLabel" class="label">
      <slot />

      <input
        :id="props.identifier"
        ref="fileInput"
        :name="name"
        hidden="true"
        type="file"
        tabindex="-1"
        @change="onFileInputChange"
      />
    </label>
  </v-btn>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue"

interface Props {
  identifier: string
  name?: string
  ariaLabel?: string
}

interface Emits {
  (event: "change", value: FileList | null | undefined): void
}

const emits = defineEmits<Emits>()
const props = defineProps<Props>()

const name = computed(() => props.name ?? props.identifier)
const ariaLabel = computed(() => props.ariaLabel ?? props.identifier)

const fileInput = ref<HTMLInputElement>()

function triggerFileInput() {
  fileInput.value?.click()
}

function onFileInputChange(): void {
  emits("change", fileInput.value?.files)
}
</script>

<style lang="scss" scoped>
.label {
  &:hover {
    cursor: pointer;
  }
}
</style>
