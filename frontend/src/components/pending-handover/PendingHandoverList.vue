<script lang="ts" setup>
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconSubject from "~icons/ic/baseline-subject"
import IconNote from "~icons/ic/outline-comment-bank"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconClock from "~icons/ic/outline-watch-later"

const props = defineProps<{
  pageEntries?: Page<DocumentUnitListEntry>
}>()

const emit = defineEmits<{
  updatePage: [number]
}>()

const entries = computed(() => {
  return props.pageEntries?.content || []
})
</script>

<template>
  <div>
    <Pagination
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <DataTable
        class="ris-label2-bold text-gray-900"
        :pt="{
          thead: {
            style: 'box-shadow: inset 0 -2px #DCE8EF;',
          },
        }"
        :value="entries"
      >
        <Column selection-mode="multiple"></Column>
        <Column field="documentNumber" header="Dokumentnummer">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center space-x-8">
              <div>{{ item.documentNumber }}</div>

              <Tooltip text="Tooltip Text">
                <IconAttachedFile
                  class="flex-end h-20 w-20"
                  :class="
                    item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                  "
                  data-testid="file-attached-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconSubject
                  class="flex-end flex h-20 w-20"
                  :class="
                    item.hasHeadnoteOrPrinciple
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="headnote-principle-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconNote
                  class="flex-end flex h-20 w-20"
                  :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                  data-testid="note-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconClock
                  class="flex-end flex h-20 w-20"
                  :class="
                    item.scheduledPublicationDateTime
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="scheduling-icon"
                />
              </Tooltip>
            </div> </template
        ></Column>
        <Column field="court.type" header="Gerichtstyp"></Column>
        <Column field="court.location" header="Ort"></Column>
        <Column field="decisionDate" header="Datum"> </Column>
        <Column field="fileNumber" header="Aktenzeichen"></Column>
        <Column field="source" header="Quelle">
          <template #body="{ data: item }">
            {{
              item.source
                ? item.source +
                  " (" +
                  item.creatingDocumentationOffice?.abbreviation +
                  ")"
                : ""
            }}</template
          >
        </Column>
        <Column field="createdAt" header="Angelegt am"> </Column>
        <Column header="Status">
          <template #body="{ data: item }">
            <IconBadge
              v-if="item.status?.publicationStatus"
              class="inline-flex"
              v-bind="useStatusBadge(item.status).value"
              data-testid="publication-status"
            />
          </template>
        </Column>
        <Column field="actions">
          <template #body>
            <div class="flex flex-row justify-end -space-x-2">
              <Tooltip text="Vorschau">
                <Button
                  aria-label="Dokumentationseinheit ansehen"
                  class="z-20"
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconView />
                  </template>
                </Button>
              </Tooltip>
              <Tooltip text="Bearbeiten">
                <Button
                  aria-label="Dokumentationseinheit bearbeiten"
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconEdit />
                  </template>
                </Button>
              </Tooltip>
            </div>
          </template>
        </Column>
      </DataTable>
    </Pagination>
  </div>
</template>
