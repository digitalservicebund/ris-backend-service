<script lang="ts" setup>
import { computed } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import ModelComponentRepeater from "@/components/ModelComponentRepeater.vue"
import TextButton from "@/components/TextButton.vue"
import { proceedingDecisionFields } from "@/domain"
import type { ProceedingDecision } from "@/domain/documentUnit"

const props = defineProps<Props>()

const emit = defineEmits<Emits>()

const defaultModel: ProceedingDecision = {
  court: {
    type: "",
    location: "",
    label: "",
    revoked: "",
  },
  date: "",
  fileNumbers: [],
}

interface Props {
  modelValue?: ProceedingDecision[]
}

interface Emits {
  (event: "update:modelValue", value: ProceedingDecision[]): void
}

const values = computed({
  get: () => {
    if (props.modelValue?.length) {
      return props.modelValue
    } else {
      return [{ ...defaultModel }]
    }
  },
  set: (newValues) => {
    if (newValues[0] !== defaultModel) {
      emit("update:modelValue", newValues)
    }
  },
})
</script>

<template>
  <ExpandableContent>
    <template #header>
      <h1 class="heading-02-regular mb-[1rem]">Vorgehende Entscheidungen</h1>
    </template>

    <ModelComponentRepeater
      v-model="values"
      :column-count="2"
      :component="InputGroup"
      :default-value="defaultModel"
      :fields="proceedingDecisionFields"
    >
      <template #removeButton="{ onClick }">
        <TextButton
          aria-label="Entscheidung Entfernen"
          button-type="ghost"
          class="mb-44 mt-6"
          label="Entfernen"
          @click="onClick"
        />
      </template>

      <template #addButton="{ onClick }">
        <TextButton
          aria-label="weitere Entscheidung hinzufügen"
          class="mt-44"
          label="weitere Entscheidung hinzufügen"
          @click="onClick"
        />
      </template>
    </ModelComponentRepeater>
  </ExpandableContent>
</template>
