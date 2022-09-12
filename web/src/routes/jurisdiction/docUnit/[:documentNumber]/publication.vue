<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocUnitDetail from "./index.vue"
import PublicationDocument from "@/components/PublicationDocument.vue"
import XmlMail from "@/domain/xmlMail"
import docUnitService from "@/services/docUnitService"
import publishService from "@/services/publishService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  (await docUnitService.getByDocumentNumber(props.documentNumber)).data
)
const loadDone = ref(false)
const lastPublishedXmlMail = ref<XmlMail>()
const publishResult = ref<XmlMail>()
const errorMessage = ref<{ title: string; description: string }>()

const publishADocument = async (email: string) => {
  const response = await publishService.publishADocument(
    docUnit.value.uuid,
    email
  )

  if (!!response.xmlMail) {
    publishResult.value = response.xmlMail
    if (response.xmlMail.statusCode === "200") {
      lastPublishedXmlMail.value = response.xmlMail

      lastPublishedXmlMail.value.publishDate = formatDate(
        lastPublishedXmlMail.value.publishDate
      )
      lastPublishedXmlMail.value.xml = lastPublishedXmlMail.value.xml
        ? lastPublishedXmlMail.value.xml.replace(/[ \t]{2,}/g, "")
        : ""
    }
  }

  errorMessage.value = response.errorMessage
}

const formatDate = (date?: string): string => {
  if (!date) {
    return ""
  }

  const publicationDate = new Date(date)
  const fullYear = publicationDate.getFullYear()
  const fullMonth = ("0" + (publicationDate.getMonth() + 1)).slice(-2)
  const fullDate = ("0" + publicationDate.getDate()).slice(-2)
  const fullHour = ("0" + publicationDate.getHours()).slice(-2)
  const fullMinute = ("0" + publicationDate.getMinutes()).slice(-2)

  return `${fullYear}-${fullMonth}-${fullDate} ${fullHour}: ${fullMinute} Uhr`
}

onMounted(async () => {
  const response = await publishService.getLastPublishedXML(docUnit.value.uuid)
  if (!!response.errorMessage) {
    loadDone.value = true
    return
  }

  if (!!response.xmlMail) {
    lastPublishedXmlMail.value = response.xmlMail
  }

  if (!!lastPublishedXmlMail.value) {
    lastPublishedXmlMail.value.publishDate = formatDate(
      lastPublishedXmlMail.value.publishDate
    )
    lastPublishedXmlMail.value.xml = lastPublishedXmlMail.value.xml
      ? lastPublishedXmlMail.value.xml.replace(/[ \t]{2,}/g, "")
      : ""
  }

  errorMessage.value = response.errorMessage

  loadDone.value = true
})
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <PublicationDocument
      v-if="loadDone"
      :publish-result="publishResult"
      :last-published-xml-mail="lastPublishedXmlMail"
      :error-message="errorMessage"
      @publish-a-document="publishADocument($event)"
    />
    <div v-else class="spinner">
      <h2>Überprüfung der Daten ...</h2>
    </div>
  </DocUnitDetail>
</template>

<style lang="scss" scoped>
.spinner {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 50vh;
}
</style>
