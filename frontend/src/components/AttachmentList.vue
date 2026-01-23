<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import dayjs from "dayjs"
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import { Ref, ref, watch } from "vue"
import CellHeaderItem from "@/components/CellHeaderItem.vue"
import CellItem from "@/components/CellItem.vue"
import TableHeader from "@/components/TableHeader.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import { Attachment } from "@/domain/attachment"
import IconDelete from "~icons/ic/baseline-close"
import IconDownload from "~icons/ic/baseline-save-alt"

const props = defineProps<{
  files?: Attachment[]
  attachmentIdsWithActiveDownload: string[]
  enableSelect?: boolean
}>()

const emit = defineEmits<{
  (e: "delete", attachment: Attachment): void
  (e: "download", attachment: Attachment): void
  (e: "select", id: number): void
}>()

/**
 * Propagates delete event to parent and closes modal again
 */
const onDelete = (attachment?: Attachment) => {
  if (attachment) emit("delete", attachment)
  closeDeleteModal()
}

const onSelect = (index: number) => {
  emit("select", index)
}

const onDownload = (attachment: Attachment) => {
  emit("download", attachment)
}

const showDeleteModal = ref(false)
const attachmentToBeDeleted: Ref<Attachment | undefined> = ref()

const scrollLock = useScrollLock(document)
watch(showDeleteModal, () => (scrollLock.value = showDeleteModal.value))

function openDeleteModal(attachment: Attachment) {
  attachmentToBeDeleted.value = attachment
  if (attachmentToBeDeleted.value != null) {
    showDeleteModal.value = true
  }
}

function closeDeleteModal() {
  showDeleteModal.value = false
  attachmentToBeDeleted.value = undefined
}
</script>

<template>
  <TableView
    id="attachment-list"
    class="relative table w-full border-separate"
    data-testid="attachment-list"
  >
    <TableHeader>
      <CellHeaderItem> Dateiname</CellHeaderItem>
      <CellHeaderItem> Format</CellHeaderItem>
      <CellHeaderItem> Hochgeladen am</CellHeaderItem>
      <CellHeaderItem></CellHeaderItem>
    </TableHeader>
    <TableRow
      v-for="(file, index) in props.files"
      :key="index"
      :class="enableSelect ? 'cursor-pointer' : ''"
      :data-testid="`list-entry-${index}`"
    >
      <CellItem class="wrap-anywhere" @click="onSelect(index)">
        {{ file.name ?? "-" }}
      </CellItem>
      <CellItem @click="onSelect(index)">
        {{ file.format ?? "-" }}
      </CellItem>

      <CellItem data-testid="uploaded-at-cell" @click="onSelect(index)">
        {{
          file.uploadTimestamp
            ? dayjs(file.uploadTimestamp).format("DD.MM.YYYY")
            : "-"
        }}
      </CellItem>
      <CellItem class="min-w-[110px] justify-end">
        <div class="flex flex-row justify-end -space-x-2">
          <Button
            v-tooltip.bottom="{
              value: 'Löschen',
              appendTo: 'body',
            }"
            aria-label="Datei löschen"
            severity="secondary"
            size="small"
            @click="openDeleteModal(file)"
          >
            <template #icon> <IconDelete /></template>
          </Button>
          <Button
            v-tooltip.bottom="{
              value: 'Herunterladen',
              appendTo: 'body',
            }"
            :aria-label="`${file.name} herunterladen`"
            :loading="attachmentIdsWithActiveDownload.includes(file.id)"
            severity="secondary"
            size="small"
            @click="onDownload(file)"
          >
            <template #icon> <IconDownload /></template>
          </Button>
        </div>
      </CellItem>
    </TableRow>
  </TableView>
  <Dialog
    v-model:visible="showDeleteModal"
    class="max-h-[768px] max-w-[640px]"
    :closable="false"
    dismissable-mask
    header="Anhang löschen"
    modal
  >
    <p>
      Möchten Sie den Anhang <b>{{ attachmentToBeDeleted?.name }}</b> wirklich
      dauerhaft löschen?
    </p>
    <div class="modal-buttons-container flex w-full flex-row gap-[1rem] pt-32">
      <Button
        aria-label="Anhang löschen"
        label="Löschen"
        severity="primary"
        size="small"
        @click="onDelete(attachmentToBeDeleted)"
      ></Button>
      <Button
        aria-label="Abbrechen"
        label="Abbrechen"
        severity="secondary"
        size="small"
        @click="showDeleteModal = false"
      ></Button>
    </div>
  </Dialog>
</template>
