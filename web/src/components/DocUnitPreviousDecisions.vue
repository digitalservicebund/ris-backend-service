<template>
  <ExpandableContent>
    <template #header>
      <h2>Vorgehende Entscheidungen</h2>
    </template>

    <div class="previous-decisions">
      <ModelComponentRepeater
        v-model="values"
        :component="InputGroup"
        :default-value="defaultModel"
        :fields="previousDecisionFields"
        :column-count="2"
      >
        <template #removeButton="{ onClick }">
          <TextButton
            label="Entfernen"
            aria-label="Entscheidung Entfernen"
            button-type="ghost"
            class="previous-decisions__remove-button"
            @click="onClick"
          />
        </template>

        <template #addButton="{ onClick }">
          <TextButton
            label="weitere Entscheidung hinzufügen"
            aria-label="weitere Entscheidung hinzufügen"
            class="previous-decisions__add-button"
            @click="onClick"
          />
        </template>
      </ModelComponentRepeater>
    </div>
  </ExpandableContent>
</template>

<script lang="ts" setup>
import { computed } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import ModelComponentRepeater from "@/components/ModelComponentRepeater.vue"
import TextButton from "@/components/TextButton.vue"
import { previousDecisionFields } from "@/domain"
import type { PreviousDecision } from "@/domain/docUnit"

const defaultModel: PreviousDecision = {
  courtType: "",
  courtPlace: "",
  date: "",
  docketNumber: "",
}

interface Props {
  modelValue?: PreviousDecision[]
}

interface Emits {
  (event: "update:modelValue", value: PreviousDecision[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

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

<style lang="scss" scoped>
.previous-decisions {
  /* FIXME */
  padding: 3rem 1rem;

  &__add-button {
    margin-top: 3rem;
  }

  &__remove-button {
    margin: 1rem 0 2rem 0;
  }
}
</style>
