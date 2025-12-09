<script lang="ts" setup>
import { computed } from "vue"
import AbuseFees from "./AbuseFees.vue"
import Appeal from "@/components/Appeal.vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import CollectiveAgreements from "@/components/CollectiveAgreements.vue"
import CountriesOfOrigin from "@/components/CountriesOfOrigin.vue"
import DefinitionList from "@/components/DefinitionList.vue"
import DismissalInputs from "@/components/DismissalInputs.vue"
import ForeignLanguageVersions from "@/components/ForeignLanguageVersions.vue"
import IncomeTypes from "@/components/IncomeTypes.vue"
import JobProfiles from "@/components/JobProfiles.vue"
import LegislativeMandate from "@/components/LegislativeMandate.vue"
import NonApplicationNorms from "@/components/norms/NonApplicationNorms.vue"
import ObjectValues from "@/components/ObjectValues.vue"
import OriginOfTranslations from "@/components/OriginOfTranslations.vue"
import PendingProceedings from "@/components/PendingProceedings.vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
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

const hasCountriesOfOrigin = computed(() => {
  return contentRelatedIndexing.value.countriesOfOrigin
    ? contentRelatedIndexing.value.countriesOfOrigin?.length > 0
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

const hasObjectValues = computed(() => {
  return contentRelatedIndexing.value.objectValues?.length
    ? contentRelatedIndexing.value.objectValues?.length > 0
    : false
})

const hasIncomeTypes = computed(() => {
  return contentRelatedIndexing.value.incomeTypes
    ? contentRelatedIndexing.value.incomeTypes?.length > 0
    : false
})

const hasAbuseFees = computed(() => {
  return contentRelatedIndexing.value.abuseFees?.length
    ? contentRelatedIndexing.value.abuseFees?.length > 0
    : false
})

const hasRelatedPendingProceedings = computed(() => {
  return contentRelatedIndexing.value.relatedPendingProceedings?.length
    ? contentRelatedIndexing.value.relatedPendingProceedings?.length > 0
    : false
})

const hasNonApplicationNorms = computed(() => {
  return contentRelatedIndexing.value.nonApplicationNorms?.length
    ? contentRelatedIndexing.value.nonApplicationNorms?.length > 0
    : false
})

const isConstitutionalCourt = computed(
  () =>
    store.documentUnit?.coreData.court?.jurisdictionType ===
    "Verfassungsgerichtsbarkeit",
)

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

const shouldDisplayIncomeType = computed(
  () => isFinanceCourt.value || hasIncomeTypes.value,
)

const shouldDisplayAbuseFees = computed(
  () => isConstitutionalCourt.value || hasAbuseFees.value,
)

const shouldDisplayLegislativeMandateCategory = computed(() => {
  return isConstitutionalCourt.value || hasLegislativeMandate.value
})

const shouldDisplayNonApplicationNorms = computed(
  () => isFinanceCourt.value || hasNonApplicationNorms.value,
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
        label="Tarifvertrag"
        :should-show-button="!hasCollectiveAgreement"
      >
        <CollectiveAgreements label="Tarifvertrag" />
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
      <CategoryWrapper
        label="Gegenstandswert"
        :should-show-button="!hasObjectValues"
      >
        <ObjectValues :label="contentRelatedIndexingLabels.objectValues" />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayAbuseFees"
        label="Missbrauchsgebühren"
        :should-show-button="!hasAbuseFees"
      >
        <AbuseFees :label="contentRelatedIndexingLabels.abuseFees" />
      </CategoryWrapper>
      <CategoryWrapper
        :label="contentRelatedIndexingLabels.countriesOfOrigin"
        :should-show-button="!hasCountriesOfOrigin"
      >
        <CountriesOfOrigin
          :label="contentRelatedIndexingLabels.countriesOfOrigin"
        />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayIncomeType"
        :label="contentRelatedIndexingLabels.incomeTypes"
        :should-show-button="!hasIncomeTypes"
      >
        <IncomeTypes :label="contentRelatedIndexingLabels.incomeTypes" />
      </CategoryWrapper>
      <CategoryWrapper
        :label="contentRelatedIndexingLabels.relatedPendingProceedings"
        :should-show-button="!hasRelatedPendingProceedings"
      >
        <PendingProceedings
          :label="contentRelatedIndexingLabels.relatedPendingProceedings"
        />
      </CategoryWrapper>
      <CategoryWrapper
        v-if="shouldDisplayNonApplicationNorms"
        :label="contentRelatedIndexingLabels.nonApplicationNorms"
        :should-show-button="!hasNonApplicationNorms"
      >
        <NonApplicationNorms />
      </CategoryWrapper>
    </div>
  </div>
</template>
