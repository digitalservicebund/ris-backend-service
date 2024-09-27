<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import CollectiveAgreements from "@/components/CollectiveAgreements.vue"
import DismissalInputs from "@/components/DismissalInputs.vue"
import JobProfiles from "@/components/JobProfiles.vue"
import LegislativeMandate from "@/components/LegislativeMandate.vue"
import { useInjectCourtType } from "@/composables/useCourtType"
import constitutionalCourtTypes from "@/data/constitutionalCourtTypes.json"
import laborCourtTypes from "@/data/laborCourtTypes.json"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const courtTypeRef = useInjectCourtType()

const hasCollectiveAgreement = ref<boolean>(
  !!store.documentUnit?.contentRelatedIndexing?.collectiveAgreements &&
    store.documentUnit?.contentRelatedIndexing?.collectiveAgreements?.length >
      0,
)
const hasDismissalInput = ref<boolean>(
  (!!store.documentUnit?.contentRelatedIndexing?.dismissalTypes &&
    store.documentUnit?.contentRelatedIndexing?.dismissalTypes?.length > 0) ||
    (!!store.documentUnit?.contentRelatedIndexing?.dismissalGrounds &&
      store.documentUnit?.contentRelatedIndexing?.dismissalGrounds?.length > 0),
)
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

const isLaborCourt = computed(() =>
  laborCourtTypes.items.includes(courtTypeRef.value),
)

const shouldDisplayDismissalAttributes = computed(
  () => isLaborCourt.value || hasDismissalInput.value,
)

const shouldDisplayCollectiveAgreements = computed(
  () => isLaborCourt.value || hasCollectiveAgreement.value,
)
</script>

<template>
  <div aria-label="Weitere Rubriken">
    <h2 class="ds-label-01-bold mb-16">Weitere Rubriken</h2>
    <div class="flex flex-col gap-24">
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
    </div>
  </div>
</template>
