<script lang="ts" setup>
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import InputGroup from "@/components/InputGroup.vue"
import SaveButton from "@/components/SaveButton.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { Norm } from "@/domain/Norm"
import { normFrameFields } from "@/domain/normFrameFields"
import { editNormFrame } from "@/services/normsService"

interface Props {
  norm: Norm
}

interface Emits {
  (event: "fetchNorm"): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const frameData = computed({
  get: () => ({
    longTitle: props.norm.longTitle,
    officialShortTitle: props.norm.officialShortTitle,
    officialAbbreviation: props.norm.officialAbbreviation,
    referenceNumber: props.norm.referenceNumber,
    publicationDate: props.norm.publicationDate,
    announcementDate: props.norm.announcementDate,
    citationDate: props.norm.citationDate,
    frameKeywords: props.norm.frameKeywords,
    authorEntity: props.norm.authorEntity,
    authorDecidingBody: props.norm.authorDecidingBody,
    authorIsResolutionMajority: props.norm.authorIsResolutionMajority,
    leadJurisdiction: props.norm.leadJurisdiction,
    leadUnit: props.norm.leadUnit,
    participationType: props.norm.participationType,
    participationInstitution: props.norm.participationInstitution,
    documentTypeName: props.norm.documentTypeName,
    documentNormCategory: props.norm.documentNormCategory,
    documentTemplateName: props.norm.documentTemplateName,
    subjectFna: props.norm.subjectFna,
    subjectPreviousFna: props.norm.subjectPreviousFna,
    subjectGesta: props.norm.subjectGesta,
    subjectBgb3: props.norm.subjectBgb3,
  }),
  set: (data) => console.log(data),
})

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)
</script>

<template>
  <div>
    <h1 id="headlines" class="heading-02-regular mb-[1rem]">Überschriften</h1>

    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normFrameFields"
    />
    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="() => editNormFrame(props.norm.guid, frameData)"
      @fetch-norm="emit('fetchNorm')"
    />
  </div>
</template>
