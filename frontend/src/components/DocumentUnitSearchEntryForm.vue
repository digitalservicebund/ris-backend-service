<script lang="ts" setup>
import { computed, ref } from "vue"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import Checkbox from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { DropdownItem } from "@/shared/components/input/types"

const emit = defineEmits<{
  search: [value: DocumentUnitListEntry]
}>()

const searchEntry = ref<DocumentUnitListEntry>({} as DocumentUnitListEntry)

const dropdownItems: DropdownItem[] = [
  { label: "Veröffentlicht", value: PublicationState.PUBLISHED },
  { label: "Unveröffentlicht", value: PublicationState.UNPUBLISHED },
  { label: "In Veröffentlichung", value: PublicationState.PUBLISHING },
]

const publishingStateModel = computed({
  get: () => searchEntry.value?.status?.publicationStatus,
  set: (data) => {
    searchEntry.value.status = {
      publicationStatus: data as PublicationState,
      withError: false,
    }
  },
})

const courtType = computed({
  get: () => searchEntry.value?.court?.type,
  set: (data) => {
    if (data) {
      if (searchEntry.value.court) {
        searchEntry.value.court.type = data
      } else {
        searchEntry.value.court = { type: data, label: data }
      }
    } else {
      searchEntry.value.court && delete searchEntry.value.court.type
    }
  },
})

const courtLocation = computed({
  get: () => searchEntry.value?.court?.location,
  set: (data) => {
    if (data) {
      if (searchEntry.value.court) {
        searchEntry.value.court.location = data
      } else {
        searchEntry.value.court = { location: data, label: data }
      }
    } else {
      searchEntry.value.court && delete searchEntry.value.court.location
    }
  },
})
</script>

<template>
  <div class="mb-32 flex flex-col bg-blue-200 py-24">
    <div
      class="m-32 grid grid-flow-col grid-cols-[180px_1fr_180px_1fr] grid-rows-3 gap-x-20 gap-y-4"
    >
      <!-- Column 1 -->
      <div class="ds-body-01-reg pl-32">Dokumentnummer/ Aktenzeichen</div>
      <div class="ds-body-01-reg pl-32">Gericht</div>
      <div class="ds-body-01-reg pl-32">Entscheidungsdatum</div>
      <!-- Column 2 -->
      <div class="pr-32">
        <InputField
          id="documentNumber"
          label="Dokumentnummer oder Aktenzeichen"
          visually-hide-label
        >
          <TextInput
            id="documentNumber"
            v-model="searchEntry.documentNumber"
            aria-label="Dokumentnummer oder Aktenzeichen Suche"
            placeholder="Dokumentnummeroder Aktenzeichen"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-20 pr-32">
        <InputField id="courtType" label="Gerichtstyp" visually-hide-label>
          <TextInput
            id="courtType"
            v-model="courtType"
            aria-label="Gerichtstyp"
            placeholder="Gerichtstyp"
          ></TextInput>
        </InputField>

        <InputField id="courtLocation" label="Gerichtsort" visually-hide-label>
          <TextInput
            id="courtLocation"
            v-model="courtLocation"
            aria-label="Gerichtsort"
            placeholder="Gerichtsort"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-20 pr-32">
        <InputField id="date" label="Entscheidungsdatum" visually-hide-label>
          <DateInput
            id="decisionDate"
            v-model="searchEntry.decisionDate"
            aria-label="Entscheidungsdatum Suche"
          ></DateInput>
        </InputField>
      </div>
      <!-- Column 3 -->
      <div class="ds-body-01-reg pl-32">Status</div>
      <div class="pl-32"></div>
      <div class="pl-32"></div>
      <!-- Column 4 -->
      <div class="pr-32">
        <InputField id="status" label="Status" visually-hide-label>
          <DropdownInput
            id="status"
            v-model="publishingStateModel"
            aria-label="Status Suche"
            :items="dropdownItems"
            placeholder="Status"
          />
        </InputField>
      </div>
      <div class="pr-32">
        <InputField
          id="documentationOffice"
          v-slot="{ id }"
          label="Nur meine Dokstelle"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="undefined"
            aria-label="Nur meine Dokstelle"
          />
        </InputField>
      </div>
    </div>

    <div class="flex w-full flex-row justify-end pr-64">
      <TextButton
        aria-label="Nach Dokumentationseinheiten suchen"
        label="Ergebnisse anzeigen"
        @click="emit('search', searchEntry)"
      />
    </div>
  </div>
</template>
