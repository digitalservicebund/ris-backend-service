<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FieldOfLawNodeView from "@/components/preview/FieldOfLawNodeView.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const contentRelatedIndexing = computed({
  get: () => store.documentUnit!.contentRelatedIndexing,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing = newValues
  },
})

const hasKeywords = computed(() => {
  return (
    contentRelatedIndexing.value.keywords &&
    contentRelatedIndexing.value.keywords?.length > 0
  )
})

const hasFieldsOfLaw = computed(() => {
  return (
    contentRelatedIndexing.value.fieldsOfLaw &&
    contentRelatedIndexing.value.fieldsOfLaw?.length > 0
  )
})

const hasNorms = computed(() => {
  return (
    contentRelatedIndexing.value.norms &&
    contentRelatedIndexing.value.norms?.length > 0
  )
})

const hasActiveCitations = computed(() => {
  return (
    contentRelatedIndexing.value.activeCitations &&
    contentRelatedIndexing.value.activeCitations?.length > 0
  )
})

const hasJobProfiles = computed(() => {
  return (
    contentRelatedIndexing.value.jobProfiles &&
    contentRelatedIndexing.value.jobProfiles?.length > 0
  )
})

const hasCollectiveAgreements = computed(() => {
  return (
    contentRelatedIndexing.value.collectiveAgreements &&
    contentRelatedIndexing.value.collectiveAgreements?.length > 0
  )
})

const hasDismissalGrounds = computed(() => {
  return (
    contentRelatedIndexing.value.dismissalGrounds &&
    contentRelatedIndexing.value.dismissalGrounds?.length > 0
  )
})

const hasDismissalTypes = computed(() => {
  return (
    contentRelatedIndexing.value.dismissalTypes &&
    contentRelatedIndexing.value.dismissalTypes?.length > 0
  )
})
const hasLegislativeMandate = computed(() => {
  return contentRelatedIndexing.value.hasLegislativeMandate
})
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <PreviewRow v-if="hasKeywords">
      <PreviewCategory>Schlagwörter</PreviewCategory>
      <PreviewContent>
        <div v-for="keyword in contentRelatedIndexing.keywords" :key="keyword">
          {{ keyword }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasFieldsOfLaw">
      <PreviewCategory>Sachgebiete</PreviewCategory>
      <PreviewContent>
        <div
          v-for="fieldOfLaw in contentRelatedIndexing.fieldsOfLaw"
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
        <div v-for="norm in contentRelatedIndexing.norms" :key="norm.id">
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
          v-for="activeCitation in contentRelatedIndexing.activeCitations"
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
          v-for="collectiveAgreement in contentRelatedIndexing.collectiveAgreements"
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
          v-for="dismissalType in contentRelatedIndexing.dismissalTypes"
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
          v-for="dismissalGround in contentRelatedIndexing.dismissalGrounds"
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
          v-for="jobProfile in contentRelatedIndexing.jobProfiles"
          :key="jobProfile"
        >
          {{ jobProfile }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="hasLegislativeMandate">
      <PreviewCategory>Gesetzgebungsauftrag</PreviewCategory>
      <PreviewContent>Ja</PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
