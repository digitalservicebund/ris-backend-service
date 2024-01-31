<script lang="ts" setup>
import { computed, onMounted, ref, toRefs } from "vue"
import { CoreData } from "../domain/documentUnit"
import InputGroup from "../shared/components/input/InputGroup.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { courtFields, coreDataFields } from "@/fields/caselaw"
import ComboboxItemService from "@/services/comboboxItemService"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import { ValidationError } from "@/shared/components/input/types"
import NestedComponent from "@/shared/components/NestedComponents.vue"
import { useTransformNestedData } from "@/shared/composables/useTransformNestedData"

interface Props {
  modelValue: CoreData
  validationErrors?: ValidationError[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  updateDocumentUnit: [void]
  "update:modelValue": [value: CoreData]
}>()

const { modelValue } = toRefs(props)

const values = useTransformNestedData(modelValue, coreDataFields, emit)
const courtValues = useTransformNestedData(modelValue, courtFields, emit)

const containerWidth = ref()
const columnCount = computed(() => (containerWidth.value < 600 ? 1 : 2))

onMounted(() => {
  const editorContainer = document.querySelector(".core-data")
  if (editorContainer != null) resizeObserver.observe(editorContainer)
})

const resizeObserver = new ResizeObserver((entries) => {
  for (const entry of entries) {
    containerWidth.value = entry.contentRect.width
  }
})
</script>

<template>
  <div class="core-data flex flex-col gap-24 bg-white p-32">
    <h2 class="ds-heading-03-bold">Stammdaten</h2>
    <NestedComponent aria-label="Fehlerhaftes Gericht" class="w-full">
      <InputField id="court" v-slot="slotProps" label="Gericht *">
        <ComboboxInput
          id="court"
          v-model="modelValue.court"
          aria-label="Gericht"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getCourts"
        ></ComboboxInput>
      </InputField>
      <!-- Child  -->
      <template #children
        ><InputField id="deviatingCourt" label="Fehlerhaftes Gericht">
          <ChipsInput
            id="deviatingCourt"
            v-model="modelValue.deviatingCourts"
            aria-label="Fehlerhaftes Gericht"
          ></ChipsInput>
        </InputField>
      </template>
    </NestedComponent>

    <div class="flex flex-row gap-24">
      <NestedComponent aria-label="Fehlerhaftes Gericht" class="w-full">
        <InputField id="fileNumber" label="Aktenzeichen *">
          <ChipsInput
            id="fileNumber"
            v-model="modelValue.fileNumbers"
            aria-label="Aktenzeichen"
          ></ChipsInput>
        </InputField>
        <!-- Child  -->
        <template #children
          ><InputField
            id="deviatingFileNumber"
            label="Abweichendes Aktenzeichen"
          >
            <ChipsInput
              id="deviatingFileNumber"
              v-model="modelValue.deviatingFileNumbers"
              aria-label="Abweichendes Aktenzeichen"
            ></ChipsInput>
          </InputField>
        </template>
      </NestedComponent>
      <NestedComponent
        aria-label="Abweichendes Entscheidungsdatum"
        class="w-full"
      >
        <InputField id="decisionDate" label="Entscheidungsdatum">
          <DateInput
            id="decisionDate"
            v-model="modelValue.decisionDate"
            aria-label="Entscheidungsdatum"
            class="ds-input-medium"
          ></DateInput>
        </InputField>
        <!-- Child  -->
        <template #children
          ><InputField
            id="deviatingDecisionDates"
            label="Abweichendes Entscheidungsdatum"
          >
            <ChipsInput
              id="deviatingDecisionDates"
              v-model="modelValue.deviatingDecisionDates"
              aria-label="Abweichendes Entscheidungsdatum"
            ></ChipsInput>
          </InputField>
        </template>
      </NestedComponent>
    </div>

    <InputGroup
      v-model="courtValues"
      :column-count="1"
      :fields="courtFields"
      :validation-errors="props.validationErrors"
    />
    <InputGroup
      v-model="values"
      :column-count="columnCount"
      :fields="coreDataFields"
      :validation-errors="props.validationErrors"
    />
    <div class="mt-4">* Pflichtfelder zum Veröffentlichen</div>
  </div>
</template>
