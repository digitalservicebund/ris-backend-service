<script lang="ts" setup>
import { computed } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ActiveCitation from "@/domain/activeCitation"
import ComboboxItemService from "@/services/comboboxItemService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const props = defineProps<{ modelValue?: ActiveCitation }>()
const emit =
  defineEmits<(e: "update:modelValue", value: ActiveCitation) => void>()

const activeCitation = computed({
  get() {
    return props.modelValue as ActiveCitation
  },
  set(value) {
    emit("update:modelValue", value)
  },
})

const activeCitationPredicate = computed({
  get: () =>
    activeCitation?.value?.predicateList
      ? {
          label: activeCitation.value.predicateList,
        }
      : undefined,
  set: (newValue) => {
    let activeCitationRef = new ActiveCitation()
    if (newValue) {
      activeCitationRef = new ActiveCitation({
        ...activeCitation.value,
        predicateList: newValue.label,
      })
    } else delete activeCitationRef.predicateList
    emit("update:modelValue", activeCitationRef)
  },
})
</script>

<template>
  <div>
    <InputField id="activeCitationPredicate" label="Prädikat">
      <ComboboxInput
        id="activeCitationPredicate"
        v-model="activeCitationPredicate"
        aria-label="Suchfeld"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getCitationStyles"
        placeholder="Bitte auswählen"
      ></ComboboxInput>
    </InputField>
    <div class="flex gap-24 justify-between">
      <InputField id="activeCitationDecisionDate" label="Entscheidungsdatum">
        <DateInput
          id="activeCitationDecisionDate"
          v-model="activeCitation.decisionDate"
          aria-label="Entscheidungsdatum Aktivzitierung"
        ></DateInput>
      </InputField>
      <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
        <ComboboxInput
          id="activeCitationDecisionDocumentType"
          v-model="activeCitation.documentType"
          aria-label="Dokumenttyp Aktivzitierung"
          placeholder="Bitte auswählen"
        ></ComboboxInput>
      </InputField>
    </div>
    <div class="flex gap-24 justify-between">
      <InputField id="activeCitationDocumentType" label="Aktenzeichen">
        <TextInput
          id="activeCitationDocumentType"
          v-model="activeCitation.fileNumber"
          aria-label="Aktenzeichen Aktivzitierung"
          placeholder="Aktenzeichen"
        ></TextInput>
      </InputField>
      <InputField id="activeCitationCourt" label="Gericht">
        <ComboboxInput
          id="activeCitationCourt"
          v-model="activeCitation.court"
          aria-label="Gericht Aktivzitierung"
          clear-on-choosing-item
          placeholder="Aktivzitierung Gericht"
        >
        </ComboboxInput>
      </InputField>
    </div>
  </div>
</template>
