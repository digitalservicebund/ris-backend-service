<script lang="ts" setup>
import dayjs from "dayjs"
import CellItem from "@/components/CellItem.vue"
import TableHeader from "@/components/TableHeader.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import File from "@/domain/file"
import IconDelete from "~icons/ic/baseline-close"

const props = defineProps<{
  files?: File[]
}>()

const emit = defineEmits<{
  deleteEvent: [file: File]
}>()

/**
 * Propagates delete event to parent and closes modal again
 */
const onDelete = (file: File) => {
  emit("deleteEvent", file)
}
</script>

<template>
  <TableView class="relative table w-full border-separate">
    <TableHeader>
      <CellItem> Dateiname</CellItem>
      <CellItem> Format</CellItem>
      <CellItem> Hochgeladen am</CellItem>
      <CellItem></CellItem>
    </TableHeader>
    <TableRow
      v-for="(file, id) in props.files"
      :key="id"
      data-testid="listEntry"
    >
      <CellItem>
        {{ file.name ?? "-" }}
      </CellItem>
      <CellItem>
        {{ file.format ?? "-" }}
      </CellItem>

      <CellItem>
        {{
          file.uploadedDate
            ? dayjs(file.uploadedDate).format("DD.MM.YYYY")
            : "-"
        }}
      </CellItem>
      <CellItem class="ext-center">
        <button
          aria-label="Datei löschen"
          class="-full cursor-pointer align-middle text-blue-800 focus:outline-none focus-visible:outline-blue-800"
          @click="onDelete(file)"
          @keyup.enter="null"
        >
          <IconDelete />
        </button>
      </CellItem>
    </TableRow>
  </TableView>
</template>
