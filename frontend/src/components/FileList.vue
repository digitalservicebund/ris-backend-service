<script lang="ts" setup>
import dayjs from "dayjs"
import CellItem from "@/components/CellItem.vue"
import TableHeader from "@/components/TableHeader.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import Attachment from "@/domain/attachment"
import IconDelete from "~icons/ic/baseline-close"

const props = defineProps<{
  files?: Attachment[]
}>()

const emit = defineEmits<{
  (e: "delete", id: number): void
  (e: "select", id: number): void
}>()

/**
 * Propagates delete event to parent and closes modal again
 */
const onDelete = (index: number) => {
  emit("delete", index)
}

const onSelect = (index: number) => {
  emit("select", index)
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
      v-for="(file, index) in props.files"
      :key="index"
      class="cursor-pointer"
      data-testid="listEntry"
    >
      <CellItem @click="onSelect(index)">
        {{ file.name ?? "-" }}
      </CellItem>
      <CellItem @click="onSelect(index)">
        {{ file.format ?? "-" }}
      </CellItem>

      <CellItem @click="onSelect(index)">
        {{
          file.uploadTimestamp
            ? dayjs(file.uploadTimestamp).format("DD.MM.YYYY")
            : "-"
        }}
      </CellItem>
      <CellItem class="ext-center">
        <button
          aria-label="Datei löschen"
          class="-full cursor-pointer align-middle text-blue-800 focus:outline-none focus-visible:outline-blue-800"
          @click="onDelete(index)"
          @keyup.enter="null"
        >
          <IconDelete />
        </button>
      </CellItem>
    </TableRow>
  </TableView>
</template>
