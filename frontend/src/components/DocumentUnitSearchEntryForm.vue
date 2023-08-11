<script lang="ts" setup>
import { computed, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import ComboboxItemService from "@/services/comboboxItemService"
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
</script>

<template>
  <div class="mb-32 flex flex-col bg-blue-200 py-24">
    <!-- TODO: remove mb-16 from InputField to geive generel gap-20 here  -->
    <div
      class="m-32 grid grid-flow-col grid-cols-[180px_auto_180px_auto] grid-rows-3 gap-x-20 gap-y-4"
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
        <InputField id="court" label="Gericht" visually-hide-label>
          <ComboboxInput
            id="court"
            v-model="searchEntry.court"
            aria-label="Gericht Suche"
            clear-on-choosing-item
            :item-service="ComboboxItemService.getCourts"
            placeholder="Gerichtstyp Gerichtsort"
          ></ComboboxInput>
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

      <!-- <div class="pr-32">
        <InputField
          id="hasError"
          v-slot="{ id }"
          label="Nur fehlerhafte Dokumentationseinheiten"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="undefined"
            aria-label="Nur fehlerhafte Dokumentationseinheiten"
            disabled
          />
        </InputField>
      </div> -->
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
