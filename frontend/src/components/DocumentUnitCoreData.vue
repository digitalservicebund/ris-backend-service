<script lang="ts" setup>
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import InputMultiSelect from "primevue/multiselect"
import InputSelect from "primevue/select"
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue"
import { DropdownItem } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CourtBranchLocation from "@/components/CourtBranchLocation.vue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import ChipsYearInput from "@/components/input/ChipsYearInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import NestedComponent from "@/components/NestedComponents.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import { CoreData, coreDataLabels } from "@/domain/coreData"
import { Kind } from "@/domain/documentationUnitKind"
import { pendingProceedingLabels } from "@/domain/pendingProceeding"
import { SourceValue } from "@/domain/source"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  kind: Kind
}>()

const isPendingProceeding = props.kind === Kind.PENDING_PROCEEDING
const coreDataModel = defineModel<CoreData>({ required: true })

watch(
  () => coreDataModel.value.resolutionDate,
  (newDate) => {
    if (newDate && !coreDataModel.value.isResolved) {
      coreDataModel.value.isResolved = true
    }
  },
  { deep: false },
)
const validationStore =
  useValidationStore<
    [
      "deviatingCourts",
      "fileNumbers",
      "deviatingFileNumbers",
      "deviatingDocumentNumbers",
      "deviatingEclis",
      "inputTypes",
      "leadingDecisionNormReferences",
      "decisionDate",
      "yearsOfDispute",
      "deviatingDecisionDates",
      "oralHearingDates",
      "sources",
    ][number]
  >()

const parentWidth = ref(0)
const parentRef = ref<HTMLElement | null>(null)
const resizeObserver: ResizeObserver | null = new ResizeObserver((entries) => {
  for (const entry of entries) {
    parentWidth.value = entry.contentRect.width
  }
})

const layoutClass = computed(() =>
  parentWidth.value < 400 ? "flex flex-col gap-24" : "flex flex-row gap-24",
)

/**
 * Our UI turns the chronological order of the list, so the latest previous procedure is first.
 */
const descendingPreviousProcedures = computed(() =>
  coreDataModel.value.previousProcedures
    ? coreDataModel.value.previousProcedures.toReversed().join(", ")
    : "",
)

const jurisdictionType = computed(() =>
  coreDataModel.value.court ? coreDataModel.value.court.jurisdictionType : "",
)

const region = computed(() =>
  coreDataModel.value.court
    ? coreDataModel.value.court.regions?.join(", ")
    : "",
)

const rawSourceItems = computed<DropdownItem[]>(
  () =>
    coreDataModel.value.sources
      ?.filter((source) => source.value == undefined)
      .map((source) => source.sourceRawValue)
      .filter((rawValue) => rawValue != undefined)
      .map((rawValue) => ({
        label: rawValue,
        value: rawValue,
      })) ?? [],
)

const sourceItems = computed<DropdownItem[]>(() => [
  {
    label: "unaufgefordert eingesandtes Original (O)",
    value: SourceValue.UnaufgefordertesOriginal,
  },
  {
    label: "angefordertes Original (A)",
    value: SourceValue.AngefordertesOriginal,
  },
  {
    label: "Zeitschriftenveröffentlichung (Z)",
    value: SourceValue.Zeitschrift,
  },
  { label: "ohne Vorlage des Originals E-Mail (E)", value: SourceValue.Email },
  {
    label:
      "Ländergerichte, EuG- und EuGH-Entscheidungen über jDV-Verfahren (L)",
    value: SourceValue.LaenderEuGH,
  },
  { label: "Sonstige (S)", value: SourceValue.Sonstige },
  ...rawSourceItems.value,
])

function isSourceValue(value: string): value is SourceValue {
  return Object.values(SourceValue).includes(value as SourceValue)
}

const sources = computed<string[]>({
  get: () =>
    coreDataModel.value.sources
      ?.map((source) => source.value ?? source.sourceRawValue)
      .filter((newValue) => newValue != undefined) ?? [],
  set: (newValues) => {
    coreDataModel.value.sources =
      newValues
        ?.filter((newValue) => newValue != undefined)
        ?.map((newValue: string) => {
          const existingSource =
            coreDataModel.value.sources?.find(
              (existingSource) => existingSource.value === newValue,
            ) ??
            coreDataModel.value.sources?.find(
              (existingSource) => existingSource.sourceRawValue === newValue,
            )

          if (existingSource) {
            return existingSource
          }

          if (!isSourceValue(newValue)) {
            return {
              sourceRawValue: newValue,
            }
          }

          return {
            value: newValue,
          }
        }) ?? []
  },
})

const deviatingCourts = computed({
  get: () => coreDataModel.value.deviatingCourts ?? [],
  set: (newValue) => {
    coreDataModel.value.deviatingCourts = newValue
  },
})
const fileNumbers = computed({
  get: () => coreDataModel.value.fileNumbers ?? [],
  set: (newValue) => {
    coreDataModel.value.fileNumbers = newValue
  },
})

const deviatingFileNumbers = computed({
  get: () => coreDataModel.value.deviatingFileNumbers ?? [],
  set: (newValue) => {
    coreDataModel.value.deviatingFileNumbers = newValue
  },
})

const deviatingDocumentNumbers = computed({
  get: () => coreDataModel.value.deviatingDocumentNumbers ?? [],
  set: (newValue) => {
    coreDataModel.value.deviatingDocumentNumbers = newValue
  },
})

const deviatingEclis = computed({
  get: () => coreDataModel.value.deviatingEclis ?? [],
  set: (newValue) => {
    coreDataModel.value.deviatingEclis = newValue
  },
})

const inputTypes = computed({
  get: () => coreDataModel.value.inputTypes ?? [],
  set: (newValue) => {
    coreDataModel.value.inputTypes = newValue
  },
})

const leadingDecisionNormReferences = computed({
  get: () => coreDataModel.value.leadingDecisionNormReferences ?? [],
  set: (newValue) => {
    coreDataModel.value.leadingDecisionNormReferences = newValue
  },
})

onMounted(() => {
  if (!parentRef.value) return
  resizeObserver.observe(parentRef.value)
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})
</script>

<template>
  <div
    ref="parentRef"
    aria-label="Formaldaten"
    class="core-data flex flex-col gap-24 bg-white p-24"
  >
    <TitleElement>Formaldaten</TitleElement>
    <NestedComponent
      aria-label="Fehlerhaftes Gericht"
      class="w-full"
      :is-open="!!coreDataModel.deviatingCourts?.length"
    >
      <InputField id="court" v-slot="slotProps" label="Gericht *">
        <ComboboxInput
          id="court"
          v-model="coreDataModel.court"
          aria-label="Gericht"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getCourts"
        ></ComboboxInput>
      </InputField>
      <!-- Child  -->
      <template #children>
        <InputField
          id="deviatingCourts"
          v-slot="slotProps"
          label="Fehlerhaftes Gericht"
        >
          <ChipsInput
            :id="slotProps.id"
            v-model="deviatingCourts"
            aria-label="Fehlerhaftes Gericht"
            data-testid="deviating-courts"
            :has-error="slotProps.hasError"
            @focus="validationStore.remove('deviatingCourts')"
            @update:validation-error="slotProps.updateValidationError"
          />
        </InputField>
      </template>
    </NestedComponent>

    <div :class="layoutClass">
      <NestedComponent
        id="fileNumbers"
        aria-label="Abweichendes Aktenzeichen"
        class="w-full min-w-0"
        :is-open="!!coreDataModel.deviatingFileNumbers?.length"
      >
        <InputField
          id="fileNumberInput"
          v-slot="slotProps"
          label="Aktenzeichen *"
        >
          <ChipsInput
            :id="slotProps.id"
            v-model="fileNumbers"
            aria-label="Aktenzeichen"
            data-testid="file-numbers"
            :has-error="slotProps.hasError"
            @focus="validationStore.remove('fileNumbers')"
            @update:validation-error="slotProps.updateValidationError"
          />
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField
            id="deviatingFileNumbers"
            v-slot="slotProps"
            label="Abweichendes Aktenzeichen"
          >
            <ChipsInput
              :id="slotProps.id"
              v-model="deviatingFileNumbers"
              aria-label="Abweichendes Aktenzeichen"
              data-testid="deviating-file-numbers"
              :has-error="slotProps.hasError"
              @focus="validationStore.remove('deviatingFileNumbers')"
              @update:validation-error="slotProps.updateValidationError"
            />
          </InputField>
        </template>
      </NestedComponent>
      <NestedComponent
        aria-label="Abweichendes Entscheidungsdatum"
        class="w-full"
        :is-open="!!coreDataModel.deviatingDecisionDates?.length"
      >
        <InputField
          id="decisionDate"
          v-slot="slotProps"
          :label="
            isPendingProceeding ? 'Mitteilungsdatum *' : 'Entscheidungsdatum *'
          "
          :validation-error="validationStore.getByField('decisionDate')"
        >
          <DateInput
            :id="slotProps.id"
            v-model="coreDataModel.decisionDate"
            aria-label="Entscheidungsdatum"
            :has-error="slotProps.hasError"
            @focus="validationStore.remove('decisionDate')"
            @update:validation-error="slotProps.updateValidationError"
          ></DateInput>
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField
            id="deviatingDecisionDates"
            v-slot="slotProps"
            label="Abweichendes Entscheidungsdatum"
            :validation-error="
              validationStore.getByField('deviatingDecisionDates')
            "
          >
            <ChipsDateInput
              :id="slotProps.id"
              v-model="coreDataModel.deviatingDecisionDates"
              aria-label="Abweichendes Entscheidungsdatum"
              data-testid="deviating-decision-dates"
              :has-error="slotProps.hasError"
              @focus="validationStore.remove('deviatingDecisionDates')"
              @update:validation-error="slotProps.updateValidationError"
            />
          </InputField>
        </template>
      </NestedComponent>
    </div>
    <div v-if="!isPendingProceeding" :class="layoutClass">
      <InputField
        id="hasDeliveryDate"
        v-slot="{ id }"
        :label="coreDataLabels.hasDeliveryDate"
        label-class="ris-label1-regular"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          v-model="coreDataModel.hasDeliveryDate"
          :aria-label="coreDataLabels.hasDeliveryDate"
          binary
          data-testid="has-delivery-date"
          :input-id="id"
          size="large"
        />
      </InputField>
      <InputField
        id="oralHearingDates"
        v-slot="slotProps"
        :label="
          coreDataLabels.oralHearingDates +
          (coreDataModel.hasDeliveryDate ? ' *' : '')
        "
        :validation-error="validationStore.getByField('oralHearingDates')"
      >
        <ChipsDateInput
          :id="slotProps.id"
          v-model="coreDataModel.oralHearingDates"
          aria-label="Datum der mündlichen Verhandlung"
          :has-error="slotProps.hasError"
          test-id="oral-hearing-dates"
          @focus="validationStore.remove('oralHearingDates')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <div :class="layoutClass">
      <InputField
        id="appraisalBody"
        v-slot="slotProps"
        class="flex-col"
        label="Spruchkörper"
      >
        <InputText
          :id="slotProps.id"
          v-model="coreDataModel.appraisalBody"
          aria-label="Spruchkörper"
          fluid
          :invalid="slotProps.hasError"
          size="small"
        />
      </InputField>

      <InputField
        v-if="isPendingProceeding"
        id="resolutionDate"
        v-slot="slotProps"
        :label="pendingProceedingLabels.resolutionDate"
      >
        <DateInput
          :id="slotProps.id"
          v-model="coreDataModel.resolutionDate"
          :aria-label="pendingProceedingLabels.resolutionDate"
          :has-error="slotProps.hasError"
          @update:validation-error="slotProps.updateValidationError"
        ></DateInput>
      </InputField>

      <InputField
        v-if="!isPendingProceeding"
        id="documentType"
        v-slot="{ id }"
        class="flex-col"
        label="Dokumenttyp *"
      >
        <ComboboxInput
          :id="id"
          v-model="coreDataModel.documentType"
          aria-label="Dokumenttyp"
          :item-service="ComboboxItemService.getCaselawDocumentTypes"
        ></ComboboxInput>
      </InputField>
    </div>

    <div :class="layoutClass">
      <InputField
        id="deviatingDocumentNumbers"
        v-slot="slotProps"
        label="Abweichende Dokumentnummer"
      >
        <ChipsInput
          :id="slotProps.id"
          v-model="deviatingDocumentNumbers"
          aria-label="Abweichende Dokumentnummer"
          data-testid="deviating-document-numbers"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('deviatingDocumentNumbers')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>

      <InputField
        v-if="!isPendingProceeding"
        id="celex"
        v-slot="slotProps"
        class="flex-col"
        :label="coreDataLabels.celexNumber"
      >
        <InputText
          :id="slotProps.id"
          v-model="coreDataModel.celexNumber"
          aria-label="Celex-Nummer"
          fluid
          :invalid="slotProps.hasError"
          :readonly="
            coreDataModel.court?.label != 'EuG' &&
            coreDataModel.court?.label != 'EuGH'
          "
          size="small"
        />
      </InputField>
    </div>

    <div v-if="!isPendingProceeding" :class="layoutClass">
      <NestedComponent
        aria-label="Abweichender ECLI"
        class="w-full"
        :is-open="!!coreDataModel.deviatingEclis?.length"
      >
        <InputField id="ecli" v-slot="{ id }" class="flex-col" label="ECLI">
          <InputText
            :id="id"
            v-model="coreDataModel.ecli"
            aria-label="ECLI"
            fluid
            size="small"
          />
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField
            id="deviatingEclis"
            v-slot="slotProps"
            label="Abweichender ECLI"
          >
            <ChipsInput
              :id="slotProps.id"
              v-model="deviatingEclis"
              aria-label="Abweichender ECLI"
              data-testid="deviating-eclis"
              :has-error="slotProps.hasError"
              @focus="validationStore.remove('deviatingEclis')"
              @update:validation-error="slotProps.updateValidationError"
            />
          </InputField>
        </template>
      </NestedComponent>

      <NestedComponent
        aria-label="Vorgangshistorie"
        class="w-full"
        :is-open="!!descendingPreviousProcedures?.length"
      >
        <InputField
          id="procedure"
          v-slot="{ id }"
          class="flex-col"
          label="Vorgang"
        >
          <ComboboxInput
            :id="id"
            v-model="coreDataModel.procedure"
            aria-label="Vorgang"
            :item-service="ComboboxItemService.getProcedures"
            manual-entry
            no-clear
          ></ComboboxInput>
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField
            id="previousProcedures"
            v-slot="{ id }"
            label="Vorgangshistorie"
          >
            <InputText
              :id="id"
              v-model="descendingPreviousProcedures"
              aria-label="Vorgangshistorie"
              fluid
              readonly
            />
          </InputField>
        </template>
      </NestedComponent>
    </div>

    <div v-if="!isPendingProceeding" :class="layoutClass">
      <InputField id="legalEffect" v-slot="{ id }" label="Rechtskraft *">
        <InputSelect
          :id="id"
          v-model="coreDataModel.legalEffect"
          aria-label="Rechtskraft"
          fluid
          option-label="label"
          option-value="value"
          :options="legalEffectTypes.items"
          placeholder="Bitte auswählen"
        />
      </InputField>

      <InputField
        id="yearsOfDispute"
        v-slot="slotProps"
        label="Streitjahr"
        :validation-error="validationStore.getByField('yearsOfDispute')"
      >
        <ChipsYearInput
          :id="slotProps.id"
          v-model="coreDataModel.yearsOfDispute"
          aria-label="Streitjahr"
          :has-error="slotProps.hasError"
          test-id="year-of-dispute"
          @focus="validationStore.remove('yearsOfDispute')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <div v-if="!isPendingProceeding" :class="layoutClass">
      <InputField
        id="sources"
        v-slot="slotProps"
        label="Quelle"
        :validation-error="validationStore.getByField('sources')"
      >
        <InputMultiSelect
          :id="slotProps.id"
          v-model="sources"
          aria-label="Quelle Input"
          data-testid="source-input"
          display="chip"
          fluid
          :invalid="slotProps.hasError"
          option-label="label"
          option-value="value"
          :options="sourceItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField id="inputTypes" v-slot="slotProps" label="Eingangsart">
        <div class="flex w-full flex-col">
          <ChipsInput
            :id="slotProps.id"
            v-model="inputTypes"
            aria-label="Eingangsart"
            data-testid="input-types"
            :has-error="slotProps.hasError"
            @focus="validationStore.remove('inputTypes')"
            @update:validation-error="slotProps.updateValidationError"
          />
          <div class="ris-label3-regular pt-4">
            Papier, BLK-DB-Schnittstelle, EUR-LEX-Schnittstelle, E-Mail
          </div>
        </div>
      </InputField>
    </div>
    <div :class="layoutClass">
      <InputField
        id="jurisdictionType"
        v-slot="{ id }"
        class="flex-col"
        label="Gerichtsbarkeit"
      >
        <InputText
          :id="id"
          v-model="jurisdictionType"
          aria-label="Gerichtsbarkeit"
          data-testid="jurisdiction-type"
          fluid
          readonly
          size="small"
        />
      </InputField>

      <InputField id="region" v-slot="{ id }" class="flex-col" label="Region">
        <InputText
          :id="id"
          v-model="region"
          aria-label="Region"
          fluid
          readonly
          size="small"
        />
      </InputField>
    </div>
    <InputField
      v-if="isPendingProceeding"
      id="isResolved"
      v-slot="{ id }"
      :label="pendingProceedingLabels.isResolved"
      label-class="ris-label1-regular"
      :label-position="LabelPosition.RIGHT"
    >
      <Checkbox
        v-model="coreDataModel.isResolved"
        :aria-label="pendingProceedingLabels.isResolved"
        binary
        data-testid="is-resolved"
        :input-id="id"
        size="large"
      />
    </InputField>

    <div
      v-if="coreDataModel.court?.label === 'BGH' && !isPendingProceeding"
      class="flex flex-row gap-24"
    >
      <InputField
        id="leadingDecisionNormReferences"
        v-slot="slotProps"
        label="BGH Nachschlagewerk"
      >
        <ChipsInput
          :id="slotProps.id"
          v-model="leadingDecisionNormReferences"
          aria-label="BGH Nachschlagewerk"
          data-testid="leading-decision-norm-references"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('leadingDecisionNormReferences')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>

    <div class="mt-4">* Pflichtfelder zur Übergabe</div>
  </div>
</template>
