<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FieldOfLawNodeView from "@/components/preview/FieldOfLawNodeView.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"

import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"

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
          :key="activeCitation.id"
        >
          {{ activeCitation.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasCollectiveAgreements">
      <PreviewCategory>Tarifvertrag</PreviewCategory>
      <PreviewContent data-testid="Tarifvertrag">
        <div
          v-for="collectiveAgreement in props.contentRelatedIndexing
            .collectiveAgreements"
          :key="collectiveAgreement"
        >
          {{ collectiveAgreement }}
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
            class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            :href="foreignLanguageVersion.link"
            rel="noopener noreferrer"
            target="_blank"
          >
            {{ foreignLanguageVersion.link }}
          </a>
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasLegislativeMandate">
      <PreviewCategory>Gesetzgebungsauftrag</PreviewCategory>
      <PreviewContent>Ja</PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
