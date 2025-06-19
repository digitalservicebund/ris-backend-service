<script lang="ts" setup>
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import InputSelect from "primevue/select"
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue"
import { DropdownItem } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import ChipsYearInput from "@/components/input/ChipsYearInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import NestedComponent from "@/components/NestedComponents.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import { CoreData } from "@/domain/coreData"
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
      "decisionDate",
      "yearsOfDispute",
      "deviatingDecisionDates",
      "source",
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
    ? coreDataModel.value.previousProcedures.toReversed()
    : undefined,
)

const jurisdictionType = computed(() =>
  coreDataModel.value.court ? coreDataModel.value.court.jurisdictionType : "",
)

const region = computed(() =>
  coreDataModel.value.court ? coreDataModel.value.court.region : "",
)

const sourceItems: DropdownItem[] = [
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
]

const source = computed({
  get: () =>
    coreDataModel.value.source
      ? (coreDataModel.value.source.value ??
        coreDataModel.value.source?.sourceRawValue)
      : undefined,
  set: (newValue) => {
    if (Object.values(SourceValue).includes(newValue as SourceValue)) {
      coreDataModel.value.source = {
        ...coreDataModel.value.source,
        value: newValue as SourceValue,
      }
    }
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
    aria-label="Stammdaten"
    class="core-data flex flex-col gap-24 bg-white p-24"
  >
    <TitleElement>Stammdaten</TitleElement>
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
        <InputField id="deviatingCourt" label="Fehlerhaftes Gericht">
          <ChipsInput
            id="deviatingCourt"
            v-model="coreDataModel.deviatingCourts"
            aria-label="Fehlerhaftes Gericht"
          ></ChipsInput>
        </InputField>
      </template>
    </NestedComponent>

    <div :class="layoutClass">
      <NestedComponent
        aria-label="Abweichendes Aktenzeichen"
        class="w-full min-w-0"
        :is-open="!!coreDataModel.deviatingFileNumbers?.length"
      >
        <InputField id="fileNumber" label="Aktenzeichen *">
          <ChipsInput
            id="fileNumber"
            v-model="coreDataModel.fileNumbers"
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
              v-model="coreDataModel.deviatingFileNumbers"
              aria-label="Abweichendes Aktenzeichen"
            ></ChipsInput>
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
            id="decisionDate"
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
              id="deviatingDecisionDates"
              v-model="coreDataModel.deviatingDecisionDates"
              aria-label="Abweichendes Entscheidungsdatum"
              :has-error="slotProps.hasError"
              @focus="validationStore.remove('deviatingDecisionDates')"
              @update:validation-error="slotProps.updateValidationError"
            />
          </InputField>
        </template>
      </NestedComponent>
    </div>
    <div :class="layoutClass">
      <InputField
        id="appraisalBody"
        v-slot="slotProps"
        class="flex-col"
        label="Spruchkörper"
      >
        <InputText
          id="appraisalBody"
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
          id="resolutionDate"
          v-model="coreDataModel.resolutionDate"
          :aria-label="pendingProceedingLabels.resolutionDate"
          :has-error="slotProps.hasError"
          @update:validation-error="slotProps.updateValidationError"
        ></DateInput>
      </InputField>

      <InputField
        v-if="!isPendingProceeding"
        id="documentType"
        class="flex-col"
        label="Dokumenttyp *"
      >
        <ComboboxInput
          id="documentType"
          v-model="coreDataModel.documentType"
          aria-label="Dokumenttyp"
          :item-service="ComboboxItemService.getCaselawDocumentTypes"
        ></ComboboxInput>
      </InputField>
    </div>

    <div v-if="!isPendingProceeding" :class="layoutClass">
      <NestedComponent
        aria-label="Abweichender ECLI"
        class="w-full"
        :is-open="!!coreDataModel.deviatingEclis?.length"
      >
        <InputField id="ecli" class="flex-col" label="ECLI">
          <InputText
            id="ecli"
            v-model="coreDataModel.ecli"
            aria-label="ECLI"
            fluid
            size="small"
          />
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField id="deviatingEclis" label="Abweichender ECLI">
            <ChipsInput
              id="deviatingEclis"
              v-model="coreDataModel.deviatingEclis"
              aria-label="Abweichender ECLI"
            ></ChipsInput>
          </InputField>
        </template>
      </NestedComponent>

      <NestedComponent
        aria-label="Vorgangshistorie"
        class="w-full"
        :is-open="!!descendingPreviousProcedures?.length"
      >
        <InputField id="procedure" class="flex-col" label="Vorgang">
          <ComboboxInput
            id="procedure"
            v-model="coreDataModel.procedure"
            aria-label="Vorgang"
            :item-service="ComboboxItemService.getProcedures"
            manual-entry
            no-clear
          ></ComboboxInput>
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField id="previousProcedures" label="Vorgangshistorie">
            <ChipsInput
              id="previousProcedures"
              v-model="descendingPreviousProcedures"
              aria-label="Vorgangshistorie"
              read-only
            ></ChipsInput>
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
          id="yearOfDispute"
          v-model="coreDataModel.yearsOfDispute"
          aria-label="Streitjahr"
          data-testid="year-of-dispute"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('yearsOfDispute')"
          @update:validation-error="slotProps.updateValidationError"
        ></ChipsYearInput>
      </InputField>
    </div>
    <div v-if="!isPendingProceeding" :class="layoutClass">
      <InputField
        id="source"
        v-slot="slotProps"
        label="Quelle"
        :validation-error="validationStore.getByField('source')"
      >
        <InputSelect
          :id="slotProps.id"
          v-model="source"
          aria-label="Quelle Input"
          fluid
          :invalid="slotProps.hasError"
          option-label="label"
          option-value="value"
          :options="sourceItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </div>
    <div :class="layoutClass">
      <InputField
        id="jurisdictionType"
        class="flex-col"
        label="Gerichtsbarkeit"
      >
        <InputText
          id="jurisdictionType"
          v-model="jurisdictionType"
          aria-label="Gerichtsbarkeit"
          data-testid="jurisdiction-type"
          fluid
          readonly
          size="small"
        />
      </InputField>

      <InputField id="region" class="flex-col" label="Region">
        <InputText
          id="region"
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
        label="BGH Nachschlagewerk"
      >
        <ChipsInput
          id="leadingDecisionNormReferences"
          v-model="coreDataModel.leadingDecisionNormReferences"
          aria-label="BGH Nachschlagewerk"
        ></ChipsInput>
      </InputField>
    </div>

    <div class="mt-4">* Pflichtfelder zur Übergabe</div>
  </div>
</template>
