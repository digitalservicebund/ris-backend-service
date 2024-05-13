<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import { ValidationError } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import YearInput from "@/components/input/YearInput.vue"
import { useInjectCourtType } from "@/composables/useCourtType"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalForce from "@/domain/legalForce"
import SingleNorm, { SingleNormValidationInfo } from "@/domain/singleNorm"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import FeatureToggleService from "@/services/featureToggleService"
import IconClear from "~icons/material-symbols/close-small"

const props = defineProps<{
  modelValue: SingleNorm
  normAbbreviation: string
}>()

const emit = defineEmits<{
  "update:modelValue": [value: SingleNorm]
  removeEntry: [void]
  "update:validationError": [value?: ValidationError, field?: string]
}>()

const validationStore = useValidationStore<(typeof SingleNorm.fields)[number]>()
const legalForceValidationScore =
  useValidationStore<(typeof LegalForce.fields)[number]>()
const singleNormInput = ref<InstanceType<typeof TextInput> | null>(null)

const courtTypeRef = useInjectCourtType()
const courtsWithLegalForce = ["BVerfG", "VerfGH", "VerfG", "StGH", "VGH", "OVG"]

const featureToggle = ref()

const singleNorm = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const withLegalForce = ref(false)

const legalForceType = computed({
  get: () =>
    props.modelValue.legalForce && props.modelValue.legalForce.type
      ? {
          uuid: props.modelValue.legalForce.type.uuid,
          label: props.modelValue.legalForce.type.abbreviation,
          value: props.modelValue.legalForce.type.abbreviation,
          abbreviation: props.modelValue.legalForce.type.abbreviation,
        }
      : undefined,
  set: (newValue) => {
    if (!newValue && singleNorm.value.legalForce?.type) {
      delete singleNorm.value.legalForce.type
    } else {
      singleNorm.value.legalForce = new LegalForce({
        ...props.modelValue.legalForce,
        type: newValue,
      })
    }
  },
})

const legalForceRegion = computed({
  get: () =>
    singleNorm.value.legalForce && singleNorm.value.legalForce.region
      ? {
          label: singleNorm.value.legalForce.region.longText,
          longText: singleNorm.value.legalForce.region.longText,
        }
      : undefined,
  set: (newValue) => {
    if (!newValue && singleNorm.value.legalForce?.region) {
      delete singleNorm.value.legalForce.region
    } else {
      singleNorm.value.legalForce = new LegalForce({
        ...props.modelValue.legalForce,
        region: newValue,
      })
    }
  },
})

const isCourtWithLegalForce = computed(() => {
  return courtsWithLegalForce.includes(courtTypeRef.value)
})

async function validateNorm() {
  validationStore.reset()
  emit("update:validationError", undefined, "singleNorm")

  //validate singleNorm
  if (singleNorm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: singleNorm.value?.singleNorm,
      normAbbreviation: props.normAbbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok") {
      validationStore.add("Inhalt nicht valide", "singleNorm")

      emit(
        "update:validationError",
        {
          message: "Inhalt nicht valide",
          instance: "singleNorm",
        },
        "singleNorm",
      )
    }
  }
}

async function removeSingleNormEntry() {
  emit("removeEntry")
}

function validateLegalForce() {
  legalForceValidationScore.reset()
  singleNorm.value.legalForce?.missingRequiredFields.forEach((field) =>
    legalForceValidationScore.add("Pflichtfeld nicht befüllt", field),
  )
}

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
  field: string,
) {
  if (validationError) {
    emit(
      "update:validationError",
      {
        message: validationError.message,
        instance: validationError.instance,
      },
      field,
    )
  } else {
    emit("update:validationError", undefined, field)
  }
}

onMounted(async () => {
  if (props.modelValue.singleNorm) {
    await validateNorm()
  }
  validateLegalForce()

  withLegalForce.value = singleNorm.value?.hasLegalForce
  singleNormInput.value?.focusInput()
  featureToggle.value = (
    await FeatureToggleService.isEnabled("neuris.legal-force")
  ).data
})
</script>

<template>
  <div class="mb-24 flex flex-col gap-24 border-b-1 border-b-blue-300 pb-24">
    <div
      v-if="featureToggle && isCourtWithLegalForce"
      class="flex flex-row justify-between gap-24"
    >
      <InputField
        id="legalForce"
        v-slot="{ id }"
        label="Mit Gesetzeskraft"
        label-class="ds-label-01-reg"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="id"
          v-model="withLegalForce"
          aria-label="Gesetzeskraft der Norm"
          data-testid="legal-force-checkbox"
          size="small"
        />
      </InputField>
      <button
        aria-label="Einzelnorm löschen"
        class="text-blue-800 focus:shadow-[inset_0_0_0_0.25rem] focus:shadow-blue-800 focus:outline-none"
        tabindex="0"
        @click="removeSingleNormEntry"
      >
        <IconClear />
      </button>
    </div>
    <div
      class="gap-24"
      :class="
        featureToggle && isCourtWithLegalForce
          ? 'grid grid-cols-3'
          : 'flex flex-row justify-between'
      "
    >
      <InputField
        id="singleNorm"
        v-slot="slotProps"
        label="Einzelnorm"
        :validation-error="validationStore.getByField('singleNorm')"
      >
        <TextInput
          id="singleNorm"
          ref="singleNormInput"
          v-model="singleNorm.singleNorm"
          aria-label="Einzelnorm der Norm"
          :has-error="slotProps.hasError"
          size="medium"
          @blur="validateNorm"
          @input="validationStore.remove('singleNorm')"
        ></TextInput>
      </InputField>
      <InputField
        id="dateOfVersion"
        v-slot="slotProps"
        label="Fassungsdatum"
        :validation-error="validationStore.getByField('dateOfVersion')"
        @update:validation-error="
          (validationError) =>
            updateDateFormatValidation(validationError, 'dateOfVersion')
        "
      >
        <DateInput
          id="dateOfVersion"
          v-model="singleNorm.dateOfVersion"
          aria-label="Fassungsdatum der Norm"
          class="ds-input-medium"
          :has-error="slotProps.hasError"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
      <InputField
        id="dateOfRelevance"
        v-slot="slotProps"
        label="Jahr"
        :validation-error="validationStore.getByField('dateOfRelevance')"
        @update:validation-error="
          (validationError) =>
            updateDateFormatValidation(validationError, 'dateOfRelevance')
        "
      >
        <YearInput
          id="dateOfRelevance"
          v-model="singleNorm.dateOfRelevance"
          aria-label="Jahr der Norm"
          :has-error="slotProps.hasError"
          size="medium"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
      <button
        v-if="!featureToggle || !isCourtWithLegalForce"
        aria-label="Einzelnorm löschen"
        class="mt-[25px] h-[50px] text-blue-800 focus:shadow-[inset_0_0_0_0.25rem] focus:shadow-blue-800 focus:outline-none"
        tabindex="0"
        @click="removeSingleNormEntry"
      >
        <IconClear />
      </button>
    </div>
    <div
      v-if="featureToggle && withLegalForce && isCourtWithLegalForce"
      class="grid grid-cols-3 gap-24"
    >
      <div>
        <InputField
          id="legalForceType"
          v-slot="slotProps"
          label="Typ der Ges.-Kraft *"
          :validation-error="legalForceValidationScore.getByField('type')"
          @input="legalForceValidationScore.remove('type')"
        >
          <ComboboxInput
            id="legalForceType"
            v-model="legalForceType"
            aria-label="Gesetzeskraft Typ"
            data-testid="legal-force-type-combobox"
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getLegalForceTypes"
            @input="legalForceValidationScore.remove('type')"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="col-span-2">
        <InputField
          id="legalForceRegion"
          v-slot="slotProps"
          label="Geltungsbereich *"
          :validation-error="legalForceValidationScore.getByField('region')"
          @input="legalForceValidationScore.remove('region')"
        >
          <ComboboxInput
            id="legalForceRegion"
            v-model="legalForceRegion"
            aria-label="Gesetzeskraft Geltungsbereich"
            data-testid="legal-force-region-combobox"
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getLegalForceRegions"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
  </div>
</template>
