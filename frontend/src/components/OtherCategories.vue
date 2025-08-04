<script lang="ts" setup>
import { computed } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import CollectiveAgreements from "@/components/CollectiveAgreements.vue"
import DefinitionList from "@/components/DefinitionList.vue"
import DismissalInputs from "@/components/DismissalInputs.vue"
import JobProfiles from "@/components/JobProfiles.vue"
import LegislativeMandate from "@/components/LegislativeMandate.vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
import constitutionalCourtTypes from "@/data/constitutionalCourtTypes.json"
import laborCourtTypes from "@/data/laborCourtTypes.json"
import { contentRelatedIndexingLabels } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const contentRelatedIndexing = computed({
  get: () => store.documentUnit!.contentRelatedIndexing,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing = newValues
  },
})

const hasCollectiveAgreement = computed<boolean>(
  () =>
    !!contentRelatedIndexing.value.collectiveAgreements &&
    contentRelatedIndexing.value.collectiveAgreements?.length > 0,
)
const hasDismissalInput = computed<boolean>(
  () =>
    (!!contentRelatedIndexing.value.dismissalTypes &&
      contentRelatedIndexing.value.dismissalTypes?.length > 0) ||
    (!!contentRelatedIndexing.value.dismissalGrounds &&
      contentRelatedIndexing.value.dismissalGrounds?.length > 0),
)
const hasJobProfiles = computed<boolean>(() =>
  contentRelatedIndexing.value.jobProfiles
    ? contentRelatedIndexing.value.jobProfiles?.length > 0
    : false,
)
const hasLegislativeMandate = computed(() => {
  return contentRelatedIndexing.value.hasLegislativeMandate
})
const evsf = computed({
  get: () => contentRelatedIndexing.value.evsf,
  set: (value) => (store.documentUnit!.contentRelatedIndexing.evsf = value),
})

const shouldDisplayLegislativeMandateCategory = computed(() => {
  return (
    constitutionalCourtTypes.items.includes(
      store.documentUnit?.coreData.court?.type ?? "",
    ) || hasLegislativeMandate.value
  )
})

const isLaborCourt = computed(() =>
  laborCourtTypes.items.includes(
    store.documentUnit?.coreData.court?.type ?? "",
  ),
)

const isFinanceCourt = computed(() =>
  ["BFH", "FG", "OFH", "RFH"].includes(
    store.documentUnit?.coreData.court?.type ?? "",
  ),
)

const shouldDisplayDismissalAttributes = computed(
  () => isLaborCourt.value || hasDismissalInput.value,
)

const shouldDisplayCollectiveAgreements = computed(
  () => isLaborCourt.value || hasCollectiveAgreement.value,
)

const shouldDisplayEvsf = computed(() => isFinanceCourt.value || evsf.value)
</script>

<template>
  <div aria-label="Weitere Rubriken">
    <h2 class="ris-label1-bold mb-16">Weitere Rubriken</h2>
    <div class="flex flex-col gap-24">
      <CategoryWrapper
        label="Definition"
        :should-show-button="!contentRelatedIndexing.definitions?.length"
      >
        <DefinitionList label="Definition" />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayCollectiveAgreements"
        v-slot="slotProps"
        label="Tarifvertrag"
        :should-show-button="!hasCollectiveAgreement"
      >
        <CollectiveAgreements label="Tarifvertrag" @reset="slotProps.reset" />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayDismissalAttributes"
        label="Kündigung"
        :should-show-button="!hasDismissalInput"
      >
        <DismissalInputs />
      </CategoryWrapper>
      <CategoryWrapper label="Berufsbild" :should-show-button="!hasJobProfiles">
        <JobProfiles label="Berufsbild" />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayLegislativeMandateCategory"
        label="Gesetzgebungsauftrag"
        :should-show-button="!hasLegislativeMandate"
      >
        <LegislativeMandate
          headline="Gesetzgebungsauftrag"
          label="Gesetzgebungsauftrag vorhanden"
        />
      </CategoryWrapper>
      <TextInputCategory
        v-if="shouldDisplayEvsf"
        id="evsf"
        v-model="evsf"
        :data-testid="contentRelatedIndexingLabels.evsf"
        editable
        :label="contentRelatedIndexingLabels.evsf"
        :should-show-button="!evsf"
      />
    </div>
  </div>
</template>
