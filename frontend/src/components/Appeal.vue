<!-- eslint-disable vue/multi-word-component-names -->
<script lang="ts" setup>
import MultiSelect from "primevue/multiselect"
import InputSelect from "primevue/select"
import { computed, onMounted, ref } from "vue"
import InputField from "@/components/input/InputField.vue"
import { appealWithdrawalItems, pkhPlaintiffItems } from "@/domain/appeal"
import { AppealStatus } from "@/domain/appealStatus"
import { Appellant } from "@/domain/appellant"
import appealService from "@/services/appealService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()

const appellants = ref<Appellant[]>()
const appealStatuses = ref<AppealStatus[]>()

const selectedAppellants = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal.appellants
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      appellants: newValues,
    }
  },
})

const selectedRevisionDefendantStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal
          .revisionDefendantStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      revisionDefendantStatuses: newValues,
    }
  },
})

const selectedRevisionPlaintiffStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal
          .revisionPlaintiffStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      revisionPlaintiffStatuses: newValues,
    }
  },
})

const selectedJointRevisionDefendantStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal
          .jointRevisionDefendantStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      jointRevisionDefendantStatuses: newValues,
    }
  },
})

const selectedJointRevisionPlaintiffStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal
          .jointRevisionPlaintiffStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      jointRevisionPlaintiffStatuses: newValues,
    }
  },
})

const selectedNzbDefendantStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal.nzbDefendantStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      nzbDefendantStatuses: newValues,
    }
  },
})

const selectedNzbPlaintiffStatuses = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal
      ? store.documentUnit!.contentRelatedIndexing.appeal.nzbPlaintiffStatuses
      : [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      nzbPlaintiffStatuses: newValues,
    }
  },
})

const appealWithdrawal = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing.appeal?.appealWithdrawal,
  set: (newValue) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      appealWithdrawal: newValue,
    }
  },
})

const pkhPlaintiff = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.appeal?.pkhPlaintiff,
  set: (newValue) => {
    store.documentUnit!.contentRelatedIndexing.appeal = {
      ...store.documentUnit!.contentRelatedIndexing.appeal,
      pkhPlaintiff: newValue,
    }
  },
})

onMounted(async () => {
  const appellantsResponse = await appealService.getAppellants()
  if (appellantsResponse.error) {
    //todo
  } else {
    appellants.value = appellantsResponse.data
  }

  const appealStatusesResponse = await appealService.getAppealStatuses()
  if (appealStatusesResponse.error) {
    //todo
  } else {
    appealStatuses.value = appealStatusesResponse.data
  }
})
</script>

<template>
  <div :aria-label="label" class="mb-16 flex flex-col gap-24 bg-white">
    <div id="appeal" class="ris-label2-regular mb-16">
      {{ label }}
    </div>

    <InputField id="apellant" label="Rechtsmittelführer">
      <MultiSelect
        id="appellant"
        v-model="selectedAppellants"
        class="w-full"
        display="chip"
        option-label="value"
        :options="appellants"
        placeholder="Bitte auswählen"
      />
    </InputField>
    <div class="flex flex-row gap-24">
      <InputField id="revision-defendant" label="Revision (Beklagter)">
        <MultiSelect
          id="revision-defendant"
          v-model="selectedRevisionDefendantStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField id="revision-plaintiff" label="Revision (Kläger)">
        <MultiSelect
          id="revision-plaintiff"
          v-model="selectedRevisionPlaintiffStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </div>
    <div class="flex flex-row gap-24">
      <InputField
        id="joint-revision-defendant"
        label="Anschlussrevision (Beklagter)"
      >
        <MultiSelect
          id="joint-revision-defendant"
          v-model="selectedJointRevisionDefendantStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField
        id="joint-revision-plaintiff"
        label="Anschlussrevision (Kläger)"
      >
        <MultiSelect
          id="joint-revision-plaintiff"
          v-model="selectedJointRevisionPlaintiffStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </div>
    <div class="flex flex-row gap-24">
      <InputField id="nzb-defendant" label="NZB (Beklagter)">
        <MultiSelect
          id="nzb-defendant"
          v-model="selectedNzbDefendantStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField id="nzb-plaintiff" label="NZB (Kläger)">
        <MultiSelect
          id="nzb-plaintiff"
          v-model="selectedNzbPlaintiffStatuses"
          class="w-full"
          display="chip"
          option-label="value"
          :options="appealStatuses"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </div>
    <div class="flex flex-row gap-24">
      <InputField
        id="revision-withdrawal"
        v-slot="{ id }"
        label="Zurücknahme der Revision"
      >
        <InputSelect
          :id="id"
          v-model="appealWithdrawal"
          aria-label="Rechtskraft"
          fluid
          option-label="label"
          option-value="value"
          :options="appealWithdrawalItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField
        id="pkh-plaintiff"
        v-slot="{ id }"
        label="PKH-Antrag (Kläger)"
      >
        <InputSelect
          :id="id"
          v-model="pkhPlaintiff"
          fluid
          option-label="label"
          option-value="value"
          :options="pkhPlaintiffItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
    </div>
  </div>
</template>
