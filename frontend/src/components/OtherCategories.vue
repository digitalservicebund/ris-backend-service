<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import JobProfiles from "@/components/JobProfiles.vue"
import LegislativeMandate from "@/components/LegislativeMandate.vue"
import ParticipatingJudges from "@/components/ParticipatingJudges.vue"
import { useInjectCourtType } from "@/composables/useCourtType"
import constitutionalCourtTypes from "@/data/constitutionalCourtTypes.json"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const courtTypeRef = useInjectCourtType()

const hasJobProfiles = ref<boolean>(
  store.documentUnit?.contentRelatedIndexing?.jobProfiles
    ? store.documentUnit?.contentRelatedIndexing?.jobProfiles?.length > 0
    : false,
)
const hasLegislativeMandate = ref<boolean>(
  store.documentUnit?.contentRelatedIndexing?.hasLegislativeMandate == true,
)
const shouldDisplayLegislativeMandateCategory = computed(() => {
  return (
    constitutionalCourtTypes.items.includes(courtTypeRef.value) ||
    hasLegislativeMandate.value
  )
})
const hasParticipatingJudges = ref<boolean>(
  store.documentUnit?.contentRelatedIndexing?.participatingJudges
    ? store.documentUnit?.contentRelatedIndexing?.participatingJudges?.length >
        0
    : false,
)
</script>

<template>
  <div aria-label="Weitere Rubriken">
    <h2 class="ds-label-01-bold mb-16">Weitere Rubriken</h2>
    <div class="flex flex-col gap-24">
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
        label="Mitwirkende Richter"
        :should-show-button="!hasParticipatingJudges"
      >
        <ParticipatingJudges label="Mitwirkende Richter" />
      </CategoryWrapper>
    </div>
  </div>
</template>
