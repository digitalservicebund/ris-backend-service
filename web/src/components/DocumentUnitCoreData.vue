<script lang="ts" setup>
import { computed } from "vue"
import { CoreData } from "../domain/documentUnit"
import InputGroup from "./InputGroup.vue"
import SaveDocumentUnitButton from "./SaveDocumentUnitButton.vue"
import { coreDataFields } from "@/domain"

interface Props {
  modelValue?: CoreData
  updateStatus: number
}

interface Emits {
  (event: "updateDocumentUnit"): void
  (event: "update:modelValue", value: CoreData): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const values = computed({
  get: () => props.modelValue ?? {},
  set: (newValues) => emit("update:modelValue", newValues),
})
</script>

<template>
  <div v-if="!modelValue">Loading...</div>

  <div v-else class="core-data">
    <h2 id="coreData">Stammdaten</h2>

    <div class="form">
      <InputGroup v-model="values" :fields="coreDataFields" :column-count="2" />

      <span class="form__require-info">
        * Pflichtfelder zum Veröffentlichen
      </span>

      <SaveDocumentUnitButton
        class="form__save-button"
        aria-label="Stammdaten Speichern Button"
        :update-status="updateStatus"
        @update-document-unit="emit('updateDocumentUnit')"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.core-data {
  padding: 3rem 1rem;
}

.form {
  padding: 1rem;

  &__save-button {
    margin-top: 2rem;
  }

  &__require-info {
    display: inline-block;
    margin: 2rem 0;
  }
}
</style>
