<script lang="ts" setup>
import Button from "primevue/button"
import { ref, watch } from "vue"
import { useRouter } from "vue-router"
import DocumentationOfficeSelector from "@/components/DocumentationOfficeSelector.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import DocumentUnit, {
  DocumentationUnitParameters,
} from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  parameters?: DocumentationUnitParameters
  validateRequiredInput: () => boolean
}>()

const emit = defineEmits<{
  createdDocumentationUnit: [value: DocumentUnit]
}>()

const router = useRouter()

/**
 * Reactive reference to store any response error when creating a new document unit
 */
const createNewFromSearchResponseError = ref<ResponseError | undefined>()

/**
 * Reference to the currently selected responsible documentation office.
 * If passed through props, it initializes with the responsible office from the parameters.
 */
const selectedDocumentationOffice = ref<DocumentationOffice | undefined>(
  props.parameters?.court?.responsibleDocOffice,
)

/**
 * Handles creating a new documentation unit.
 * It first checks the validity of required inputs via `validateRequiredInput`.
 * If valid, it proceeds to create the document unit and optionally opens the newly created document.
 * @param {boolean} [openDocunit=false] - Whether to open the newly created documentation unit in a new tab.
 * @returns {Promise<void>}
 */
async function createNewFromSearch(openDocunit: boolean = false) {
  createNewFromSearchResponseError.value = undefined

  const isValid = props.validateRequiredInput()

  if (!isValid) {
    return
  }

  const createResponse = await documentUnitService.createNew({
    ...props.parameters,
    documentationOffice: selectedDocumentationOffice.value,
  })
  if (createResponse.error) {
    createNewFromSearchResponseError.value = createResponse.error
    return
  }

  if (openDocunit) {
    const routeData = router.resolve({
      name: "caselaw-documentUnit-documentNumber-categories",
      params: { documentNumber: createResponse.data.documentNumber },
    })
    window.open(routeData.href, "_blank")
  }

  emit("createdDocumentationUnit", createResponse.data)
}

/**
 * Watches for changes in the `parameters` prop and updates the local `docOffice` value accordingly.
 */
watch(
  () => props.parameters?.court,
  () => {
    selectedDocumentationOffice.value =
      props.parameters?.court?.responsibleDocOffice
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col gap-24 bg-blue-200 p-24">
    <div>
      <p class="ris-label1-bold">Keine passende Entscheidung gefunden?</p>
      <p>
        Übernehmen Sie die Stammdaten und erstellen Sie eine neue Entscheidung.
      </p>
    </div>
    <InputField
      id="responsibleDocOffice"
      label="Dokumentationsstelle zuweisen *"
    >
      <div class="w-1/2 flex-shrink flex-grow-0">
        <DocumentationOfficeSelector v-model="selectedDocumentationOffice" />
      </div>
    </InputField>
    <div class="flex flex-row gap-8">
      <Button
        aria-label="Dokumentationseinheit erstellen"
        :disabled="!selectedDocumentationOffice"
        label="Übernehmen"
        size="small"
        @click="() => createNewFromSearch()"
      >
      </Button>
      <Button
        aria-label="Dokumentationseinheit erstellen und direkt bearbeiten"
        :disabled="!selectedDocumentationOffice"
        label="Übernehmen und weiter bearbeiten"
        severity="secondary"
        size="small"
        @click="() => createNewFromSearch(true)"
      >
      </Button>
    </div>
  </div>

  <div v-if="createNewFromSearchResponseError">
    <InfoModal
      :description="createNewFromSearchResponseError.description"
      :title="createNewFromSearchResponseError.title"
    />
  </div>
</template>
