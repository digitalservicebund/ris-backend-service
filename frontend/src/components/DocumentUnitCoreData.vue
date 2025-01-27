<script lang="ts" setup>
import { computed, toRefs, watch, ref, onMounted, onBeforeUnmount } from "vue"
import { DropdownItem } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import ChipsYearInput from "@/components/input/ChipsYearInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import DropdownInput from "@/components/input/DropdownInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import NestedComponent from "@/components/NestedComponents.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import { CoreData, SourceValue } from "@/domain/documentUnit"
import ComboboxItemService from "@/services/comboboxItemService"

interface Props {
  modelValue: CoreData
}

const props = defineProps<Props>()
const emit = defineEmits<{
  "update:modelValue": [value: CoreData]
}>()
const { modelValue } = toRefs(props)
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
  parentWidth.value < 768 ? "flex flex-col gap-24" : "flex flex-row gap-24",
)

/**
 * Our UI turns the chronological order of the list, so the latest previous procedure is first.
 */
const descendingPreviousProcedures = computed(() =>
  modelValue.value.previousProcedures
    ? modelValue.value.previousProcedures.toReversed()
    : undefined,
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
    props.modelValue.source?.value
      ? props.modelValue.source?.value
      : (props.modelValue.source?.sourceRawValue ?? undefined),
  set: (newValue) => {
    if (Object.values(SourceValue).includes(newValue as SourceValue)) {
      modelValue.value.source = {
        ...modelValue.value.source,
        value: newValue as SourceValue,
      }
    }
  },
})

watch(
  modelValue,
  () => {
    emit("update:modelValue", modelValue.value)
  },
  { deep: true },
)

/**
 * This updates the local norm with the updated model value from the props. It also stores a copy of the last saved
 * model value, because the local norm might change in between. When the new model value is empty, all validation
 * errors are resetted. If it has an amiguous norm reference, the validation store is updated. When the list of
 * single norms is empty, a new empty single norm entry is added.
 */
watch(
  () => props.modelValue,
  () => {
    if (
      !!props.modelValue.source &&
      !props.modelValue.source.value &&
      !!props.modelValue.source.sourceRawValue
    ) {
      validationStore.add(
        `"${props.modelValue.source.sourceRawValue}" ist keine gültige Quellenangabe`,
        "source",
      )
    }
  },
  { immediate: true, deep: true },
)

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
      :is-open="!!modelValue.deviatingCourts?.length"
    >
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

    <div :class="layoutClass">
      <NestedComponent
        aria-label="Abweichendes Aktenzeichen"
        class="w-full min-w-0"
        :is-open="!!modelValue.deviatingFileNumbers?.length"
      >
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
        :is-open="!!modelValue.deviatingDecisionDates?.length"
      >
        <InputField
          id="decisionDate"
          v-slot="slotProps"
          label="Entscheidungsdatum *"
          :validation-error="validationStore.getByField('decisionDate')"
        >
          <DateInput
            id="decisionDate"
            v-model="modelValue.decisionDate"
            aria-label="Entscheidungsdatum"
            class="ds-input-medium"
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
              v-model="modelValue.deviatingDecisionDates"
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
        <TextInput
          id="appraisalBody"
          v-model="modelValue.appraisalBody"
          aria-label="Spruchkörper"
          class="ds-input-medium"
          :has-error="slotProps.hasError"
          size="medium"
        ></TextInput>
      </InputField>

      <InputField id="documentType" class="flex-col" label="Dokumenttyp *">
        <ComboboxInput
          id="documentType"
          v-model="modelValue.documentType"
          aria-label="Dokumenttyp"
          :item-service="ComboboxItemService.getDocumentTypes"
        ></ComboboxInput>
      </InputField>
    </div>

    <div :class="layoutClass">
      <NestedComponent
        aria-label="Abweichender ECLI"
        class="w-full"
        :is-open="!!modelValue.deviatingEclis?.length"
      >
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

      <NestedComponent
        aria-label="Vorgangshistorie"
        class="w-full"
        :is-open="!!descendingPreviousProcedures?.length"
      >
        <InputField id="procedure" class="flex-col" label="Vorgang">
          <ComboboxInput
            id="procedure"
            v-model="modelValue.procedure"
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

    <div :class="layoutClass">
      <InputField id="legalEffect" v-slot="{ id }" label="Rechtskraft *">
        <DropdownInput
          :id="id"
          v-model="modelValue.legalEffect"
          aria-label="Rechtskraft"
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
    <div :class="layoutClass">
      <InputField
        id="source"
        v-slot="slotProps"
        label="Quelle"
        :validation-error="validationStore.getByField('source')"
      >
        <DropdownInput
          :id="slotProps.id"
          v-model="source"
          aria-label="Quelle"
          :has-error="slotProps.hasError"
          :items="sourceItems"
          placeholder="Bitte auswählen"
        />
      </InputField>

      <!-- Todo Eingangsart -->
      <InputField id="region" class="flex-col" label="Eingangsart">
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
    <div>
      <InputField
        id="yearsOfDispute"
        v-slot="slotProps"
        label="Streitjahr"
        :validation-error="validationStore.getByField('yearsOfDispute')"
      >
        <ChipsYearInput
          id="yearOfDispute"
          v-model="modelValue.yearsOfDispute"
          aria-label="Streitjahr"
          data-testid="year-of-dispute"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('yearsOfDispute')"
          @update:validation-error="slotProps.updateValidationError"
        ></ChipsYearInput>
      </InputField>
    </div>

    <div v-if="modelValue.court?.label === 'BGH'" class="flex flex-row gap-24">
      <InputField
        id="leadingDecisionNormReferences"
        label="BGH Nachschlagewerk"
      >
        <ChipsInput
          id="leadingDecisionNormReferences"
          v-model="modelValue.leadingDecisionNormReferences"
          aria-label="BGH Nachschlagewerk"
        ></ChipsInput>
      </InputField>
    </div>

    <div class="mt-4">* Pflichtfelder zur Übergabe</div>
  </div>
</template>
