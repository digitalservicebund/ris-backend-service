<script lang="ts" setup>
import { produce } from "immer"
import { ref, watch } from "vue"
import ChipsList from "@/shared/components/input/ChipsList.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import InputErrorMessages from "@/shared/components/InputErrorMessages.vue"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
}>()

/* -------------------------------------------------- *
 * Data handling                                      *
 * -------------------------------------------------- */

const newChipText = ref<string>("")

const errorMessage = ref()

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
        draft.push(chip)
      })
    : [chip]

  emit("update:modelValue", next)
  newChipText.value = ""
}

function onDeleteChip() {
  focusInputIfEmpty()
  if (props.modelValue?.length === 1) emit("update:modelValue", [])
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

async function focusInputIfEmpty() {
  if (props.modelValue?.length === 1) focusInput()
}

const handleOnBlur = () => {
  errorMessage.value = undefined
}

watch(newChipText, (is) => {
  if (errorMessage.value && is !== "") errorMessage.value = undefined
})
</script>

<template>
  <div>
    <TextInput
      :id="id"
      ref="chipsInput"
      v-model="newChipText"
      :aria-label="ariaLabel ?? ''"
      class="mb-8"
      size="medium"
      @blur="handleOnBlur"
      @keydown.enter.stop="addChip"
      @keydown.right.stop="maybeFocusFirst"
    />

    <InputErrorMessages
      :error-message="errorMessage?.title"
    ></InputErrorMessages>

    <ChipsList
      v-model:focused-item="focusedChip"
      :model-value="modelValue"
      @chip-deleted="onDeleteChip"
      @previous-clicked-on-first="focusInput"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>
