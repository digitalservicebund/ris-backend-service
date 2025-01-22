import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitManagementData from "@/components/DocumentUnitManagementData.vue"
import DocumentUnit, {
  DuplicateRelation,
  DuplicateRelationStatus,
  ManagementData,
} from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

describe("DocumentUnitManagementData", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.resetAllMocks()
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

  function renderManagementData(managementData: ManagementData) {
    const store = mockDocUnitStore(managementData)

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

  function mockDocUnitStore(managementData: ManagementData) {
    const mockedSessionStore = useDocumentUnitStore()
    mockedSessionStore.documentUnit = new DocumentUnit("q834", {
      documentNumber: "original",
      managementData,
    })

    return mockedSessionStore
  }
})
