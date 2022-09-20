<script lang="ts" setup>
import { computed } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import ModelComponentRepeater from "@/components/ModelComponentRepeater.vue"
import TextButton from "@/components/TextButton.vue"
import { previousDecisionFields } from "@/domain"
import type { PreviousDecision } from "@/domain/documentUnit"

const props = defineProps<Props>()

const emit = defineEmits<Emits>()

const defaultModel: PreviousDecision = {
  courtType: "",
  courtPlace: "",
  date: "",
  fileNumber: "",
}

interface Props {
  modelValue?: PreviousDecision[]
}

interface Emits {
  (event: "update:modelValue", value: PreviousDecision[]): void
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
  <ExpandableContent class="p-16">
    <template #header>
      <h1 class="heading-03-bold">Vorgehende Entscheidungen</h1>
    </template>

    <div class="previous-decisions">
      <ModelComponentRepeater
        v-model="values"
        :column-count="2"
        :component="InputGroup"
        :default-value="defaultModel"
        :fields="previousDecisionFields"
      >
        <template #removeButton="{ onClick }">
          <TextButton
            aria-label="Entscheidung Entfernen"
            button-type="ghost"
            class="mb-32 mt-16"
            label="Entfernen"
            @click="onClick"
          />
        </template>

        <template #addButton="{ onClick }">
          <TextButton
            aria-label="weitere Entscheidung hinzufügen"
            class="mt-48"
            label="weitere Entscheidung hinzufügen"
            @click="onClick"
          />
        </template>
      </ModelComponentRepeater>
    </div>
  </ExpandableContent>
</template>
