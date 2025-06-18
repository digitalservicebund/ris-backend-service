import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DuplicateRelationListItem from "@/components/management-data/DuplicateRelationListItem.vue"
import { Decision } from "@/domain/decision"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/documentUnit"
import { PublicationState } from "@/domain/publicationStatus"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

function mockDocUnitStore(
  {
    duplicateRelations,
    state,
  }: {
    duplicateRelations: DuplicateRelation[]
    state?: PublicationState
  } = { duplicateRelations: [] },
) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new Decision("q834", {
    documentNumber: "original",
    status: state ? { publicationStatus: state } : undefined,
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
        status: DuplicateRelationStatus.PENDING,
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

      const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
      // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
      expect(warningIcon).not.toHaveClass("invisible")

      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
    })

    it("should show checked check box if status is ignored", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicateRelationStatus.IGNORED,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toBeChecked()
      expect(ignoreCheckbox).not.toHaveAttribute("readonly")
      expect(ignoreCheckbox).toBeEnabled()

      const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
      // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
      expect(warningIcon).toHaveClass("invisible")

      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
    })

    it("should display full data if full core data is given", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        courtLabel: "AG Aachen",
        decisionDate: "2020-04-03",
        documentType: "Beschluss",
        fileNumber: "fileNumber-A",
        publicationStatus: PublicationState.PUBLISHED,
        status: DuplicateRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation, PublicationState.UNPUBLISHED)

      const decisionSummary = screen.getByTestId("decision-summary-duplicate-1")
      expect(decisionSummary).toHaveTextContent(
        "AG Aachen, 03.04.2020, fileNumber-A, Beschluss, Veröffentlicht",
      )

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).toBeEnabled()

      const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
      // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
      expect(warningIcon).not.toHaveClass("invisible")

      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
    })

    it("should display only partial core data", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        courtLabel: "AG Aachen",
        fileNumber: "fileNumber-A",
        status: DuplicateRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const decisionSummary = screen.getByTestId("decision-summary-duplicate-1")
      expect(decisionSummary).toHaveTextContent("AG Aachen, fileNumber-A")

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).toBeEnabled()

      const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
      // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
      expect(warningIcon).not.toHaveClass("invisible")
    })

    it("should display only single core data", async () => {
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        fileNumber: "fileNumber-A",
        status: DuplicateRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      renderDuplicateRelation(duplicateRelation)

      const coreDataText = screen.getByTestId("decision-summary-duplicate-1")
      expect(coreDataText).toHaveTextContent("fileNumber-A")

      const docUnitLink = screen.getByTestId("document-number-link-duplicate-1")
      expect(docUnitLink).toHaveTextContent("duplicate-1")

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeChecked()
      expect(ignoreCheckbox).toBeEnabled()

      const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
      // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
      expect(warningIcon).not.toHaveClass("invisible")
    })

    describe("automatically ignore warning", () => {
      const jdvDupCodeWarningLabel =
        'Warnung ignoriert wegen "Dupcode ausschalten" (jDV)'
      const statusWarningLabel =
        'Warnung ignoriert wegen Status "Dublette" oder "Gesperrt"'

      ;[
        {
          currentDocUnitStatus: PublicationState.PUBLISHED,
          duplicateDocUnitStatus: PublicationState.PUBLISHING,
          isJdvDuplicateCheckActive: false,
          expectedCheckboxLabel: jdvDupCodeWarningLabel,
        },
        {
          currentDocUnitStatus: PublicationState.DUPLICATED,
          duplicateDocUnitStatus: PublicationState.LOCKED,
          isJdvDuplicateCheckActive: false,
          expectedCheckboxLabel: jdvDupCodeWarningLabel,
        },
        {
          currentDocUnitStatus: PublicationState.DUPLICATED,
          duplicateDocUnitStatus: PublicationState.UNPUBLISHED,
          isJdvDuplicateCheckActive: true,
          expectedCheckboxLabel: statusWarningLabel,
        },
        {
          currentDocUnitStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
          duplicateDocUnitStatus: PublicationState.DUPLICATED,
          isJdvDuplicateCheckActive: true,
          expectedCheckboxLabel: statusWarningLabel,
        },
        {
          currentDocUnitStatus: PublicationState.LOCKED,
          duplicateDocUnitStatus: undefined,
          isJdvDuplicateCheckActive: true,
          expectedCheckboxLabel: statusWarningLabel,
        },
        {
          currentDocUnitStatus: undefined,
          duplicateDocUnitStatus: PublicationState.LOCKED,
          isJdvDuplicateCheckActive: true,
          expectedCheckboxLabel: statusWarningLabel,
        },
      ].forEach(
        ({
          currentDocUnitStatus,
          duplicateDocUnitStatus,
          isJdvDuplicateCheckActive,
          expectedCheckboxLabel,
        }) => {
          it(`should show a disabled check check box if jdv dup check is turned ${isJdvDuplicateCheckActive ? "on" : "off"} and the publication statuses are ${currentDocUnitStatus} and ${duplicateDocUnitStatus}`, async () => {
            const duplicateRelation: DuplicateRelation = {
              documentNumber: "duplicate-1",
              status: DuplicateRelationStatus.IGNORED,
              publicationStatus: duplicateDocUnitStatus,
              isJdvDuplicateCheckActive,
            }
            renderDuplicateRelation(duplicateRelation, currentDocUnitStatus)

            const docUnitLink = screen.getByTestId(
              "document-number-link-duplicate-1",
            )
            expect(docUnitLink).toHaveTextContent("duplicate-1")

            const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
            expect(ignoreCheckbox).toBeChecked()
            expect(ignoreCheckbox).toBeDisabled()

            const checkboxLabel = screen.getByText(expectedCheckboxLabel)
            expect(checkboxLabel).toBeVisible()

            const warningIcon = screen.queryByTestId("warning-icon-duplicate-1")
            // jsdom does not load tailwind classes, so toBeVisible() check does not work here.
            expect(warningIcon).toHaveClass("invisible")

            expect(
              screen.queryByTestId("set-state-error"),
            ).not.toBeInTheDocument()
          })
        },
      )
    })
  })

  describe("set duplicate relation status", () => {
    it("should set state from PENDING -> IGNORED", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicateRelationStatus")
        .mockResolvedValue({ error: false })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicateRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(screen.getByLabelText("Warnung ignorieren")).toBeChecked()

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(
        documentUnit?.managementData?.duplicateRelations[0].status,
      ).toEqual(DuplicateRelationStatus.IGNORED)
    })

    it("should set state from IGNORED -> PENDING", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicateRelationStatus")
        .mockResolvedValue({ error: false })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicateRelationStatus.IGNORED,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(screen.getByLabelText("Warnung ignorieren")).toBeChecked()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()
      expect(
        documentUnit?.managementData?.duplicateRelations[0].status,
      ).toEqual(DuplicateRelationStatus.PENDING)
    })

    it("should show error if setting state fails", async () => {
      const setStatusServiceMock = vi
        .spyOn(documentUnitService, "setDuplicateRelationStatus")
        .mockResolvedValue({ error: true })
      const duplicateRelation: DuplicateRelation = {
        documentNumber: "duplicate-1",
        status: DuplicateRelationStatus.PENDING,
        isJdvDuplicateCheckActive: true,
      }
      const { documentUnit } = renderDuplicateRelation(duplicateRelation)

      expect(screen.getByLabelText("Warnung ignorieren")).not.toBeChecked()
      expect(screen.queryByTestId("set-state-error")).not.toBeInTheDocument()

      const ignoreCheckbox = screen.getByLabelText("Warnung ignorieren")
      await fireEvent.click(ignoreCheckbox)

      expect(setStatusServiceMock).toHaveBeenCalledOnce()
      expect(screen.getByTestId("set-state-error")).toBeInTheDocument()
      // The state is set before the error is evaluated as to allow for quick navigation after setting the state
      expect(
        documentUnit?.managementData?.duplicateRelations[0].status,
      ).toEqual(DuplicateRelationStatus.IGNORED)
    })
  })

  function renderDuplicateRelation(
    duplicateRelation: DuplicateRelation,
    state?: PublicationState,
  ) {
    const store = mockDocUnitStore({
      duplicateRelations: [duplicateRelation],
      state,
    })

    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    render(DuplicateRelationListItem, {
      props: { duplicateRelation },
      global: {
        plugins: [router],
      },
    })
    return store as Store<
      "docunitStore",
      {
        documentUnit: Ref<Decision>
      }
    >
  }
})
