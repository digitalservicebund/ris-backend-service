<script lang="ts" setup>
import { computed, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { PublicationState } from "@/domain/documentUnit"
import { DocumentUnitListEntry } from "@/domain/documentUnitListEntry"
import ComboboxItemService from "@/services/comboboxItemService"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { DropdownItem } from "@/shared/components/input/types"

const emit = defineEmits<{
  search: [value: DocumentUnitListEntry]
}>()

const searchEntry = ref<DocumentUnitListEntry>({} as DocumentUnitListEntry)

const dropdownItems: DropdownItem[] = [
  { label: "Veröffentlicht", value: "Veröffentlicht" },
  { label: "Unveröffentlicht", value: "Unveröffentlicht" },
  { label: "In Veröffentlichung", value: "In Veröffentlichung" },
]

const publishingStateModel = computed({
  get: () => searchEntry.value?.status?.publicationStatus,
  set: (data) => {
    return (searchEntry.value.status = {
      publicationStatus: data as PublicationState,
      withError: false,
    })
  },
})
</script>

<template>
  <div class="mb-32 flex flex-col gap-20 bg-blue-200 p-24">
    <h2 class="ds-heading-02-reg pt-8">Suche</h2>
    <div class="flex justify-between gap-20">
      <InputField id="documentNumber" label="Dokumentnummer oder Aktenzeichen">
        <TextInput
          id="documentNumber"
          v-model="searchEntry.documentNumber"
          aria-label="Dokumentnummer Suche"
          placeholder="Dokumentnummer"
        ></TextInput>
      </InputField>
      <InputField id="court" label="Gericht">
        <ComboboxInput
          id="court"
          v-model="searchEntry.court"
          aria-label="Gericht Suche"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getCourts"
          placeholder="Gerichtstyp Gerichtsort"
        ></ComboboxInput>
      </InputField>
      <InputField id="date" label="Entscheidungsdatum">
        <DateInput
          id="decisionDate"
          v-model="searchEntry.decisionDate"
          aria-label="Entscheidungsdatum Suche"
        ></DateInput>
      </InputField>
      <InputField id="status" label="Status">
        <DropdownInput
          id="status"
          v-model="publishingStateModel"
          aria-label="Status Suche"
          :items="dropdownItems"
          placeholder="Status"
        />
      </InputField>
    </div>
    <div>
      <TextButton
        label="Ergebnisse anzeigen"
        @click="emit('search', searchEntry)"
      />
    </div>
  </div>
</template>
