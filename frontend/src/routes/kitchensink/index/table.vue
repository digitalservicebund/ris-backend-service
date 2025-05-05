<script setup lang="ts">
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { ref } from "vue"
import CellHeaderItem from "@/components/CellHeaderItem.vue"
import CellItem from "@/components/CellItem.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import IconBadge from "@/components/IconBadge.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import SearchResultStatus from "@/components/SearchResultStatus.vue"
import TableHeader from "@/components/TableHeader.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconDelete from "~icons/ic/baseline-close"
import IconError from "~icons/ic/baseline-error"
import IconSubject from "~icons/ic/baseline-subject"
import IconNote from "~icons/ic/outline-comment-bank"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconClock from "~icons/ic/outline-watch-later"
import IconArrowDown from "~icons/mdi/arrow-down-drop"

const data = [
  {
    name: "Lea",
    date: new Date().toDateString(),
  },
  { name: "Roy", date: new Date(8.64e15).toDateString() },
]

const documentationUnitListEntries: DocumentUnitListEntry[] = [
  {
    id: "id",
    uuid: "1",
    documentNumber: "123",
    decisionDate: dayjs("2022-02-10").format("DD.MM.YYYY"),
    createdAt: dayjs("2024-02-10").format("DD.MM.YYYY"),
    fileNumber: "ABC1234",
    note: "a note",
    appraisalBody: "",
    documentType: { label: "Test", jurisShortcut: "T" },
    court: { type: "typeA", location: "locB", label: "typeA locB" },
    status: {
      publicationStatus: PublicationState.PUBLISHED,
      withError: false,
    },
    hasAttachments: true,
    hasHeadnoteOrPrinciple: true,
    isDeletable: false,
    isEditable: false,
  },
  {
    id: "id",
    uuid: "2",
    documentNumber: "234",
    decisionDate: dayjs("2022-02-10").format("DD.MM.YYYY"),
    createdAt: dayjs("2024-02-10").format("DD.MM.YYYY"),
    fileNumber: "ABC4321",
    note: "longNote",
    appraisalBody: "cba",
    documentType: { label: "Test", jurisShortcut: "T" },
    court: { type: "typeA", location: "locB", label: "typeA locB" },
    status: {
      publicationStatus: PublicationState.PUBLISHED,
      withError: false,
    },
    hasAttachments: false,
    hasHeadnoteOrPrinciple: false,
    isDeletable: true,
    isEditable: true,
  },
  {
    id: "id",
    uuid: "3",
    documentNumber: "567",
    decisionDate: dayjs("2022-02-10").format("DD.MM.YYYY"),
    createdAt: dayjs("2024-02-10").format("DD.MM.YYYY"),
    fileNumber: "ABC0000",
    appraisalBody: "1. Senat",
    documentType: { label: "Urteil", jurisShortcut: "Urt" },
    court: { type: "LG", location: "Berlin", label: "LG Berlin" },
    status: {
      publicationStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
      withError: false,
    },
    hasAttachments: false,
    hasHeadnoteOrPrinciple: false,
    isDeletable: false,
    isEditable: false,
    source: "NJW",
    creatingDocumentationOffice: {
      id: "creatingDocumentationOfficeId",
      abbreviation: "DS",
    },
  },
]

const publicationDate = (listEntry: DocumentUnitListEntry) => {
  const date =
    listEntry.scheduledPublicationDateTime ?? listEntry.lastPublicationDateTime
  if (date) {
    return dayjs(date).format("DD.MM.YYYY HH:mm")
  } else {
    return "-"
  }
}

const searchResultString = "Keine Ergebnisse gefunden."

const isLoading = ref(true)
</script>

<template>
  <KitchensinkPage name="Table">
    <KitchensinkStory name="">
      <TableView class="w-full">
        <TableHeader>
          <CellHeaderItem>Name</CellHeaderItem>
          <CellHeaderItem>Datum</CellHeaderItem>
        </TableHeader>
        <TableRow v-for="item in data" :key="item.name">
          <CellItem>{{ item.name }}</CellItem>
          <CellItem>{{ item.date }}</CellItem>
        </TableRow>
      </TableView>
    </KitchensinkStory>

    <KitchensinkStory name="No search result">
      <div class="flex h-full flex-col bg-white">
        <TableView class="w-full">
          <TableHeader>
            <CellHeaderItem>Name</CellHeaderItem>
            <CellHeaderItem>Datum</CellHeaderItem>
          </TableHeader>
          <TableRow />
        </TableView>
        <SearchResultStatus :text="searchResultString"></SearchResultStatus>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Loading ">
      <div class="flex h-full flex-col bg-white">
        <TableView class="w-full">
          <TableHeader>
            <CellHeaderItem>Name</CellHeaderItem>
            <CellHeaderItem>Datum</CellHeaderItem>
          </TableHeader>
          <TableRow />
        </TableView>
        <SearchResultStatus
          v-if="!isLoading"
          :text="searchResultString"
        ></SearchResultStatus>
        <div
          v-if="isLoading"
          class="bg-opacity-60 grid justify-items-center bg-white py-112"
        >
          <LoadingSpinner />
        </div>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Documentation Unit List (RIS-UI)">
      <DataTable
        class="ris-label2-bold text-gray-900"
        :pt="{
          thead: {
            style: 'box-shadow: inset 0 -2px #DCE8EF;',
          },
        }"
        :value="documentationUnitListEntries"
      >
        <Column selection-mode="multiple"></Column>
        <Column field="documentNumber" header="Dokumentnummer">
          <template #body="{ data: item }">
            <FlexContainer align-items="items-center" class="space-x-8">
              <FlexItem class="flex-grow">{{ item.documentNumber }} </FlexItem>

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
            </FlexContainer> </template
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
            <div class="flex flex-row -space-x-2">
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
    </KitchensinkStory>

    <KitchensinkStory name="Documentation Unit List (old)">
      <TableView
        class="relative table w-full border-separate"
        data-testid="documentUnitList"
      >
        <TableHeader>
          <CellHeaderItem class="w-[1%]"> Dokumentnummer</CellHeaderItem>
          <CellHeaderItem> Gerichtstyp</CellHeaderItem>
          <CellHeaderItem> Ort</CellHeaderItem>
          <CellHeaderItem>
            <div class="flex flex-row">
              Datum
              <IconArrowDown />
            </div>
          </CellHeaderItem>
          <CellHeaderItem> Aktenzeichen</CellHeaderItem>
          <CellHeaderItem> Spruchkörper</CellHeaderItem>
          <CellHeaderItem> Typ</CellHeaderItem>
          <CellHeaderItem> Status</CellHeaderItem>
          <CellHeaderItem> Fehler</CellHeaderItem>
          <CellHeaderItem>
            <div class="flex flex-row items-center">
              jDV Übergabe <IconArrowDown /></div
          ></CellHeaderItem>
          <CellHeaderItem />
        </TableHeader>
        <TableRow
          v-for="(listEntry, id) in documentationUnitListEntries"
          :key="id"
          :data-testid="`listEntry_${listEntry.documentNumber}`"
        >
          <CellItem>
            <FlexContainer align-items="items-center" class="space-x-8">
              <FlexItem class="flex-grow"
                >{{ listEntry.documentNumber }}
              </FlexItem>

              <Tooltip text="Tooltip Text">
                <IconAttachedFile
                  class="flex-end h-20 w-20"
                  :class="
                    listEntry.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                  "
                  data-testid="file-attached-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconSubject
                  class="flex-end flex h-20 w-20"
                  :class="
                    listEntry.hasHeadnoteOrPrinciple
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="headnote-principle-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconNote
                  class="flex-end flex h-20 w-20"
                  :class="!!listEntry.note ? 'text-blue-800' : 'text-gray-500'"
                  data-testid="note-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconClock
                  class="flex-end flex h-20 w-20"
                  :class="
                    listEntry.scheduledPublicationDateTime
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="scheduling-icon"
                />
              </Tooltip>
            </FlexContainer>
          </CellItem>
          <CellItem>
            {{ listEntry.court?.type ?? "-" }}
          </CellItem>
          <CellItem>
            {{ listEntry.court?.location ?? "-" }}
          </CellItem>
          <CellItem>
            {{
              listEntry.decisionDate
                ? dayjs(listEntry.decisionDate).format("DD.MM.YYYY")
                : "-"
            }}
          </CellItem>
          <CellItem>
            {{ listEntry.fileNumber ? listEntry.fileNumber : "-" }}
          </CellItem>
          <CellItem>
            {{ listEntry.appraisalBody ?? "-" }}
          </CellItem>
          <CellItem>
            {{
              listEntry.documentType
                ? listEntry.documentType.jurisShortcut
                : "-"
            }}
          </CellItem>
          <CellItem class="flex min-w-176 flex-row">
            <IconBadge
              v-if="listEntry.status?.publicationStatus"
              class="inline-flex"
              v-bind="useStatusBadge(listEntry.status).value"
              data-testid="publication-status"
            />
            <span
              v-if="
                listEntry.status?.publicationStatus ===
                PublicationState.EXTERNAL_HANDOVER_PENDING
              "
              class="ris-body2-regular"
            >
              {{
                " aus " +
                listEntry.source +
                " (" +
                listEntry.creatingDocumentationOffice?.abbreviation +
                ")"
              }}
            </span>
          </CellItem>
          <CellItem>
            <IconBadge
              v-if="listEntry.status?.withError"
              background-color="bg-red-300"
              color="text-red-900"
              :icon="IconError"
              label="Fehler"
            />
            <span v-else>-</span>
          </CellItem>
          <CellItem data-testid="publicationDate">
            {{ publicationDate(listEntry) }}
          </CellItem>
          <CellItem class="flex">
            <div class="flex flex-row -space-x-2">
              <Tooltip text="Bearbeiten">
                <Button
                  aria-label="Dokumentationseinheit bearbeiten"
                  :disabled="
                    !listEntry.isEditable ||
                    listEntry.status?.publicationStatus ==
                      'EXTERNAL_HANDOVER_PENDING'
                  "
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconEdit />
                  </template>
                </Button>
              </Tooltip>

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
              <Tooltip text="Löschen">
                <Button
                  aria-label="Dokumentationseinheit löschen"
                  :disabled="
                    !listEntry.isDeletable ||
                    listEntry.status?.publicationStatus ==
                      'EXTERNAL_HANDOVER_PENDING'
                  "
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconDelete />
                  </template>
                </Button>
              </Tooltip>
            </div>
          </CellItem>
        </TableRow>
      </TableView>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
