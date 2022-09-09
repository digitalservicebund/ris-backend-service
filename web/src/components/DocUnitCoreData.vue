<script lang="ts" setup>
import { computed } from "vue"
import { CoreData } from "../domain/docUnit"
import * as iconsAndLabels from "../iconsAndLabels.json"
import InputGroup from "./InputGroup.vue"
import SaveDocUnitButton from "./SaveDocUnitButton.vue"

interface Props {
  modelValue?: CoreData
  updateStatus: number
}

interface Emits {
  (event: "updateDocUnit"): void
  (event: "update:modelValue", value: CoreData): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// TODO: Remove this once `iconsAndLabels` has been adapted everywhere.
const fields = computed(() =>
  iconsAndLabels.coreData.map((item) => ({
    id: item.name,
    label: item.label,
    ariaLabel: item.label,
    iconName: item.icon,
    requiredText: item.requiredText,
  }))
)

const values = computed({
  get() {
    return props.modelValue ?? {}
  },
  set(newValues: CoreData) {
    emit("update:modelValue", newValues)
  },
})
</script>

<template>
  <div v-if="!modelValue">Loading...</div>

  <div v-else>
    <h2 id="coreData">Stammdaten</h2>

    <div class="form">
      <InputGroup v-model="values" :fields="fields" :column-count="2" />
      <span class="form__required-field-infos"
        >* Pflichtfelder zum Veröffentlichen</span
      >
      <SaveDocUnitButton
        class="form__save-button"
        aria-label="Stammdaten Speichern Button"
        :update-status="updateStatus"
        @update-doc-unit="emit('updateDocUnit')"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.form {
  padding: 2rem;

  &__save-button {
    margin-top: 2rem;
  }
  &__required-field-infos {
    display: inline-block;
    margin: 2rem 0;
  }
}
</style>
