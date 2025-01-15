<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, watch } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import { PublicationState } from "@/domain/publicationStatus"
import Reference from "@/domain/reference"
import useSessionStore from "@/stores/sessionStore"

const props = defineProps<{
  data: Reference
}>()
const { user } = storeToRefs(useSessionStore())

const linkClickable = computed(() => {
  const docUnit = props.data.documentationUnit
  const userDocOffice = user.value?.documentationOffice?.abbreviation

  const isOwningDocOffice =
    docUnit?.documentationOffice?.abbreviation === userDocOffice
  const isCreatingDocOffice =
    docUnit?.creatingDocOffice?.abbreviation === userDocOffice

  const status = docUnit?.status?.publicationStatus
  const isPublishedOrInPublishing =
    status === PublicationState.PUBLISHED ||
    status === PublicationState.PUBLISHING
  const isPendingAndCreatingDocOffice =
    status === PublicationState.EXTERNAL_HANDOVER_PENDING && isCreatingDocOffice

  return (
    isOwningDocOffice ||
    isPublishedOrInPublishing ||
    isPendingAndCreatingDocOffice
  )
})

watch(
  linkClickable,
  () => {
    console.log(props.data)
  },
  { deep: true },
)
</script>

<template>
  <div class="w-full" data-testid="reference-list-summary">
    <div v-if="props.data.documentationUnit?.documentNumber">
      <div class="ds-label-01-bold" data-testid="citation-summary">
        {{ props.data.renderSummary }}
      </div>

      <DecisionSummary
        :display-mode="DisplayMode.SIDEPANEL"
        :document-number="props.data.documentationUnit.documentNumber"
        :link-clickable="linkClickable"
        :status="props.data.documentationUnit.status"
        :summary="props.data.documentationUnit.renderSummary"
      ></DecisionSummary>
    </div>
  </div>
</template>
