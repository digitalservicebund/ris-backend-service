<script lang="ts" setup>
import { ref, computed, onMounted } from "vue"
import ExpandableContent from "./ExpandableContent.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import EventRecord, {
  EventRecordType,
  HandoverMail,
  Preview,
} from "@/domain/eventRecord"
import FeatureToggleService from "@/services/featureToggleService"
import HandoverEditionService from "@/services/handoverEditionService"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconKeyboardArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconKeyboardArrowUp from "~icons/ic/baseline-keyboard-arrow-up"
import IconHandover from "~icons/ic/outline-campaign"

const props = defineProps<{
  eventLog?: EventRecord[]
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  handoverEdition: []
}>()

const store = useEditionStore()

const isFirstTimeHandover = computed(() => {
  return !props.eventLog || props.eventLog.length === 0
})

const preview = ref<Preview[]>()
const frontendError = ref()
const previewError = ref()
const featureToggle = ref()
const errorMessage = computed(
  () => frontendError.value ?? previewError.value ?? props.errorMessage,
)

onMounted(async () => {
  featureToggle.value = (
    await FeatureToggleService.isEnabled("neuris.evaluation-handover")
  ).data
  if (!numberOfReferences.value || !store.edition) return
  const previewResponse = await HandoverEditionService.getPreview(
    store.edition.id!,
  )
  if (previewResponse.error) {
    previewError.value = previewResponse.error
  } else if (previewResponse.data) {
    preview.value = previewResponse.data
  }
})

function handoverEdition() {
  if (!numberOfReferences.value) {
    frontendError.value = {
      title: "Es sind noch keine Fundstellen vermerkt.",
      description: "Die Ausgabe kann nicht übergeben werden.",
    }
  } else {
    emits("handoverEdition")
  }
}

function getHeader(item: EventRecord) {
  switch (item.type) {
    case EventRecordType.HANDOVER_REPORT:
      return "Juris Protokoll - " + item.getDate()
    case EventRecordType.HANDOVER:
      return "Xml Email Abgabe - " + item.getDate()
    default:
      return "Unbekanntes Ereignis - " + item.getDate()
  }
}

const numberOfReferences = computed(() => {
  return store.edition?.references?.length
})
</script>

<template>
  <div
    v-if="store.edition"
    class="flex-start flex max-w-[80rem] flex-col justify-start gap-40"
  >
    <h1 class="ds-heading-02-reg" data-testid="handover-title">
      Übergabe an jDV
    </h1>
    <div aria-label="Datenprüfung" class="flex flex-row">
      <div v-if="!numberOfReferences" class="flex flex-row items-center gap-8">
        <IconErrorOutline class="text-red-800" />
        <p class="flexds-body-01-reg">
          Es wurden noch keine Fundstellen hinzugefügt
        </p>
      </div>
      <div v-else class="flex flex-row items-center gap-8">
        <IconCheck class="text-green-700" />
        <p class="ds-body-01-reg">
          Die Ausgabe enthält {{ numberOfReferences }} Fundstellen
        </p>
      </div>
    </div>

    <ExpandableContent
      v-if="
        numberOfReferences &&
        numberOfReferences > 0 &&
        preview &&
        preview.length > 0
      "
      as-column
      class="border-b-1 border-r-1 border-gray-400 bg-white p-10"
      :data-set="preview"
      header="XML Vorschau"
      header-class="font-bold"
      :is-expanded="false"
      title="XML Vorschau"
    >
      <CodeSnippet
        v-for="(item, index) in preview"
        :key="index"
        class="mb-16"
        :title="item.fileName!"
        :xml="item.xml!"
      />
    </ExpandableContent>
    <InfoModal
      v-if="errorMessage"
      aria-label="Fehler bei jDV Übergabe"
      class="mt-8"
      :description="errorMessage.description"
      :title="errorMessage.title"
    />
    <InfoModal
      v-else-if="succeedMessage"
      aria-label="Erfolg der jDV Übergabe"
      class="mt-8"
      v-bind="succeedMessage"
      :status="InfoStatus.SUCCEED"
    />
    <TextButton
      v-if="featureToggle"
      aria-label="Fundstellen der Ausgabe an jDV übergeben"
      button-type="secondary"
      class="w-fit"
      :icon="IconHandover"
      label="Fundstellen der Ausgabe an jDV übergeben"
      @click="handoverEdition"
    />
    <div aria-label="Letzte Ereignisse" class="flex flex-col gap-24">
      <h2 class="ds-heading-03-reg">Letzte Ereignisse</h2>
      <p v-if="isFirstTimeHandover">
        Diese Ausgabe wurde bisher nicht an die jDV übergeben
      </p>
      <div v-else class="flex flex-col gap-24">
        <div v-for="(item, index) in eventLog" :key="index">
          <ExpandableContent
            as-column
            class="border-b-1 border-r-1 border-gray-400 bg-white p-10"
            :data-set="item"
            :header="getHeader(item)"
            header-class="font-bold"
            :is-expanded="index == 0"
            :title="item.type"
          >
            <template #open-icon>
              <IconKeyboardArrowDown />
            </template>

            <template #close-icon>
              <IconKeyboardArrowUp />
            </template>

            <!-- eslint-disable vue/no-v-html -->
            <div
              v-if="item.type == EventRecordType.HANDOVER_REPORT"
              class="p-20"
              v-html="item.getContent()"
            />
            <div v-else-if="item.type == EventRecordType.HANDOVER">
              <div class="ds-label-section pt-20 text-gray-900">ÜBER</div>
              <div class="ds-label-02-reg">
                <div>
                  <span class="ds-label-02-bold">E-Mail an:</span>
                  {{ (item as HandoverMail).receiverAddress }}
                </div>
                <div>
                  <span class="ds-label-02-bold"> Betreff: </span>
                  {{ (item as HandoverMail).mailSubject }}
                </div>
              </div>
              <div class="ds-label-section text-gray-900">ALS</div>
              <CodeSnippet
                v-for="(attachment, attachmentIndex) in (item as HandoverMail)
                  .attachments"
                :key="attachmentIndex"
                class="mb-16"
                :title="attachment.fileName!"
                :xml="attachment.fileContent!"
              />
            </div>
          </ExpandableContent>
        </div>
      </div>
    </div>
  </div>
</template>
@/services/handoverDocumentationUnitService @/domain/eventRecord
