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

const validateErrorMessages = (issues: Array<string>): Array<string> => {
  return [
    ...new Set(
      issues.map((issue) => {
        const invalidElement = issue.toString().split('"')[1]
        return (
          invalidElement.charAt(0).toUpperCase() +
          invalidElement.toLowerCase().slice(1)
        )
      })
    ),
  ]
}

const formattedDate = (date: string): string => {
  const publicationDate = new Date(date)
  const fullYear = publicationDate.getFullYear()
  const fullMonth = ("0" + publicationDate.getMonth()).slice(-2)
  const fullDate = ("0" + publicationDate.getDate()).slice(-2)
  const fullHour = ("0" + publicationDate.getHours()).slice(-2)
  const fullMinute = ("0" + publicationDate.getMinutes()).slice(-2)
  return `${fullYear}-${fullMonth}-${fullDate} ${fullHour}: ${fullMinute} Uhr`
}

const loadDone = ref<boolean>(false)
const xml = ref<string>("")
const issues = ref<Array<string>>([])

const receiverEmail = ref<string>("")
const emailSubject = ref<string>("")
const lastPublicationDate = ref<string>("")

onMounted(async () => {
  const emailInfos = await fileService.getEmailInfos(docUnit.value.uuid)
  lastPublicationDate.value = formattedDate(emailInfos.publishDate)
  emailSubject.value = emailInfos.mailSubject
  receiverEmail.value = "dokmbx@juris.de"
  issues.value =
    emailInfos.statusCode === "200"
      ? []
      : validateErrorMessages(emailInfos.statusMessages)
  const xmlText: string = emailInfos.xml ? emailInfos.xml : ""
  xml.value = xmlText.replaceAll("  ", "")
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
