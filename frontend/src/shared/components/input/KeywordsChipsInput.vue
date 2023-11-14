<script lang="ts" setup>
import { produce } from "immer"
import { ref, watch } from "vue"
import { ResponseError } from "@/services/httpClient"
import ChipsList from "@/shared/components/input/ChipsList.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel?: string
  error?: ResponseError
  validationError?: ValidationError
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  chipAdded: [value: string]
  chipDeleted: [value: string]
}>()

/* -------------------------------------------------- *
 * Data handling                                      *
 * -------------------------------------------------- */

const newChipText = ref<string>("")

const errorMessage = ref<ResponseError | undefined>(props.error)

watch(
  () => props.error,
  (error) => {
    errorMessage.value = error
  },
)

const handleOnBlur = () => {
  errorMessage.value = undefined
}

watch(newChipText, (is) => {
  if (errorMessage.value && is !== "") errorMessage.value = undefined
})

function addChip() {
  const chip = newChipText.value.trim()
  if (!chip) return

  if (props.modelValue?.includes(chip)) {
    errorMessage.value = { title: "Schlagwort bereits vergeben." }
    newChipText.value = ""
    return
  }

  const next = props.modelValue
    ? produce(props.modelValue, (draft) => {
        draft?.push(chip)
      })
    : [chip]

  emit("chipAdded", chip)
  emit("update:modelValue", next)

  newChipText.value = ""
}

/* -------------------------------------------------- *
 * Focus management                                   *
 * -------------------------------------------------- */

const chipsInput = ref<InstanceType<typeof TextInput>>()

const focusedChip = ref<number | undefined>(undefined)

function maybeFocusFirst() {
  const inputEl = chipsInput.value?.inputRef

  if (inputEl?.selectionStart === newChipText.value.length) {
    focusedChip.value = props.modelValue !== undefined ? 0 : undefined
  }
}

const focusInput = () => {
  focusedChip.value = undefined
  chipsInput.value?.focusInput()
}
</script>

<template>
  <div>
    <TextInput
      :id="id"
      ref="chipsInput"
      v-model="newChipText"
      :aria-label="ariaLabel ?? ''"
      class="mb-8"
      @blur="handleOnBlur"
      @keydown.enter.stop="addChip"
      @keydown.right.stop="maybeFocusFirst"
    />

    <div v-if="errorMessage" class="flex flex-row items-center">
      <IconErrorOutline class="leading-default text-gray-900" />
      <p class="ds-label-02-reg m-4 text-gray-900">{{ errorMessage?.title }}</p>
    </div>

    <ChipsList
      v-model:focused-item="focusedChip"
      :model-value="modelValue"
      @chip-deleted="(_, keyword) => $emit('chipDeleted', keyword)"
      @previous-clicked-on-first="focusInput"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>
