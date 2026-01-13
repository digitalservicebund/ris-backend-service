<script lang="ts" setup>
import Button from "primevue/button"
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import { computed, onMounted, ref } from "vue"
import { ValidationError } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import YearInput from "@/components/input/YearInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalForce from "@/domain/legalForce"
import SingleNorm, { SingleNormValidationInfo } from "@/domain/singleNorm"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconClear from "~icons/material-symbols/close-small"

const props = defineProps<{
  modelValue: SingleNorm
  normAbbreviation: string
  index: number
}>()

const emit = defineEmits<{
  "update:modelValue": [value: SingleNorm]
  removeEntry: [void]
  "update:validationError": [value?: ValidationError, field?: string]
}>()

const store = useDocumentUnitStore()
const validationStore = useValidationStore<(typeof SingleNorm.fields)[number]>()
const legalForceValidationStore =
  useValidationStore<(typeof LegalForce.fields)[number]>()
const singleNormInput = ref<InstanceType<typeof InputText> | null>(null)
const isNormValidating = ref(false)

const singleNorm = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

/**
 * Computed property to get or set the presence of a legal force on the model.
 *
 * @constant {ComputedRef<boolean>} hasLegalForce
 * @property {boolean} get - Returns `true` if `modelValue.legalForce` is defined, otherwise `false`.
 * @property {Function} set - Updates `modelValue.legalForce`.
 *                            If set to `true` and `modelValue.legalForce` is undefined, a new `LegalForce`
 *                            instance is created. If set to `false`, `modelValue.legalForce` is set to undefined.
 */
const hasLegalForce = computed({
  get: () => !!props.modelValue.legalForce,
  set: (newValue) => {
    if (newValue && !props.modelValue.legalForce) {
      singleNorm.value.legalForce = new LegalForce()
    } else if (!newValue) {
      singleNorm.value.legalForce = undefined
    }
  },
})

/**
 * Data restructuring from legal force type props to combobox item.
 */
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
    singleNorm.value.legalForce = new LegalForce({
      ...props.modelValue.legalForce,
      type: newValue,
    })
  },
})

/**
 * Data restructuring from legal force region props to combobox item.
 */
const legalForceRegion = computed({
  get: () =>
    singleNorm.value.legalForce && singleNorm.value.legalForce.region
      ? {
          label: singleNorm.value.legalForce.region.longText,
          longText: singleNorm.value.legalForce.region.longText,
        }
      : undefined,
  set: (newValue) => {
    singleNorm.value.legalForce = new LegalForce({
      ...props.modelValue.legalForce,
      region: newValue,
    })
  },
})

const isCourtWithLegalForce = computed(
  () =>
    store.documentUnit?.coreData.court?.jurisdictionType ===
    "Verfassungsgerichtsbarkeit",
)

/**
 * Validates a given single norm against with a given norm abbreviation against a validation endpoint.
 * The validation endpint either responds with "Ok" oder "Validation error". In the latter case a validation error is emitted to the parent.
 */
async function validateNorm() {
  if (isNormValidating.value) {
    return
  }

  isNormValidating.value = true
  validationStore.reset()
  emit("update:validationError", undefined, "singleNorm")
  try {
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
  } finally {
    isNormValidating.value = false
  }
}
/**
 * Emits the 'removeEntry' event to the parent, where the data entry is removed from the model value.
 */
async function removeSingleNormEntry() {
  emit("removeEntry")
}

function validateLegalForce() {
  legalForceValidationStore.reset()
  if (singleNorm.value.legalForce?.missingRequiredFields?.length) {
    singleNorm.value.legalForce?.missingRequiredFields.forEach((field) => {
      legalForceValidationStore.add("Pflichtfeld nicht befüllt", field)
    })
  }
}

/**
 * Could be triggered by invalid date formats in the fields 'dateOfVersion' and 'dateOfRelevance'.
 * This forwards the validation error to the parent, so it knows, that this single norm entry has a validation error.
 * @param validationError The actual message
 * @param field The name of the field with format validation. The validationError also holds this information ('instance'),
 * but when the validationError resets to undefined, we do not have the instance information anymore.
 * For this case this field is needed in order to identify, which field resetted it's valdiation error.
 */
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

/**
 * On mount, the first input is focussed, the local value for legal force is set.
 * If the single norm entry mounts and the single norm field is filled, then it is validated immediately.
 */
onMounted(async () => {
  if (props.modelValue.singleNorm) {
    await validateNorm()
  }
  validateLegalForce()

  hasLegalForce.value = !!singleNorm.value?.legalForce
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const inputElement = (singleNormInput.value as any)?.$el.querySelector(
    "input",
  )
  inputElement?.focus() // This works without TypeScript errors
})

defineExpose({ validateNorm })
</script>

<template>
  <div>
    <div
      v-if="isCourtWithLegalForce"
      class="flex flex-row justify-between gap-24"
    >
      <InputField
        :id="'legalForce' + index"
        v-slot="{ id }"
        label="Mit Gesetzeskraft"
        label-class="ris-label1-regular"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          v-model="hasLegalForce"
          aria-label="Gesetzeskraft der Norm"
          binary
          data-testid="legal-force-checkbox"
          :input-id="id"
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
        isCourtWithLegalForce
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
        <InputText
          id="singleNorm"
          ref="singleNormInput"
          v-model.trim="singleNorm.singleNorm"
          aria-label="Einzelnorm der Norm"
          fluid
          :invalid="slotProps.hasError"
          size="small"
          @blur="validateNorm"
          @focus="validationStore.remove('singleNorm')"
        ></InputText>
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
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('dateOfVersion')"
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
          @focus="validationStore.remove('dateOfRelevance')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>

      <Button
        v-if="!isCourtWithLegalForce"
        v-tooltip.bottom="{
          value: 'Einzelnorm entfernen',
        }"
        aria-label="Einzelnorm löschen"
        class="min-w-48 self-end"
        text
        @click="removeSingleNormEntry"
      >
        <template #icon>
          <IconClear class="order-last" />
        </template>
      </Button>
    </div>
    <div
      v-if="hasLegalForce && isCourtWithLegalForce"
      class="grid grid-cols-3 gap-24"
    >
      <div>
        <InputField
          id="type"
          v-slot="slotProps"
          label="Typ der Ges.-Kraft *"
          :validation-error="legalForceValidationStore.getByField('type')"
        >
          <ComboboxInput
            id="legalForceType"
            v-model="legalForceType"
            aria-label="Gesetzeskraft Typ"
            data-testid="legal-force-type-combobox"
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getLegalForceTypes"
            @focus="legalForceValidationStore.remove('type')"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="col-span-2">
        <InputField
          id="region"
          v-slot="slotProps"
          label="Geltungsbereich *"
          :validation-error="legalForceValidationStore.getByField('region')"
        >
          <ComboboxInput
            id="legalForceRegion"
            v-model="legalForceRegion"
            aria-label="Gesetzeskraft Geltungsbereich"
            data-testid="legal-force-region-combobox"
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getLegalForceRegions"
            @focus="legalForceValidationStore.remove('region')"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
  </div>
</template>
