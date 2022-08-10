<script lang="ts" setup>
import { ref } from "vue"
import DocUnitDetail from "./index.vue"
import PublicationDocument from "@/components/PublicationDocument.vue"
import docUnitService from "@/services/docUnitService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  await docUnitService.getByDocumentNumber(props.documentNumber)
)
// const email = ref(await fileService.getEmailInfos(docUnit.value.uuid))
const xml = ref<string>(
  '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<juris-r>\n<metadaten>\n<gericht>\n<gertyp>Gerichtstyp</gertyp>\n<gerort>Gerichtssitz</gerort>\n</gericht>\n</metadaten>\n<textdaten>\n<titelzeile>\n<body>\n<div>\n<p>Titelzeile</p>\n</div>\n</body>\n</titelzeile>\n<leitsatz>\n<body>\n<div>\n<p>Leitsatz</p>\n</div>\n</body>\n</leitsatz>\n<osatz>\n<body>\n<div>\n<p>Orientierungssatz</p>\n</div>\n</body>\n</osatz>\n<tenor>\n<body>\n<div>\n<p>Tenor</p>\n</div>\n</body>\n</tenor>\n<tatbestand>\n<body>\n<div>\n<p>Tatbestand</p>\n<br/>\n</div>\n</body>\n</tatbestand>\n<entscheidungsgruende>\n<body>\n<div>\n<p>Entscheidungsgründe</p>\n</div>\n</body>\n</entscheidungsgruende>\n<gruende>\n<body>\n<div>\n<p>Gründe</p>\n</div>\n</body>\n</gruende>\n</textdaten>\n</juris-r>'
)
const issues = ref<Array<string>>([
  "Aktenzeichen",
  "Entscheidungsname",
  "Gericht",
])
const receiverEmail = ref<string>("dokmbx@juris.de")
const emailSubject = ref<string>('id=OVGNW name="knorr" da=r dt=b df=r')
const lastPublicationDate = ref<string>("24.07.2022 16:53 Uhr")
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <PublicationDocument
      :xml="xml"
      :issues="issues"
      :receiver-email="receiverEmail"
      :email-subject="emailSubject"
      :last-publication-date="lastPublicationDate"
    />
  </DocUnitDetail>
</template>
