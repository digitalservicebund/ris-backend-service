<script lang="ts" setup>
import Message from "primevue/message"
import { computed } from "vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import EventRecord, {
  EventRecordType,
  HandoverMail,
} from "@/domain/eventRecord"
import { ResponseError } from "@/services/httpClient"
import IconKeyboardArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconKeyboardArrowUp from "~icons/ic/baseline-keyboard-arrow-up"

const props = defineProps<{
  eventLog?: EventRecord[]
  eventLogError?: ResponseError
}>()

const isFirstTimeHandover = computed(() => {
  return !props.eventLog || props.eventLog.length === 0
})

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
</script>

<template>
  <div aria-label="Letzte Ereignisse">
    <h2 class="ris-label1-bold mb-16">Letzte Ereignisse</h2>
    <Message
      v-if="eventLogError"
      aria-label="Fehler beim Laden des Event Logs"
      class="mt-8"
      severity="error"
    >
      <p class="ris-body1-bold">{{ eventLogError.title }}</p>
      <p>{{ eventLogError.description }}</p>
    </Message>
    <div v-else class="flex flex-col gap-24">
      <p v-if="isFirstTimeHandover">
        Diese Ausgabe wurde bisher nicht an die jDV übergeben
      </p>
      <div v-else class="flex flex-col gap-24">
        <div v-for="(item, index) in eventLog" :key="index">
          <ExpandableContent
            as-column
            class="border-b-1 border-gray-400 bg-white pb-10"
            :data-set="item"
            :header="getHeader(item)"
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
              class="pt-24"
              v-html="item.getContent()"
            />

            <div
              v-else-if="item.type == EventRecordType.HANDOVER"
              class="flex flex-col gap-24 pt-24"
            >
              <div class="ris-label2-regular">
                <div>
                  <span class="ris-label2-bold">E-Mail an:</span>
                  {{ (item as HandoverMail).receiverAddress }}
                </div>
                <div>
                  <span class="ris-label2-bold"> Betreff: </span>
                  {{ (item as HandoverMail).mailSubject }}
                </div>
              </div>

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
