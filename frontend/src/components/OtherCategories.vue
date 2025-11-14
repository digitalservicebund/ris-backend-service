<script lang="ts" setup>
import { computed } from "vue"
import Appeal from "@/components/Appeal.vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import CollectiveAgreements from "@/components/CollectiveAgreements.vue"
import DefinitionList from "@/components/DefinitionList.vue"
import DismissalInputs from "@/components/DismissalInputs.vue"
import ForeignLanguageVersions from "@/components/ForeignLanguageVersions.vue"
import JobProfiles from "@/components/JobProfiles.vue"
import LegislativeMandate from "@/components/LegislativeMandate.vue"
import OriginOfTranslations from "@/components/OriginOfTranslations.vue"
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

const hasForeignLanguageVersion = computed(() => {
  return contentRelatedIndexing.value.foreignLanguageVersions
    ? contentRelatedIndexing.value.foreignLanguageVersions?.length > 0
    : false
})

const hasOriginOfTranslations = computed(() => {
  return contentRelatedIndexing.value.originOfTranslations
    ? contentRelatedIndexing.value.originOfTranslations?.length > 0
    : false
})

const hasAppeal = computed(() => {
  return (
    contentRelatedIndexing.value.appeal?.appellants?.length ||
    contentRelatedIndexing.value.appeal?.revisionDefendantStatuses?.length ||
    contentRelatedIndexing.value.appeal?.revisionPlaintiffStatuses?.length ||
    contentRelatedIndexing.value.appeal?.jointRevisionDefendantStatuses
      ?.length ||
    contentRelatedIndexing.value.appeal?.jointRevisionPlaintiffStatuses
      ?.length ||
    contentRelatedIndexing.value.appeal?.nzbDefendantStatuses?.length ||
    contentRelatedIndexing.value.appeal?.nzbPlaintiffStatuses?.length ||
    contentRelatedIndexing.value.appeal?.appealWithdrawal ||
    contentRelatedIndexing.value.appeal?.pkhPlaintiff
  )
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

const isFinanceCourt = computed(
  () =>
    store.documentUnit?.coreData.court?.jurisdictionType ===
    "Finanzgerichtsbarkeit",
)

const shouldDisplayDismissalAttributes = computed(
  () => isLaborCourt.value || hasDismissalInput.value,
)

const shouldDisplayCollectiveAgreements = computed(
  () => isLaborCourt.value || hasCollectiveAgreement.value,
)

const shouldDisplayEvsf = computed(() => isFinanceCourt.value || evsf.value)

const shouldDisplayAppeal = computed(
  () => isFinanceCourt.value || hasAppeal.value,
)
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
      <CategoryWrapper
        label="Fremdsprachige Fassung"
        :should-show-button="!hasForeignLanguageVersion"
      >
        <ForeignLanguageVersions label="Fremdsprachige Fassung" />
      </CategoryWrapper>
      <CategoryWrapper
        label="Herkunft der Übersetzung"
        :should-show-button="!hasOriginOfTranslations"
      >
        <OriginOfTranslations label="Herkunft der Übersetzung" />
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
      <CategoryWrapper
        v-if="shouldDisplayAppeal"
        :label="contentRelatedIndexingLabels.appeal"
        :should-show-button="!hasAppeal"
      >
        <Appeal :label="contentRelatedIndexingLabels.appeal" />
      </CategoryWrapper>
    </div>
  </div>
</template>
