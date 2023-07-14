<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import PublicationDocument from "@/components/PublicationDocument.vue"
import DocumentUnit from "@/domain/documentUnit"
import XmlMail from "@/domain/xmlMail"
import { ResponseError } from "@/services/httpClient"
import publishService from "@/services/publishService"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()

const emits = defineEmits<{
  updateDocumentUnit: []
}>()

const loadDone = ref(false)
const publicationLog = ref<XmlMail[]>()
const publishResult = ref<XmlMail>()
const errorMessage = ref<ResponseError>()
const succeedMessage = ref<{ title: string; description: string }>()

async function publishADocument() {
  const response = await publishService.publishDocument(props.documentUnit.uuid)
  publishResult.value = response.data
  if (!publicationLog.value) publicationLog.value = []
  if (response.data && Number(response.data?.statusCode) < 300) {
    const publication = response.data
    publication.date = formatDate(publication.date)
    publication.xml = publication.xml
      ? publication.xml.replace(/[ \t]{2,}/g, "")
      : ""

    publicationLog.value.unshift(publication)

    succeedMessage.value = {
      title: "Email wurde versendet",
      description: "",
    }
  } else {
    errorMessage.value = response.error
  }
  emits("updateDocumentUnit")
}

function formatDate(date?: string): string {
  if (!date) {
    return ""
  }

  const publicationDate = new Date(date)
  const fullYear = publicationDate.getFullYear()
  const fullMonth = ("0" + (publicationDate.getMonth() + 1)).slice(-2)
  const fullDate = ("0" + publicationDate.getDate()).slice(-2)
  const fullHour = ("0" + publicationDate.getHours()).slice(-2)
  const fullMinute = ("0" + publicationDate.getMinutes()).slice(-2)

  return `${fullDate}.${fullMonth}.${fullYear} um ${fullHour}:${fullMinute} Uhr`
}

onMounted(async () => {
  const response = await publishService.getPublicationLog(
    props.documentUnit.uuid,
  )
  if (response.data) {
    loadDone.value = true
    publicationLog.value = response.data
  }

  if (publicationLog.value) {
    for (const item of publicationLog.value) {
      item.date = formatDate(item.date)
      item.xml = item.xml ? item.xml : ""
    }
  }

  errorMessage.value = response.error
  loadDone.value = true
})
</script>

<template>
  <DocumentUnitWrapper :document-unit="documentUnit">
    <template #default="{ classes }">
      <div :class="classes">
        <PublicationDocument
          v-if="loadDone"
          :document-unit="documentUnit"
          :error-message="errorMessage"
          :publication-log="publicationLog"
          :publish-result="publishResult"
          :succeed-message="succeedMessage"
          @publish-a-document="publishADocument"
        />

        <div v-else class="spinner">
          <h2>Überprüfung der Daten ...</h2>
        </div>
      </div>
    </template>
  </DocumentUnitWrapper>
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
