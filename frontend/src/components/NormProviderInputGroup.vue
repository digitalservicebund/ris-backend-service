<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

const entity = computed({
  get: () => inputValue.value.ENTITY?.[0],
  set: (data?: string) => data && (inputValue.value.ENTITY = [data]),
})

const decidingBody = computed({
  get: () => inputValue.value.DECIDING_BODY?.[0],
  set: (data?: string) => data && (inputValue.value.DECIDING_BODY = [data]),
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
  <div class="flex flex-col">
    <div class="mb-12">
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
    <div class="mb-24 mt-12">
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
        />
      </InputField>
    </div>
  </div>
</template>
