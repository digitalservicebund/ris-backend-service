<script setup lang="ts">
import { computed } from "vue"
import BorderNumberLinkView from "@/components/BorderNumberLinkView.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import IncomeTypeSummaryBasic from "@/components/IncomeTypeSummaryBasic.vue"
import OriginOfTranslationSummaryPreview from "@/components/OriginOfTranslationSummaryPreview.vue"
import FieldOfLawNodeView from "@/components/preview/FieldOfLawNodeView.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"

import { appealWithdrawalItems, pkhPlaintiffItems } from "@/domain/appeal"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { contentRelatedIndexingLabels } from "@/domain/decision"

const props = defineProps<{
  contentRelatedIndexing: ContentRelatedIndexing
}>()

const hasKeywords = computed(() => {
  return (
    props.contentRelatedIndexing.keywords &&
    props.contentRelatedIndexing.keywords?.length > 0
  )
})

const hasFieldsOfLaw = computed(() => {
  return (
    props.contentRelatedIndexing.fieldsOfLaw &&
    props.contentRelatedIndexing.fieldsOfLaw?.length > 0
  )
})

const hasNorms = computed(() => {
  return (
    props.contentRelatedIndexing.norms &&
    props.contentRelatedIndexing.norms?.length > 0
  )
})

const hasActiveCitations = computed(() => {
  return (
    props.contentRelatedIndexing.activeCitations &&
    props.contentRelatedIndexing.activeCitations?.length > 0
  )
})

const hasAppealAdmission = computed(() => {
  return props.contentRelatedIndexing.appealAdmission != null
})

const hasJobProfiles = computed(() => {
  return (
    props.contentRelatedIndexing.jobProfiles &&
    props.contentRelatedIndexing.jobProfiles?.length > 0
  )
})

const hasCollectiveAgreements = computed(() => {
  return (
    props.contentRelatedIndexing.collectiveAgreements &&
    props.contentRelatedIndexing.collectiveAgreements?.length > 0
  )
})

const hasDismissalGrounds = computed(() => {
  return (
    props.contentRelatedIndexing.dismissalGrounds &&
    props.contentRelatedIndexing.dismissalGrounds?.length > 0
  )
})

const hasDismissalTypes = computed(() => {
  return (
    props.contentRelatedIndexing.dismissalTypes &&
    props.contentRelatedIndexing.dismissalTypes?.length > 0
  )
})

const hasLegislativeMandate = computed(() => {
  return props.contentRelatedIndexing.hasLegislativeMandate
})

const hasForeignLanguageVersions = computed(() => {
  return (
    props.contentRelatedIndexing.foreignLanguageVersions &&
    props.contentRelatedIndexing.foreignLanguageVersions?.length > 0
  )
})

const hasOriginOfTranslations = computed(() => {
  return (
    props.contentRelatedIndexing.originOfTranslations &&
    props.contentRelatedIndexing.originOfTranslations?.length > 0
  )
})

const hasEvsf = computed(() => {
  return props.contentRelatedIndexing.evsf
})

const hasDefinitions = computed(() => {
  return !!props.contentRelatedIndexing.definitions?.length
})

const hasObjectValues = computed(() => {
  return (
    props.contentRelatedIndexing.objectValues &&
    props.contentRelatedIndexing.objectValues.length > 0
  )
})

const hasAbuseFees = computed(() => {
  return (
    props.contentRelatedIndexing.abuseFees &&
    props.contentRelatedIndexing.abuseFees.length > 0
  )
})

const hasAppeal = computed(() => {
  return (
    props.contentRelatedIndexing.appeal?.appellants?.length ||
    props.contentRelatedIndexing.appeal?.revisionDefendantStatuses?.length ||
    props.contentRelatedIndexing.appeal?.revisionPlaintiffStatuses?.length ||
    props.contentRelatedIndexing.appeal?.jointRevisionDefendantStatuses
      ?.length ||
    props.contentRelatedIndexing.appeal?.jointRevisionPlaintiffStatuses
      ?.length ||
    props.contentRelatedIndexing.appeal?.nzbDefendantStatuses?.length ||
    props.contentRelatedIndexing.appeal?.nzbPlaintiffStatuses?.length ||
    props.contentRelatedIndexing.appeal?.appealWithdrawal ||
    props.contentRelatedIndexing.appeal?.pkhPlaintiff
  )
})

const hasCountriesOfOrigin = computed(() => {
  return (
    props.contentRelatedIndexing.countriesOfOrigin &&
    props.contentRelatedIndexing.countriesOfOrigin.length > 0
  )
})

const hasIncomeTypes = computed(() => {
  return (
    props.contentRelatedIndexing.incomeTypes &&
    props.contentRelatedIndexing.incomeTypes?.length > 0
  )
})

const hasRelatedPendingProceedings = computed(() => {
  return (
    props.contentRelatedIndexing.relatedPendingProceedings &&
    props.contentRelatedIndexing.relatedPendingProceedings?.length > 0
  )
})

const hasNonApplicationNorms = computed(() => {
  return (
    props.contentRelatedIndexing.nonApplicationNorms &&
    props.contentRelatedIndexing.nonApplicationNorms?.length > 0
  )
})
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <PreviewRow v-if="hasKeywords">
      <PreviewCategory>Schlagwörter</PreviewCategory>
      <PreviewContent>
        <div
          v-for="keyword in props.contentRelatedIndexing.keywords"
          :key="keyword"
        >
          {{ keyword }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasFieldsOfLaw">
      <PreviewCategory>Sachgebiete</PreviewCategory>
      <PreviewContent>
        <div
          v-for="fieldOfLaw in props.contentRelatedIndexing.fieldsOfLaw"
          :key="fieldOfLaw.identifier"
          class="flex flex-row"
        >
          <div class="min-w-[150px]">{{ fieldOfLaw.identifier }}</div>
          <div>
            <FieldOfLawNodeView :node="fieldOfLaw" />
          </div>
        </div>
      </PreviewContent>
    </PreviewRow>

    <PreviewRow v-if="hasNorms">
      <PreviewCategory>Normen</PreviewCategory>
      <PreviewContent>
        <div v-for="norm in props.contentRelatedIndexing.norms" :key="norm.id">
          <div v-if="norm.singleNorms && norm.singleNorms.length > 0">
            <div v-for="(singleNorm, i) in norm.singleNorms" :key="i">
              {{ norm.renderSummary }}
              {{
                singleNorm.renderSummary.length > 0
                  ? " - " + singleNorm.renderSummary
                  : ""
              }}
              {{
                singleNorm.legalForce ? " | " + singleNorm.renderLegalForce : ""
              }}
            </div>
          </div>
          <div v-else>
            {{ norm.renderSummary }}
          </div>
        </div>
      </PreviewContent>
    </PreviewRow>

    <PreviewRow v-if="hasActiveCitations">
      <PreviewCategory>Aktivzitierung</PreviewCategory>
      <PreviewContent>
        <div
          v-for="activeCitation in props.contentRelatedIndexing.activeCitations"
          :key="activeCitation.uuid"
        >
          {{ activeCitation.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasAppealAdmission">
      <PreviewCategory>Rechtsmittelzulassung</PreviewCategory>
      <PreviewContent>
        <div>
          <span
            v-if="
              props.contentRelatedIndexing.appealAdmission?.admitted === true
            "
          >
            <span v-if="props.contentRelatedIndexing.appealAdmission.by"
              >Ja, durch
              {{ props.contentRelatedIndexing.appealAdmission.by }}</span
            >
            <span v-else>Ja</span>
          </span>
          <span v-else>Nein</span>
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasCollectiveAgreements">
      <PreviewCategory>Tarifvertrag</PreviewCategory>
      <PreviewContent data-testid="Tarifvertrag">
        <div
          v-for="collectiveAgreement in props.contentRelatedIndexing
            .collectiveAgreements"
          :key="collectiveAgreement.id"
        >
          {{ new CollectiveAgreement(collectiveAgreement).renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasDismissalTypes">
      <PreviewCategory>Kündigungsarten</PreviewCategory>
      <PreviewContent data-testid="Kündigungsarten">
        <div
          v-for="dismissalType in props.contentRelatedIndexing.dismissalTypes"
          :key="dismissalType"
        >
          {{ dismissalType }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasDismissalGrounds">
      <PreviewCategory>Kündigungsgründe</PreviewCategory>
      <PreviewContent data-testid="Kündigungsgründe">
        <div
          v-for="dismissalGround in props.contentRelatedIndexing
            .dismissalGrounds"
          :key="dismissalGround"
        >
          {{ dismissalGround }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasJobProfiles">
      <PreviewCategory>Berufsbild</PreviewCategory>
      <PreviewContent>
        <div
          v-for="jobProfile in props.contentRelatedIndexing.jobProfiles"
          :key="jobProfile"
        >
          {{ jobProfile }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasForeignLanguageVersions">
      <PreviewCategory>Fremdsprachige Fassung</PreviewCategory>
      <PreviewContent data-testid="Fremdsprachige Fassung">
        <div
          v-for="foreignLanguageVersion in props.contentRelatedIndexing
            .foreignLanguageVersions"
          :key="foreignLanguageVersion.id"
        >
          {{ foreignLanguageVersion.languageCode?.label }}:
          <a
            v-if="foreignLanguageVersion.link"
            class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            :href="
              /^https?:\/\//i.test(foreignLanguageVersion.link)
                ? foreignLanguageVersion.link
                : `https://${foreignLanguageVersion.link}`
            "
            rel="noopener noreferrer"
            target="_blank"
          >
            {{ foreignLanguageVersion.link }}
          </a>
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasOriginOfTranslations">
      <PreviewCategory>{{
        contentRelatedIndexingLabels.originOfTranslations
      }}</PreviewCategory>
      <PreviewContent data-testid="Herkunft der Übersetzung">
        <span
          v-for="originOfTranslation in props.contentRelatedIndexing
            .originOfTranslations"
          :key="originOfTranslation.id"
          ><OriginOfTranslationSummaryPreview :data="originOfTranslation"
        /></span>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasLegislativeMandate">
      <PreviewCategory>Gesetzgebungsauftrag</PreviewCategory>
      <PreviewContent>Ja</PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasEvsf">
      <PreviewCategory>{{ contentRelatedIndexingLabels.evsf }}</PreviewCategory>
      <PreviewContent>{{ props.contentRelatedIndexing.evsf }}</PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasDefinitions">
      <PreviewCategory>Definition</PreviewCategory>
      <PreviewContent>
        <div
          v-for="definition in props.contentRelatedIndexing.definitions"
          :key="`${definition.definedTerm}|${definition.id}`"
        >
          <span>{{ definition?.definedTerm }}</span>
          <span v-if="definition?.definingBorderNumber">
            |
            <BorderNumberLinkView
              :border-number="definition.definingBorderNumber"
            />
          </span>
        </div>
      </PreviewContent>
    </PreviewRow>

    <div v-if="hasAppeal" class="pb-8">
      <PreviewCategory>Rechsmittel</PreviewCategory>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.appellants &&
          props.contentRelatedIndexing.appeal?.appellants.length > 0
        "
      >
        <PreviewCategory>Rechtsmittelführer</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.appellants
              .map((appellant) => appellant.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.revisionDefendantStatuses &&
          props.contentRelatedIndexing.appeal?.revisionDefendantStatuses
            .length > 0
        "
      >
        <PreviewCategory>Revision (Beklagter)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.revisionDefendantStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.revisionPlaintiffStatuses &&
          props.contentRelatedIndexing.appeal?.revisionPlaintiffStatuses
            .length > 0
        "
      >
        <PreviewCategory>Revision (Kläger)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.revisionPlaintiffStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.jointRevisionDefendantStatuses &&
          props.contentRelatedIndexing.appeal?.jointRevisionDefendantStatuses
            .length > 0
        "
      >
        <PreviewCategory>Anschlussrevision (Beklagter)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.jointRevisionDefendantStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.jointRevisionPlaintiffStatuses &&
          props.contentRelatedIndexing.appeal?.jointRevisionPlaintiffStatuses
            .length > 0
        "
      >
        <PreviewCategory>Anschlussrevision (Kläger)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.jointRevisionPlaintiffStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.nzbDefendantStatuses &&
          props.contentRelatedIndexing.appeal?.nzbDefendantStatuses.length > 0
        "
      >
        <PreviewCategory>NZB (Beklagter)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.nzbDefendantStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow
        v-if="
          props.contentRelatedIndexing.appeal?.nzbPlaintiffStatuses &&
          props.contentRelatedIndexing.appeal?.nzbPlaintiffStatuses.length > 0
        "
      >
        <PreviewCategory>NZB (Kläger)</PreviewCategory>
        <PreviewContent>
          <span>{{
            props.contentRelatedIndexing.appeal.nzbPlaintiffStatuses
              .map((it) => it.value)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="props.contentRelatedIndexing.appeal?.appealWithdrawal">
        <PreviewCategory>Zurücknahme der Revision</PreviewCategory>
        <PreviewContent>
          <span>{{
            appealWithdrawalItems
              .filter(
                (it) =>
                  it.value ===
                  props.contentRelatedIndexing.appeal?.appealWithdrawal,
              )
              .map((it) => it.label)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="props.contentRelatedIndexing.appeal?.pkhPlaintiff">
        <PreviewCategory>PKH-Antrag (Kläger)</PreviewCategory>
        <PreviewContent>
          <span>{{
            pkhPlaintiffItems
              .filter(
                (it) =>
                  it.value ===
                  props.contentRelatedIndexing.appeal?.pkhPlaintiff,
              )
              .map((it) => it.label)
              .join(", ")
          }}</span>
        </PreviewContent>
      </PreviewRow>
    </div>
    <PreviewRow v-if="hasObjectValues">
      <PreviewCategory>Gegenstandswert</PreviewCategory>
      <PreviewContent>
        <div
          v-for="objectValue in props.contentRelatedIndexing.objectValues"
          :key="objectValue.id"
        >
          {{ objectValue.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasAbuseFees">
      <PreviewCategory>Missbrauchsgebühren</PreviewCategory>
      <PreviewContent>
        <div
          v-for="abuseFee in props.contentRelatedIndexing.abuseFees"
          :key="abuseFee.id"
        >
          {{ abuseFee.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasCountriesOfOrigin">
      <PreviewCategory>Herkunftsland</PreviewCategory>
      <PreviewContent>
        <div
          v-for="countryOfOrigin in props.contentRelatedIndexing
            .countriesOfOrigin"
          :key="countryOfOrigin.id"
        >
          {{ countryOfOrigin.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasIncomeTypes">
      <PreviewCategory>{{
        contentRelatedIndexingLabels.incomeTypes
      }}</PreviewCategory>
      <PreviewContent data-testid="Einkunftsart">
        <span
          v-for="incomeType in props.contentRelatedIndexing.incomeTypes"
          :key="incomeType.id"
        >
          <IncomeTypeSummaryBasic :data="incomeType" />
        </span>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasRelatedPendingProceedings">
      <PreviewCategory>{{
        contentRelatedIndexingLabels.relatedPendingProceedings
      }}</PreviewCategory>
      <PreviewContent>
        <div
          v-for="relatedPendingProceeding in props.contentRelatedIndexing
            .relatedPendingProceedings"
          :key="relatedPendingProceeding.uuid"
        >
          {{ relatedPendingProceeding.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasNonApplicationNorms">
      <PreviewCategory>Nichtanwendungsgesetz</PreviewCategory>
      <PreviewContent>
        <div
          v-for="norm in props.contentRelatedIndexing.nonApplicationNorms"
          :key="norm.id"
        >
          <div v-if="norm.singleNorms && norm.singleNorms.length > 0">
            <div v-for="(singleNorm, i) in norm.singleNorms" :key="i">
              {{ norm.renderSummary }}
              {{
                singleNorm.renderSummary.length > 0
                  ? " - " + singleNorm.renderSummary
                  : ""
              }}
              {{
                singleNorm.legalForce ? " | " + singleNorm.renderLegalForce : ""
              }}
            </div>
          </div>
          <div v-else>
            {{ norm.renderSummary }}
          </div>
        </div>
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
