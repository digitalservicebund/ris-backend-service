<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const inputValue = ref(props.modelValue)

const entity = computed({
  get: () => inputValue.value.ENTITY?.[0],
  set: (data?: string) => (inputValue.value.ENTITY = data ? [data] : undefined),
})

const decidingBody = computed({
  get: () => inputValue.value.DECIDING_BODY?.[0],
  set: (data?: string) =>
    (inputValue.value.DECIDING_BODY = data ? [data] : undefined),
})

const isResolutionMajority = computed({
  get: () => inputValue.value.RESOLUTION_MAJORITY?.[0],
  set: (data?: boolean) =>
    data !== undefined && (inputValue.value.RESOLUTION_MAJORITY = [data]),
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
  <div class="flex flex-col gap-8">
    <div class="">
      <InputField
        id="normProviderEntity"
        aria-label="Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt "
        class="w-1/2"
        :label="[
          'Staat, Land, Stadt, Landkreis oder juristische Person,',
          'deren Hoheitsgewalt oder Rechtsmacht die Norm trägt',
        ]"
      >
        <TextInput
          id="normProviderEntity"
          v-model="entity"
          aria-label="Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt "
        />
      </InputField>
    </div>
    <div>
      <InputField
        id="normProviderDecidingBody"
        aria-label="Beschließendes Organ"
        class="w-1/2"
        label="Beschließendes Organ"
      >
        <TextInput
          id="normProviderDecidingBody"
          v-model="decidingBody"
          aria-label="Beschließendes Organ"
        />
      </InputField>
    </div>
    <div>
      <InputField
        id="normProviderIsResolutionMajority"
        aria-label="Beschlussfassung mit qualifizierter Mehrheit"
        label="Beschlussfassung mit qualifizierter Mehrheit"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          id="normProviderIsResolutionMajority"
          v-model="isResolutionMajority"
          aria-label="Beschlussfassung mit qualifizierter Mehrheit"
          size="small"
        />
      </InputField>
    </div>
  </div>
</template>
