<script lang="ts" setup>
import { computed, ref, watch, watchEffect } from "vue"
import { NormReference } from "@/domain/normReference"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const props = defineProps<{ modelValue: NormReference }>()
const emit = defineEmits<{
  (e: "update:modelValue", value: NormReference): void
}>()

const abbreviationInEditMode = ref(true)
const abbreviationInput = ref<typeof TextInput>()
let firstCheck = true

const norm = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit("update:modelValue", value)
  },
})

watch(
  props.modelValue,
  () => {
    if (!firstCheck) {
      return
    }

    firstCheck = false
    abbreviationInEditMode.value = props.modelValue.abbreviation ? false : true
  },
  { immediate: true }
)
watchEffect(() => {
  if (abbreviationInput.value) {
    abbreviationInput.value.focusInput()
  }
})

function editAbbreviation() {
  abbreviationInEditMode.value = true
}

function handleBlur() {
  abbreviationInEditMode.value = false
}
</script>

<template>
  <div class="m-24">
    <InputField id="norm-reference-abbreviation-field" label="RIS-Abkürzung">
      <TextInput
        v-if="abbreviationInEditMode"
        id="norm-reference-abbreviation"
        ref="abbreviationInput"
        v-model="norm.abbreviation"
        aria-label="RIS-Abkürzung"
        @blur="handleBlur"
      ></TextInput>
      <span
        v-if="!abbreviationInEditMode"
        id="norm-reference-abbreviation"
        aria-label="RIS-Abkürzung"
        tabindex="0"
        @click="editAbbreviation"
        @keydown.enter="editAbbreviation"
        >{{ norm.abbreviation }}</span
      >
    </InputField>
    <InputField id="norm-reference-abbreviation-field" label="Einzelnorm">
      <TextInput
        id="norm-reference-singleNorm"
        v-model="norm.singleNorm"
        aria-label="Einzelnorm"
      ></TextInput>
    </InputField>
    <InputField id="norm-reference-abbreviation-field" label="Jahr">
      <TextInput
        id="norm-reference-year"
        v-model="norm.year"
        aria-label="Jahr"
      ></TextInput>
    </InputField>
    <InputField id="norm-reference-abbreviation-field" label="Fassungsdatum">
      <TextInput
        id="norm-reference-versionDate"
        v-model="norm.versionDate"
        aria-label="Fassungsdatum"
      ></TextInput>
    </InputField>
  </div>
</template>

<style scoped>
span:focus {
  outline: auto;
}
</style>
