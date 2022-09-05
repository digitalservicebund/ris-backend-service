<script lang="ts" setup>
import { computed } from "vue"
import { CoreData } from "../domain/docUnit"
import * as iconsAndLabels from "../iconsAndLabels.json"
import InputField from "./InputField.vue"
import SaveDocUnitButton from "./SaveDocUnitButton.vue"
import TextInput from "./TextInput.vue"

const props = defineProps<{ coreData: CoreData; updateStatus: number }>()
const emit = defineEmits<{
  (e: "updateValue", updatedValue: [keyof CoreData, string]): void
  (e: "updateDocUnit"): void
}>()

const data = computed(() =>
  iconsAndLabels.coreData.map((item) => {
    return {
      id: item.name as keyof CoreData,
      name: item.name,
      label: item.label,
      aria: item.label,
      icon: item.icon,
      value: props.coreData[item.name as keyof CoreData],
    }
  })
)

const updateValue = (event: Event, index: number) => {
  emit("updateValue", [
    data.value[index].id,
    (event.target as HTMLInputElement).value,
  ])
}
</script>

<template>
  <div v-if="!coreData">Loading...</div>
  <div v-else>
    <form novalidate class="ris-form" @submit="emit('updateDocUnit')">
      <v-row>
        <v-col>
          <h2 id="coreData">Stammdaten</h2>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="6" class="ris-form__column">
          <template v-for="(item, index) in data">
            <InputField
              v-if="index <= 5"
              :id="item.id"
              :key="item.id"
              :label="item.label"
              :icon-name="item.icon"
            >
              <TextInput
                :id="item.id"
                :value="item.value"
                :aria-label="item.aria"
                @input="updateValue($event, index)"
              />
            </InputField>
          </template>
        </v-col>
        <v-col cols="6" class="ris-form__column">
          <template v-for="(item, index) in data">
            <InputField
              v-if="index > 5"
              :id="item.id"
              :key="item.id"
              :label="item.label"
              :icon-name="item.icon"
            >
              <TextInput
                :id="item.id"
                :value="item.value"
                :aria-label="item.aria"
                @input="updateValue($event, index)"
              />
            </InputField>
          </template>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <div class="ris-form__column">
            <SaveDocUnitButton
              aria-label="Stammdaten Speichern Button"
              :update-status="updateStatus"
              @update-doc-unit="emit('updateDocUnit')"
            />
          </div>
        </v-col>
      </v-row>
    </form>
  </div>
</template>

<style lang="scss">
.ris-form {
  padding: 2rem;

  &__column {
    display: flex;
    flex-direction: column;
    gap: 2.5rem;
    padding: 2rem;
  }
}
</style>
