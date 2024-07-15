<script lang="ts" setup>
import { onMounted, ref } from "vue"
import HandoverView from "@/components/HandoverView.vue"
import DocumentUnit from "@/domain/documentUnit"
import EventRecord from "@/domain/eventRecord"
import handoverService from "@/services/handoverService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()

const emits = defineEmits<{
  requestDocumentUnitFromServer: []
}>()

const loadDone = ref(false)
const eventLog = ref<EventRecord[]>()
const handoverResult = ref<EventRecord>()
const errorMessage = ref<ResponseError>()
const succeedMessage = ref<{ title: string; description: string }>()

async function handoverDocument() {
  const response = await handoverService.handoverDocument(
    props.documentUnit.uuid,
  )
  handoverResult.value = response.data
  if (!eventLog.value) eventLog.value = []
  if (response.data && response.data?.success) {
    const handover = response.data
    handover.date = formatDate(handover.date)
    handover.xml = handover.xml ? handover.xml.replace(/[ \t]{2,}/g, "") : ""

    eventLog.value.unshift(handover)

    succeedMessage.value = {
      title: "Email wurde versendet",
      description: "",
    }
  } else {
    errorMessage.value = response.error
  }
  emits("requestDocumentUnitFromServer")
}

function formatDate(date?: string): string {
  if (!date) {
    return ""
  }

  const handoverDate = new Date(date)
  const fullYear = handoverDate.getFullYear()
  const fullMonth = ("0" + (handoverDate.getMonth() + 1)).slice(-2)
  const fullDate = ("0" + handoverDate.getDate()).slice(-2)
  const fullHour = ("0" + handoverDate.getHours()).slice(-2)
  const fullMinute = ("0" + handoverDate.getMinutes()).slice(-2)

  return `${fullDate}.${fullMonth}.${fullYear} um ${fullHour}:${fullMinute} Uhr`
}

onMounted(async () => {
  const response = await handoverService.getEventLog(props.documentUnit.uuid)
  if (!response.error && response.data) {
    eventLog.value = response.data
    for (const item of eventLog.value) {
      item.date = formatDate(item.date)
      item.xml = item.xml ? item.xml : ""
    }
  } else {
    errorMessage.value = response.error
  }

  loadDone.value = true
})
</script>

<template>
  <div class="w-full grow p-24">
    <HandoverView
      v-if="loadDone"
      :document-unit="documentUnit"
      :error-message="errorMessage"
      :event-log="eventLog"
      :handover-result="handoverResult"
      :succeed-message="succeedMessage"
      @handover-document="handoverDocument"
    />

    <div v-else class="spinner">
      <h2>Überprüfung der Daten ...</h2>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.spinner {
  display: flex;
  height: 50vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
@/services/handoverService @/domain/eventRecord
