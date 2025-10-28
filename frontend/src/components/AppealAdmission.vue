<script lang="ts" setup>
import InputSelect from "primevue/select"
import { computed } from "vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import InputField from "@/components/input/InputField.vue"
import { AppealAdmitter } from "@/domain/appealAdmitter"
import { contentRelatedIndexingLabels } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const appealAdmitted = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appealAdmission?.admitted,
  set: (newValue) => {
    if (newValue == null) {
      store.documentUnit!.contentRelatedIndexing.appealAdmission = undefined
      return
    }

    store.documentUnit!.contentRelatedIndexing.appealAdmission = {
      admitted: newValue,
    }
  },
})

const appealAdmittedBy = computed({
  get: () => {
    if (store.documentUnit!.contentRelatedIndexing.appealAdmission?.admitted) {
      return store.documentUnit!.contentRelatedIndexing.appealAdmission?.by
    }
    return undefined
  },
  set: (newValue) => {
    if (!store.documentUnit!.contentRelatedIndexing.appealAdmission?.admitted) {
      return
    }
    store.documentUnit!.contentRelatedIndexing.appealAdmission.by = newValue
  },
})
</script>

<template>
  <div :aria-label="contentRelatedIndexingLabels.appealAdmission">
    <h2
      :id="DocumentUnitCategoriesEnum.APPEAL_ADMISSION"
      class="ris-label1-bold mb-16"
    >
      {{ contentRelatedIndexingLabels.appealAdmission }}
    </h2>
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="appealAdmittedInput"
          v-slot="{ id }"
          label="Rechtsmittel zugelassen"
        >
          <InputSelect
            :id="id"
            v-model="appealAdmitted"
            aria-label="Rechtsmittel zugelassen"
            fluid
            option-label="label"
            option-value="value"
            :options="[
              { label: 'Ja', value: true },
              { label: 'Nein', value: false },
            ]"
            show-clear
          />
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          v-if="appealAdmitted"
          id="appealAdmittedByInput"
          v-slot="{ id }"
          label="Rechtsmittel zugelassen durch"
        >
          <InputSelect
            :id="id"
            v-model="appealAdmittedBy"
            aria-label="Rechtsmittel zugelassen durch"
            fluid
            :options="Object.values(AppealAdmitter)"
            show-clear
          />
        </InputField>
      </div>
    </div>
  </div>
</template>
