<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocUnitDetail from "./index.vue"
import PublicationDocument from "@/components/PublicationDocument.vue"
import docUnitService from "@/services/docUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  await docUnitService.getByDocumentNumber(props.documentNumber)
)
const loadDone = ref(false)
const xml = ref("")
const issues = ref<Array<string>>([])
const isFristTimePublication = ref(true)
const hasValidationError = ref(false)

const receiverEmail = ref("")
const emailSubject = ref("")
const lastPublicationDate = ref("")

const publishADocument = async () => {
  const respone = await fileService.publishADocument(docUnit.value.uuid)
  loadEmailToJurisInfos(respone)
}

const loadEmailToJurisInfos = (publishedXML: {
  xml: string
  statusMessages: Array<string>
  statusCode: string
  mailSubject: string
  publishDate: string
}) => {
  hasValidationError.value = publishedXML.statusCode === "400"
  issues.value = hasValidationError.value ? publishedXML.statusMessages : []
  if (!hasValidationError.value) {
    lastPublicationDate.value = formattedDate(publishedXML.publishDate)
    emailSubject.value = publishedXML.mailSubject
    receiverEmail.value = "dokmbx@juris.de"
    xml.value = publishedXML.xml ? publishedXML.xml.replace(/( )+/g, "") : ""
    isFristTimePublication.value = !(xml.value !== null && xml.value.length > 0)
  }
}

// const validateErrorMessages = (issues: Array<string>): Array<string> => {
//   return [
//     ...new Set(
//       issues.map((issue) => {
//         const invalidElement = issue.toString().split('"')[1]
//         return (
//           invalidElement.charAt(0).toUpperCase() +
//           invalidElement.toLowerCase().slice(1)
//         )
//       })
//     ),
//   ]
// }

const formattedDate = (date: string): string => {
  const publicationDate = new Date(date)
  const fullYear = publicationDate.getFullYear()
  const fullMonth = ("0" + publicationDate.getMonth()).slice(-2)
  const fullDate = ("0" + publicationDate.getDate()).slice(-2)
  const fullHour = ("0" + publicationDate.getHours()).slice(-2)
  const fullMinute = ("0" + publicationDate.getMinutes()).slice(-2)
  return `${fullYear}-${fullMonth}-${fullDate} ${fullHour}: ${fullMinute} Uhr`
}

onMounted(async () => {
  const lastPublisedXML = await fileService.getLastPublishedXML(
    docUnit.value.uuid
  )
  if (lastPublisedXML.length === 0) {
    loadDone.value = true
    return
  }
  loadEmailToJurisInfos(lastPublisedXML)
  loadDone.value = true
})
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <PublicationDocument
      v-if="loadDone"
      :xml="xml"
      :issues="issues"
      :receiver-email="receiverEmail"
      :email-subject="emailSubject"
      :last-publication-date="lastPublicationDate"
      :is-frist-time-publication="isFristTimePublication"
      :has-validation-error="hasValidationError"
      @publish-a-document="publishADocument"
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
