<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import { computed, ref, watch } from "vue"
import { useRouter } from "vue-router"
import TextButton from "@/components/input/TextButton.vue"
import PopupModal from "@/components/PopupModal.vue"
import documentUnitService from "@/services/documentUnitService"

const { documentNumber, uuid } = defineProps<{
  documentNumber: string
  uuid: string
}>()

const showDeleteModal = ref(false)

const scrollLock = useScrollLock(document)
watch(showDeleteModal, () => (scrollLock.value = showDeleteModal.value))

const deleteModalText = computed(
  () =>
    `Möchten Sie die Dokumentationseinheit ${documentNumber} wirklich dauerhaft löschen?`,
)
const router = useRouter()

const deleteDocumentUnit = async () => {
  if (!uuid) return

  const { error, data } = await documentUnitService.delete(uuid)
  if (error) {
    alert("Fehler beim Löschen der Dokumentationseinheit: " + data)
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
  <TextButton
    button-type="destructive"
    label="Dokumentationseinheit löschen"
    size="small"
    @click="showDeleteModal = true"
  />
</template>
