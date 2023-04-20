<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const entity = computed({
  get: () => inputValue.value.ENTITY?.[0],
  set: (value) => value && (inputValue.value.ENTITY = [value]),
})
</script>
<template>
  <InputField
    id="otherOfficialAnnouncement"
    aria-label="Sonstige amtliche Fundstelle"
    label="Sonstige amtliche Fundstelle"
  >
    <textarea
      id="otherOfficialAnnouncement"
      v-model="entity"
      aria-label="Sonstige amtliche Fundstelle"
      class="mt-4 outline outline-2 outline-blue-900 overflow-y-auto"
      rows="4"
    />
  </InputField>
</template>
