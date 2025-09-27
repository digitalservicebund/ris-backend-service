import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import BulkAssignProcessStep from "@/components/BulkAssignProcessStep.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import { User } from "@/domain/user"
import DocumentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

const mockPayload = {
  processStep: { uuid: "test-uuid", name: "Test Step" },
  user: { id: "user-id-test", name: "Test User" },
}

const mockUser: User = {
  id: "user-id-1",
  name: "Test User",
  email: "test@user.com",
  documentationOffice: { abbreviation: "DS" },
  internal: true,
  initials: "TU",
}

function renderComponent(
  props: { documentationUnits: DocumentUnitListEntry[] | undefined } = {
    documentationUnits: [],
  },
) {
  const user = userEvent.setup()
  return {
    user,
    ...render(BulkAssignProcessStep, {
      props: {
        documentationUnits: props.documentationUnits,
      },
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              session: {
                user: mockUser,
              },
            },
            stubActions: false,
          }),
        ],

        stubs: {
          AssignProcessStep: {
            template: `
                <div v-if="props.visible" data-testid="assign-process-step-stub">
                  <button data-testid="assign-button" @click="assignProcessStep"></button>
                </div>
              `,
            props: ["visible", "handleAssignProcessStep"],
            setup(props) {
              async function assignProcessStep() {
                await props.handleAssignProcessStep(mockPayload)
              }

              return { assignProcessStep, props }
            },
          },
          Menu: false,
          Button: false,
        },
        directives: {
          tooltip: {},
        },
      },
    }),
  }
}

describe("BulkAssignProcessStep component", () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it("renders the 'Aktionen' button and the assignProcessStep dialog is not visible initially", () => {
    renderComponent()

    expect(screen.getByLabelText("Aktionen")).toBeVisible()

    expect(
      screen.queryByTestId("assign-process-step-stub"),
    ).not.toBeInTheDocument()
  })

  it("opens the menu and shows the dialog when a valid menu item is clicked", async () => {
    // Arrange: Create a list of valid documentation units
    const docUnit = new DocumentUnitListEntry({
      uuid: "doc-unit-id-1",
      documentNumber: "123",
      documentationOffice: {
        id: "doc-office-id-1",
        abbreviation: "DS",
      } as DocumentationOffice,
    })
    const { user } = renderComponent({ documentationUnits: [docUnit] })

    await user.click(screen.getByLabelText("Aktionen"))
    const menuItem = screen.getByText("Weitergeben")
    expect(menuItem).toBeVisible()

    await user.click(menuItem)

    expect(screen.getByTestId("assign-process-step-stub")).toBeVisible()
  })

  describe("Validation Scenarios", () => {
    it("emits an error and does not show dialog when no documentation unit is selected", async () => {
      // Arrange
      const { user, emitted } = renderComponent({ documentationUnits: [] })

      await user.click(screen.getByLabelText("Aktionen"))
      const menuItem = screen.getByText("Weitergeben")
      expect(menuItem).toBeVisible()

      await user.click(menuItem)

      // Assert: The validation should emit an error without opening the dialog
      expect(emitted().updateSelectionErrors).toBeTruthy()
      expect(emitted().updateSelectionErrors[0]).toEqual([
        "Wählen Sie mindestens eine Dokumentationseinheit aus.",
        [],
      ])
      expect(
        screen.queryByTestId("assign-process-step-stub"),
      ).not.toBeInTheDocument()
    })

    it("emits an error for an external handover pending unit", async () => {
      const pendingDocUnit = new DocumentUnitListEntry({
        uuid: "doc-unit-id-3",
        documentNumber: "789",
        documentationOffice: {
          id: "doc-office-id-1",
          abbreviation: "DS",
        } as DocumentationOffice,
        status: {
          publicationStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
        },
      })
      const { user, emitted } = renderComponent({
        documentationUnits: [pendingDocUnit],
      })

      await user.click(screen.getByLabelText("Aktionen"))
      const menuItem = screen.getByText("Weitergeben")
      expect(menuItem).toBeVisible()
      await user.click(menuItem)

      // Assert: The validation should emit the correct error
      expect(emitted().updateSelectionErrors).toBeTruthy()
      expect(emitted().updateSelectionErrors[0]).toEqual([
        "Nehmen Sie die Fremdanlage(n) im Eingang an, um sie bearbeiten zu können.",
        ["doc-unit-id-3"],
      ])
      expect(
        screen.queryByTestId("assign-process-step-stub"),
      ).not.toBeInTheDocument()
    })

    it("emits an error when selection contains documentation unit from other docoffices", async () => {
      // Arrange
      const docUnit = new DocumentUnitListEntry({
        uuid: "doc-unit-id-1",
        documentNumber: "123",
        documentationOffice: {
          abbreviation: "BGH",
        } as DocumentationOffice,
      })
      const { user, emitted } = renderComponent({
        documentationUnits: [docUnit],
      })

      await user.click(screen.getByLabelText("Aktionen"))
      const menuItem = screen.getByText("Weitergeben")
      expect(menuItem).toBeVisible()
      await user.click(menuItem)

      expect(emitted().updateSelectionErrors).toBeTruthy()
      expect(emitted().updateSelectionErrors[0]).toEqual([
        "Dokumentationseinheiten von fremden Dokstellen können nicht bearbeitet werden.",
        ["doc-unit-id-1"],
      ])
      expect(
        screen.queryByTestId("assign-process-step-stub"),
      ).not.toBeInTheDocument()
    })
  })

  describe("handleAssignProcessStep method", () => {
    it("calls bulkAssignProcessStep and shows a toast on success", async () => {
      const docUnit = new DocumentUnitListEntry({
        uuid: "doc-unit-id-1",
        documentNumber: "123",
        documentationOffice: {
          id: "doc-office-id-1",
          abbreviation: "DS",
        } as DocumentationOffice,
      })

      const { user, emitted } = renderComponent({
        documentationUnits: [docUnit],
      })

      vi.spyOn(DocumentUnitService, "bulkAssignProcessStep").mockResolvedValue({
        status: 200,
        data: [],
      })

      await user.click(screen.getByLabelText("Aktionen"))
      const menuItem = screen.getByText("Weitergeben")
      expect(menuItem).toBeVisible()
      await user.click(menuItem)

      await user.click(screen.getByTestId("assign-button"))

      expect(DocumentUnitService.bulkAssignProcessStep).toHaveBeenCalledWith(
        mockPayload,
        ["doc-unit-id-1"],
      )
      expect(emitted().processStepAssigned).toBeTruthy()
      expect(addToastMock).toHaveBeenCalled()
      expect(
        screen.queryByTestId("assign-process-step-stub"),
      ).not.toBeInTheDocument()
    })

    it("returns an error and does not show toast on API failure", async () => {
      const docUnit = new DocumentUnitListEntry({
        uuid: "doc-unit-id-1",
        documentNumber: "123",
        documentationOffice: {
          id: "doc-office-id-1",
          abbreviation: "DS",
        } as DocumentationOffice,
      })
      const mockError: ResponseError = {
        title: "Error",
        description: "API failed",
      }
      vi.spyOn(DocumentUnitService, "bulkAssignProcessStep").mockResolvedValue({
        status: 400,
        error: mockError,
      })
      const { user, emitted } = renderComponent({
        documentationUnits: [docUnit],
      })

      await user.click(screen.getByLabelText("Aktionen"))
      const menuItem = screen.getByText("Weitergeben")
      expect(menuItem).toBeVisible()
      await user.click(menuItem)

      await user.click(screen.getByTestId("assign-button"))

      expect(DocumentUnitService.bulkAssignProcessStep).toHaveBeenCalled()
      expect(emitted().processStepAssigned).toBeFalsy()
      expect(screen.getByTestId("assign-process-step-stub")).toBeInTheDocument()
      expect(addToastMock).not.toHaveBeenCalled()
    })
  })
})
