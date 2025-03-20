<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import Button from "primevue/button"
import { computed, ref, watch } from "vue"
import { useRouter } from "vue-router"
import PopupModal from "@/components/PopupModal.vue"
import documentUnitService from "@/services/documentUnitService"

const { documentNumber, uuid } = defineProps<{
  documentNumber: string
  uuid: string
}>()

const showDeleteModal = ref(false)
const showErrorModal = ref(false)
const errorModalText = ref<string>("")

const scrollLock = useScrollLock(document)
watch(
  [showDeleteModal, showErrorModal],
  () => (scrollLock.value = showDeleteModal.value || showErrorModal.value),
)

const deleteModalText = computed(
  () =>
    `Möchten Sie die Dokumentationseinheit ${documentNumber} wirklich dauerhaft löschen?`,
)
const router = useRouter()

const deleteDocumentUnit = async () => {
  if (!uuid) return

  const { error, data } = await documentUnitService.delete(uuid)
  if (error) {
    errorModalText.value = data ?? "Ein unbekannter Fehler ist aufgetreten."
    showErrorModal.value = true
    showDeleteModal.value = false
  } else {
    await router.push({ path: "/" })
  }
}
</script>

<template>
  <PopupModal
    v-if="showDeleteModal"
    aria-label="Dokumentationseinheit löschen"
    :content-text="deleteModalText"
    header-text="Dokumentationseinheit löschen"
    primary-button-text="Löschen"
    primary-button-type="destructive"
    @close-modal="showDeleteModal = false"
    @primary-action="deleteDocumentUnit"
  />
  <PopupModal
    v-if="showErrorModal"
    aria-label="Fehler beim Löschen der Dokumentationseinheit"
    cancel-button-type="none"
    :content-text="errorModalText"
    header-text="Fehler beim Löschen der Dokumentationseinheit"
    primary-button-text="OK"
    primary-button-type="primary"
    @close-modal="showErrorModal = false"
    @primary-action="showErrorModal = false"
  />
  <Button
    aria-label="Dokumentationseinheit löschen"
    label="Dokumentationseinheit löschen"
    severity="danger"
    size="small"
    @click="showDeleteModal = true"
  >
  </Button>
</template>
