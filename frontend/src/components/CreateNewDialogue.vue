<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import DocumentUnit, {
  DocumentationUnitParameters,
} from "@/domain/documentUnit"
import ComboboxItemService from "@/services/comboboxItemService"
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
const createNewFromSearchResponseError = ref<ResponseError | undefined>()

const docOffice = ref<DocumentationOffice | undefined>(
  props.parameters?.court?.responsibleDocOffice,
)

const responsibleDocOffice = computed({
  get: () =>
    docOffice.value
      ? {
          label: docOffice.value.abbreviation,
          value: docOffice.value,
        }
      : undefined,
  set: (newValue) => {
    const newDocOffice = { ...newValue } as DocumentationOffice
    if (newValue) {
      docOffice.value = newDocOffice
    }
  },
})

async function createNewFromSearch(openDocunit: boolean = false) {
  createNewFromSearchResponseError.value = undefined

  const isValid = props.validateRequiredInput()

  if (!isValid) {
    return
  }

  const createResponse = await documentUnitService.createNew(props.parameters)
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

watch(
  () => props.parameters,
  () => {
    docOffice.value = props.parameters?.court?.responsibleDocOffice
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col gap-24 bg-blue-200 p-24">
    <div>
      <p class="ds-label-01-bold">Nicht die passende Entscheidung gefunden?</p>
      <p>
        Wollen Sie die Daten übernehmen und eine neue Entscheidung erstellen?
      </p>
    </div>
    <InputField
      id="responsibleDocOffice"
      label="Zuständige Dokumentationsstelle *"
    >
      <ComboboxInput
        id="responsibleDocOffice"
        v-model="responsibleDocOffice"
        aria-label="zuständige Dokumentationsstelle"
        class="flex-shrink flex-grow-0 basis-1/2"
        data-testid="documentation-office-combobox"
        :item-service="ComboboxItemService.getDocumentationOffices"
      ></ComboboxInput>
    </InputField>

    <div class="flex flex-row gap-8">
      <TextButton
        aria-label="Ok"
        button-type="primary"
        :disabled="!responsibleDocOffice"
        label="Ok"
        size="small"
        @click="() => createNewFromSearch()"
      />
      <TextButton
        aria-label="Ok und Dokumentationseinheit direkt bearbeiten"
        button-type="tertiary"
        :disabled="!responsibleDocOffice"
        label="Ok und Dokumentationseinheit direkt bearbeiten"
        size="small"
        @click="() => createNewFromSearch(true)"
      />
    </div>
  </div>
  <div v-if="createNewFromSearchResponseError">
    <InfoModal
      :description="createNewFromSearchResponseError.description"
      :title="createNewFromSearchResponseError.title"
    />
  </div>
</template>
