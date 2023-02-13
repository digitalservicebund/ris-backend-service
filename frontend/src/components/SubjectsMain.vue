<script lang="ts" setup>
import { ref } from "vue"
import SubjectSelectionList from "./SubjectSelectionList.vue"
import SubjectTree from "./SubjectTree.vue"

type Subject = { id: string; text: string }

const selectedSubjects = ref<Subject[]>([
  {
    id: "ST-09-01",
    text: "Allgemeines (Steuersystem, Steuerreform, Harmonisierung, Geltungsbereich, Inland, Gemeinschaftsgebiet u.a.)",
  },
  {
    id: "ST-09-04-04",
    text: "Lieferung Sonderfälle (u.a. Kommissionsgeschäft, Tausch, Lieferung mittels elektronsicher Schnittstelle)",
  },
  {
    id: "GB-04-08-02",
    text: "Mitwirkungspflichten der Beteiligten",
  },
])

function handleRemoveFromList(index: number) {
  selectedSubjects.value.splice(index, 1)
}

function handleAddToList(id: string) {
  selectedSubjects.value.push({ id, text: id })
}
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-02-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <SubjectSelectionList
          :selected-subjects="selectedSubjects"
          @remove-from-list="handleRemoveFromList($event)"
        ></SubjectSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <SubjectTree @add-to-list="handleAddToList($event)"></SubjectTree>
      </div>
    </div>
  </div>
</template>
