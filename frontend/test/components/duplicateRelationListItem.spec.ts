import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import DuplicateRelationListItem from "@/components/DuplicateRelationListItem.vue"
import DocumentUnit, {
  DuplicateRelation,
  DuplicationRelationStatus,
} from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

function mockDocUnitStore(
  {
    duplicateRelations,
  }: {
    duplicateRelations: DuplicateRelation[]
  } = { duplicateRelations: [] },
) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new DocumentUnit("q834", {
    documentNumber: "original",
    managementData: {
      duplicateRelations,
      borderNumbers: [],
    },
  })

  return mockedSessionStore
}

describe("DuplicateRelationListItem", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.resetAllMocks()
  })

  describe("display core data, link and checkbox", () => {
    it("should show docnumber and unchecked check box if no core data is given", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.queryByTestId("core-data-text")
      expect(coreDataText).not.toBeInTheDocument()

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).not.toHaveAttribute("readonly")
      expect(ignoreCheckbox).toBeEnabled()
    })

    it("should show checked check box if status is ignored", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.IGNORED,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.queryByTestId("core-data-text")
      expect(coreDataText).not.toBeInTheDocument()

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toBeChecked()
      expect(ignoreCheckbox).not.toHaveAttribute("readonly")
      expect(ignoreCheckbox).toBeEnabled()
    })

    it("should show docnumber and disabled checked check box if jdv dup check is turned off", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.IGNORED,
        isJdvDuplicateCheckActive: false,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.queryByTestId("core-data-text")
      expect(coreDataText).not.toBeInTheDocument()

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toBeChecked()
      expect(ignoreCheckbox).toBeDisabled()
    })

    it("should show full data if full core data is given", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        courtLabel: "AG Aachen",
        decisionDate: "2020-04-03",
        fileNumber: "fileNumber-A",
        status: DuplicationRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.getByTestId("core-data-text")
      expect(coreDataText).toHaveTextContent(
        "AG Aachen, fileNumber-A, 03.04.2020",
      )

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).toBeEnabled()
    })

    it("should show only partial core data", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        courtLabel: "AG Aachen",
        fileNumber: "fileNumber-A",
        status: DuplicationRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.getByTestId("core-data-text")
      expect(coreDataText).toHaveTextContent("AG Aachen, fileNumber-A")

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).toBeEnabled()
    })
  })

  describe("set duplicate relation status", () => {
    it("should set state from PENDING -> IGNORED", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicationRelationStatus")
        .mockResolvedValue({ error: false })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(screen.getByLabelText("Warnung ignorieren")).toBeChecked()

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(
        documentUnit?.managementData?.duplicateRelations?.[0].status,
      ).toEqual(DuplicationRelationStatus.IGNORED)
    })

    it("should set state from IGNORED -> PENDING", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicationRelationStatus")
        .mockResolvedValue({ error: false })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.IGNORED,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.getByLabelText("Warnung ignorieren")).toBeChecked()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(
        documentUnit?.managementData?.duplicateRelations?.[0].status,
      ).toEqual(DuplicationRelationStatus.PENDING)
    })

    it("should show error if setting state fails", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicationRelationStatus")
        .mockResolvedValue({ error: true })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicationRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.getByTestId("set-state-error")).toBeInTheDocument()
      expect(
        documentUnit?.managementData?.duplicateRelations?.[0].status,
      ).toEqual(DuplicationRelationStatus.PENDING)
    })
  })

  function renderDuplicateRelation(duplicateRelation: DuplicateRelation) {
    const store = mockDocUnitStore({ duplicateRelations: [duplicateRelation] })
    render(DuplicateRelationListItem, {
      props: { duplicateRelation },
      global: {
        stubs: {
          RouterLink: {
            template: "<a><slot/></a>",
          },
        },
      },
    })
    return store
  }
})
