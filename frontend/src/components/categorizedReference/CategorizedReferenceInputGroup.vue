<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const inputValue = ref(props.modelValue)

const text = computed({
  get: () => inputValue.value.TEXT?.[0],
  set: (data?: string) => (inputValue.value.TEXT = data ? [data] : undefined),
})

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})
</script>

<template>
  <div class="flex flex-col">
    <div>
      <InputField
        id="categorizedReferenceText"
        aria-label="Aktivverweisung"
        class="w-1/2"
        label="Aktivverweisung"
      >
        <TextInput
          id="categorizedReferenceText"
          v-model="text"
          aria-label="Aktivverweisung"
        />
      </InputField>
    </div>
  </div>
</template>
