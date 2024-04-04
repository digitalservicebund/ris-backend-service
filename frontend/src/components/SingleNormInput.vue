<script lang="ts" setup>
import { onMounted, ref, watch } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import YearInput from "@/components/input/YearInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import SingleNorm, { SingleNormValidationInfo } from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  modelValue: SingleNorm
  normAbbreviation: string
}>()

const validationStore = useValidationStore<(typeof SingleNorm.fields)[number]>()

const singleNorm = ref(props.modelValue)

async function validateNorm() {
  validationStore.reset()

  //validate singleNorm
  if (singleNorm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: singleNorm.value?.singleNorm,
      normAbbreviation: props.normAbbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok")
      validationStore.add("Inhalt nicht valide", "singleNorm")
  }

  //validate required fields
  if (singleNorm.value?.missingRequiredFields?.length) {
    singleNorm.value?.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

watch(
  () => props.modelValue,
  () => {
    singleNorm.value = new SingleNorm({ ...props.modelValue })
  },
)

onMounted(async () => {
  // On first mount, we don't need to validate. When the props.modelValue do not
  // have the isEmpty getter, we can be sure that it has not been initialized as
  // NormReference and is therefore the inital load. As soons as we are using
  // uuid for norms, the check should be 'props.modelValue?.uuid !== undefined'

  singleNorm.value = new SingleNorm({ ...props.modelValue })
})
</script>

<template>
  <div
    class="mb-24 flex justify-between gap-24 border-b-1 border-blue-300 pb-24"
  >
    <InputField
      id="norm-reference-singleNorm-field"
      v-slot="slotProps"
      label="Einzelnorm"
      :validation-error="validationStore.getByField('singleNorm')"
    >
      <TextInput
        id="norm-reference-singleNorm"
        v-model="singleNorm.singleNorm"
        aria-label="Einzelnorm der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @blur="validateNorm"
        @input="validationStore.remove('singleNorm')"
      ></TextInput>
    </InputField>
    <InputField
      id="norm-date-of-version"
      v-slot="slotProps"
      label="Fassungsdatum"
      :validation-error="validationStore.getByField('dateOfVersion')"
    >
      <DateInput
        id="norm-date-of-version"
        v-model="singleNorm.dateOfVersion"
        aria-label="Fassungsdatum der Norm"
        class="ds-input-medium"
        :has-error="slotProps.hasError"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
    <InputField
      id="norm-date-of-relevance"
      v-slot="slotProps"
      label="Jahr"
      :validation-error="validationStore.getByField('dateOfRelevance')"
    >
      <YearInput
        id="norm-date-of-relevance"
        v-model="singleNorm.dateOfRelevance"
        aria-label="Jahr der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
  </div>
</template>
