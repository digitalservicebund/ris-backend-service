<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { onMounted, ref, watch } from "vue"
import CollectiveAgreementDateInput from "@/components/CollectiveAgreementDateInput.vue"
import CollectiveAgreementNormInput from "@/components/CollectiveAgreementNormInput.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import { ValidationError } from "@/components/input/types"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: CollectiveAgreement
  modelValueList?: CollectiveAgreement[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: CollectiveAgreement]
  "update:validationError": [value: ValidationError]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(
  new CollectiveAgreement({ ...props.modelValue }),
)
const collectiveAgreement = ref(
  new CollectiveAgreement({ ...props.modelValue }),
)

async function addCollectiveAgreement() {
  emit("update:modelValue", collectiveAgreement.value as CollectiveAgreement)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    collectiveAgreement.value = new CollectiveAgreement({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new CollectiveAgreement({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  collectiveAgreement.value = new CollectiveAgreement({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="collectiveAgreementNameInput"
          label="Bezeichnung des Tarifvertrags *"
        >
          <InputText
            id="collectiveAgreementNameInputText"
            v-model="collectiveAgreement.name"
            aria-label="Bezeichnung des Tarifvertrags"
            class="w-full"
            size="small"
          ></InputText>
        </InputField>
      </div>

      <div class="basis-1/2">
        <InputField
          id="collectiveAgreementDateInput"
          v-slot="slotProps"
          label="Datum"
        >
          <CollectiveAgreementDateInput
            id="collectiveAgreementDateInputText"
            v-model="collectiveAgreement.date"
            @update:validation-error="slotProps.updateValidationError"
          ></CollectiveAgreementDateInput>
        </InputField>
      </div>
    </div>
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="collectiveAgreementNormInput"
          v-slot="slotProps"
          label="Tarifnorm"
        >
          <CollectiveAgreementNormInput
            id="collectiveAgreementNormInputText"
            v-model="collectiveAgreement.norm"
            @update:validation-error="slotProps.updateValidationError"
          ></CollectiveAgreementNormInput>
        </InputField>
      </div>

      <div class="basis-1/2">
        <InputField
          id="collectiveAgreementIndustryInput"
          v-slot="slotProps"
          label="Branche"
        >
          <ComboboxInput
            id="collectiveAgreementIndustryInputText"
            v-model="collectiveAgreement.industry"
            aria-label="Branche"
            class="w-full"
            :invalid="slotProps.hasError"
            :item-service="ComboboxItemService.getCollectiveAgreementIndustries"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Tarifvertrag speichern"
            :disabled="collectiveAgreement.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addCollectiveAgreement"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
