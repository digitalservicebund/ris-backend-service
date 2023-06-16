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
  (event: "updateDocumentUnit"): void
}>()

const loadDone = ref(false)
const lastPublishedXmlMail = ref<XmlMail>()
const publishResult = ref<XmlMail>()
const errorMessage = ref<ResponseError>()
const succeedMessage = ref<{ title: string; description: string }>()

async function publishADocument() {
  const response = await publishService.publishDocument(props.documentUnit.uuid)
  publishResult.value = response.data
  if (response.data && Number(response.data?.statusCode) < 300) {
    lastPublishedXmlMail.value = response.data
    lastPublishedXmlMail.value.publishDate = formatDate(
      lastPublishedXmlMail.value.publishDate
    )
    lastPublishedXmlMail.value.xml = lastPublishedXmlMail.value.xml
      ? lastPublishedXmlMail.value.xml.replace(/[ \t]{2,}/g, "")
      : ""
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
  const response = await publishService.getLastPublishedXML(
    props.documentUnit.uuid
  )
  if (!!response.error) {
    loadDone.value = true
    return
  }

  if (!!response.data) {
    lastPublishedXmlMail.value = response.data
  }

  if (!!lastPublishedXmlMail.value) {
    lastPublishedXmlMail.value.publishDate = formatDate(
      lastPublishedXmlMail.value.publishDate
    )
    lastPublishedXmlMail.value.xml = lastPublishedXmlMail.value.xml
      ? lastPublishedXmlMail.value.xml
      : ""
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
          :last-published-xml-mail="lastPublishedXmlMail"
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
