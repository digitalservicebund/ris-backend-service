import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { it } from "vitest"
import { nextTick } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitManagementData from "@/components/management-data/DocumentUnitManagementData.vue"
import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
  ManagementData,
} from "@/domain/managementData"
import PendingProceeding from "@/domain/pendingProceeding"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: vi.fn() }),
}))

function renderManagementData(managementData: ManagementData, kind?: Kind) {
  const store = mockDocUnitStore(managementData, kind)

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  render(DocumentUnitManagementData, {
    global: {
      plugins: [router],
    },
  })
  return store
}

function duplicateRelation(docNumber: string): DuplicateRelation {
  return {
    documentNumber: docNumber,
    status: DuplicateRelationStatus.PENDING,
    isJdvDuplicateCheckActive: true,
  }
}

function mockDocUnitStore(
  managementData: ManagementData,
  kind = Kind.DECISION,
) {
  const mockedSessionStore = useDocumentUnitStore()
  if (kind === Kind.DECISION) {
    mockedSessionStore.documentUnit = new Decision("q834", {
      documentNumber: "DS123",
      managementData,
    })
  } else {
    mockedSessionStore.documentUnit = new PendingProceeding("q834", {
      documentNumber: "DS123",
      managementData,
    })
  }
  vi.spyOn(mockedSessionStore, "updateDocumentUnit").mockResolvedValue({
    status: 200,
    data: { documentationUnitVersion: 0, patch: [], errorPaths: [] },
  })

  return mockedSessionStore
}

describe("management data", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.resetAllMocks()
  })

  it("should show doc unit metadata", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    expect(screen.getByText(`Zuletzt bearbeitet am`)).toBeVisible()
  })

  it("should load and show doc unit history", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 200,
      data: [{ id: "1" }],
    })

    renderManagementData(managementData)
    // Wait for saving doc unit
    await nextTick()
    // Wait for loading history
    await nextTick()

    expect(screen.getByText(`Historie`)).toBeVisible()
    expect(screen.getByText(`Änderung am`)).toBeVisible()
  })

  it("should show error for doc unit history", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 403,
      error: { title: "Kein Zugang", description: "Nicht erlaubt" },
    })

    renderManagementData(managementData)
    // Wait for saving doc unit
    await nextTick()
    // Wait for loading history
    await nextTick()

    expect(
      screen.getByText(`Die Historie konnte nicht geladen werden.`),
    ).toBeVisible()
  })

  describe("Duplicate relations", () => {
    it("should show empty duplicate relations", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const ignoreCheckbox = screen.queryByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeInTheDocument()

      const noDuplicateRelationsText = screen.getByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).toBeVisible()
    })

    it("should display one duplicate relation", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [duplicateRelation("dup-1")],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const duplicateRelationsText = screen.getByText("dup-1")
      expect(duplicateRelationsText).toBeVisible()

      const ignoreCheckbox = screen.getAllByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toHaveLength(1)

      const noDuplicateRelationsText = screen.queryByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).not.toBeInTheDocument()
    })

    it("should display multiple duplicate relations", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [
          duplicateRelation("dup-1"),
          duplicateRelation("dup-2"),
        ],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const duplicateDocNumberText1 = screen.getByText("dup-1")
      expect(duplicateDocNumberText1).toBeVisible()

      const duplicateDocNumberText2 = screen.getByText("dup-2")
      expect(duplicateDocNumberText2).toBeVisible()

      const ignoreCheckbox = screen.getAllByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toHaveLength(2)

      const noDuplicateRelationsText = screen.queryByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).not.toBeInTheDocument()
    })
  })

  it("should show a delete doc unit button", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit löschen" }),
    ).toBeVisible()
  })
})

describe("pending proceeding management data", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.resetAllMocks()
  })

  it("should show doc unit metadata", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData, Kind.PENDING_PROCEEDING)

    expect(screen.getByText(`Zuletzt bearbeitet am`)).toBeVisible()
  })

  it("should load and show doc unit history", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 200,
      data: [{ id: "1" }],
    })

    renderManagementData(managementData, Kind.PENDING_PROCEEDING)
    // Wait for saving doc unit
    await nextTick()
    // Wait for loading history
    await nextTick()

    expect(screen.getByText(`Historie`)).toBeVisible()
    expect(screen.getByText(`Änderung am`)).toBeVisible()
  })

  it("should not display duplicate check", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData, Kind.PENDING_PROCEEDING)

    expect(screen.queryByText("Dublettenverdacht")).not.toBeInTheDocument()
  })

  it("should not display assign docoffice input", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData, Kind.PENDING_PROCEEDING)

    expect(
      screen.queryByTestId("assign-documentation-office-title"),
    ).not.toBeInTheDocument()
  })

  it("should show a delete doc unit button", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData, Kind.PENDING_PROCEEDING)

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit löschen" }),
    ).toBeVisible()
  })
})
