<script lang="ts" setup>
import { UUID } from "crypto"
import { useInterval } from "@vueuse/core"
import Button from "primevue/button"
import Message from "primevue/message"
import { computed, ref, watch } from "vue"
import { useRoute } from "vue-router"
import PeriodicalEditionReferenceInput from "./PeriodicalEditionReferenceInput.vue"
import PeriodicalEditionReferenceSummary from "./PeriodicalEditionReferenceSummary.vue"
import EditableList from "@/components/EditableList.vue"
import TitleElement from "@/components/TitleElement.vue"
import Reference from "@/domain/reference"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconAdd from "~icons/material-symbols/add"

const route = useRoute()
const store = useEditionStore()
const responseError = ref<ResponseError | undefined>()
const extraContentSidePanelStore = useExtraContentSidePanelStore()
const editableListRef = ref()

const loadEditionIntervalCounter = useInterval(10_000, {})

const references = computed({
  get: () => store.edition?.references ?? [],
  set: async (newValues) => {
    await saveReferences(newValues)
    extraContentSidePanelStore.togglePanel(false)
  },
})

async function saveReferences(references: Reference[]) {
  store.edition!.references = references
  const response = await store.saveEdition()
  if (response.error) {
    const message =
      "Fehler beim Speichern der Fundstellen. Bitte laden Sie die Seite neu."
    alert(message)
    responseError.value = {
      title: message,
    }
  } else
    store.edition!.references = response.data.references?.map(
      (decision) => new Reference({ ...decision }),
    )
}

async function addNewEntry() {
  await editableListRef.value.toggleNewEntry(true)
  const element = document.getElementById("reference-input")
  setTimeout(() => {
    if (!element) return
    const headerOffset = 80
    const scrollPosition =
      element.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({
      top: scrollPosition,
      behavior: "smooth",
    })
  })
}

/**
 * A watch to load document every x times, to make sure user has the latest version of references
 * which is critical for external changes
 */
watch(loadEditionIntervalCounter, async () => {
  const editionId = route.params.editionId as string

  if (editionId) {
    await store.loadEdition(editionId as UUID)
  }
})
</script>

<template>
  <div class="flex w-full p-24">
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement data-testid="references-title">Fundstellen</TitleElement>
      <div v-if="responseError">
        <Message severity="error">
          <p class="ris-body1-bold">{{ responseError.title }}</p>
          <p>{{ responseError.description }}</p>
        </Message>
      </div>
      <Button
        v-if="references.length > 3"
        aria-label="Weitere Angabe Top"
        label="Weitere Angabe"
        severity="secondary"
        size="small"
        @click="addNewEntry"
        ><template #icon> <IconAdd /> </template
      ></Button>
      <div aria-label="Fundstellen">
        <EditableList
          ref="editableListRef"
          v-model="references"
          :create-entry="() => new Reference()"
          :edit-component="PeriodicalEditionReferenceInput"
          :summary-component="PeriodicalEditionReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
