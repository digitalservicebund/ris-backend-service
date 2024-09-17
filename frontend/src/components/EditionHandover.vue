<script lang="ts" setup>
import { onMounted, ref } from "vue"
import LoadingSpinner from "./LoadingSpinner.vue"
import HandoverEditionView from "@/components/HandoverEditionView.vue"
import EventRecord, { HandoverMail } from "@/domain/eventRecord"
import HandoverEditionService from "@/services/handoverEditionService"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"

const store = useEditionStore()

const loadDone = ref(false)
const eventLog = ref<EventRecord[]>()
const errorMessage = ref<ResponseError>()
const succeedMessage = ref<{ title: string; description: string }>()

async function handoverEdition() {
  const response = await HandoverEditionService.handoverEdition(
    store.edition!.id!,
  )
  if (!eventLog.value) eventLog.value = []
  if (response.data && response.data?.success) {
    const handover = new HandoverMail(response.data)
    handover.setContent(handover.getContent().replace(/[ \t]{2,}/g, ""))

    eventLog.value.unshift(handover)

    succeedMessage.value = {
      title: "Email wurde versendet",
      description: "",
    }
  } else {
    errorMessage.value = response.error
  }
}

onMounted(async () => {
  if (!store.edition) {
    return
  }
  const response = await HandoverEditionService.getEventLog(store.edition.id!)
  if (!response.error && response.data) {
    eventLog.value = response.data
  } else {
    errorMessage.value = response.error
  }

  loadDone.value = true
})
</script>

<template>
  <div class="w-full grow p-24">
    <HandoverEditionView
      v-if="loadDone"
      :error-message="errorMessage"
      :event-log="eventLog"
      :succeed-message="succeedMessage"
      @handover-edition="handoverEdition"
    />

    <div v-else class="my-112 grid justify-items-center bg-white bg-opacity-60">
      <h2>Überprüfung der Daten ...</h2>
      <LoadingSpinner />
    </div>
  </div>
</template>

@/services/handoverEditionService @/domain/eventRecord
