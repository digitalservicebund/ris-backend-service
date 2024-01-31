<script lang="ts" setup>
import { toRefs, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import { CoreData } from "@/domain/documentUnit"
import ComboboxItemService from "@/services/comboboxItemService"
import ChipsDateInput from "@/shared/components/input/ChipsDateInput.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import NestedComponent from "@/shared/components/NestedComponents.vue"

interface Props {
  modelValue: CoreData
}

const props = defineProps<Props>()
const emit = defineEmits<{
  "update:modelValue": [value: CoreData]
}>()
const { modelValue } = toRefs(props)

watch(
  modelValue,
  () => {
    emit("update:modelValue", modelValue.value)
  },
  { deep: true },
)
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
      <template #children>
        <InputField id="deviatingCourt" label="Fehlerhaftes Gericht">
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
        <template #children>
          <InputField
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
        <template #children>
          <InputField
            id="deviatingDecisionDates"
            label="Abweichendes Entscheidungsdatum"
          >
            <ChipsDateInput
              id="deviatingDecisionDates"
              v-model="modelValue.deviatingDecisionDates"
              aria-label="Abweichendes Entscheidungsdatum"
            />
          </InputField>
        </template>
      </NestedComponent>
    </div>
    <div class="flex flex-row gap-24">
      <InputField
        id="appraisalBody"
        v-slot="slotProps"
        class="flex-col"
        label="Spruchkörper"
      >
        <TextInput
          id="appraisalBody"
          v-model="modelValue.appraisalBody"
          aria-label="Spruchkörper"
          class="ds-input-medium"
          :has-error="slotProps.hasError"
          size="medium"
        ></TextInput>
      </InputField>

      <InputField id="documentType" class="flex-col" label="Dokumenttyp">
        <ComboboxInput
          id="documentType"
          v-model="modelValue.documentType"
          aria-label="Dokumenttyp"
          :item-service="ComboboxItemService.getDocumentTypes"
        ></ComboboxInput>
      </InputField>
    </div>

    <div class="flex flex-row gap-24">
      <NestedComponent aria-label="Fehlerhaftes Gericht" class="w-full">
        <InputField id="ecli" class="flex-col" label="ECLI">
          <TextInput
            id="ecli"
            v-model="modelValue.ecli"
            aria-label="ECLI"
            class="ds-input-medium"
            size="medium"
          ></TextInput>
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField id="deviatingEclis" label="Abweichender ECLI">
            <ChipsInput
              id="deviatingEclis"
              v-model="modelValue.deviatingEclis"
              aria-label="Abweichender ECLI"
            ></ChipsInput>
          </InputField>
        </template>
      </NestedComponent>

      <NestedComponent aria-label="Vorgangshistorie" class="w-full">
        <InputField id="procedure" class="flex-col" label="Vorgang">
          <ComboboxInput
            id="procedure"
            v-model="modelValue.procedure"
            aria-label="Vorgang"
            :item-service="ComboboxItemService.getProcedures"
          ></ComboboxInput>
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField id="previousProcedure" label="Vorgangshistorie">
            <ChipsInput
              id="previousProcedure"
              v-model="modelValue.previousProcedure"
              aria-label="Vorgangshistorie"
              read-only
            ></ChipsInput>
          </InputField>
        </template>
      </NestedComponent>
    </div>

    <div class="flex flex-row gap-24">
      <InputField id="emptyDropdown" v-slot="{ id }" label="Rechtskraft *">
        <DropdownInput
          :id="id"
          v-model="modelValue.legalEffect"
          aria-label="dropdown input"
          :items="legalEffectTypes.items"
          placeholder="Bitte auswählen"
        />
      </InputField>

      <InputField id="region" class="flex-col" label="Region">
        <TextInput
          id="region"
          v-model="modelValue.region"
          aria-label="Region"
          class="ds-input-medium"
          read-only
          size="medium"
        ></TextInput>
      </InputField>
    </div>

    <div class="mt-4">* Pflichtfelder zum Veröffentlichen</div>
  </div>
</template>
