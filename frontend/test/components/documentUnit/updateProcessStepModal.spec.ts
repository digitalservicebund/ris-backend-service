import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Tooltip from "primevue/tooltip"
import { beforeEach } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import UpdateProcessStepDialog from "@/components/UpdateProcessStepDialog.vue"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import errorMessages from "@/i18n/errors.json"
import documentationUnitSerice from "@/services/documentUnitService"
import processStepService from "@/services/processStepService"
import routes from "~/test-helper/routes"

const currentProcessStep: DocumentationUnitProcessStep = {
  id: "c-id",
  createdAt: new Date(),
  processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
}

const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
  currentProcessStep,
  {
    id: "b-id",
    createdAt: new Date(),
    processStep: {
      uuid: "blockiert-id",
      name: "Blockiert",
      abbreviation: "B",
    },
  },
  {
    id: "a-id",
    createdAt: new Date(),
    processStep: { uuid: "neu-id", name: "Neu", abbreviation: "N" },
  },
]

function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  const documentUnit = new Decision("foo", {
    documentNumber: "1234567891234",
    currentDocumentationUnitProcessStep: currentProcessStep,
    processSteps: docUnitProcessSteps,
  })

  return {
    user,
    ...render(UpdateProcessStepDialog, {
      props: { visible: true },
      global: {
        directives: { tooltip: Tooltip },
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              docunitStore: {
                originalDocumentUnit: documentUnit,
                documentUnit: documentUnit,
              },
            },
            stubActions: false,
          }),
        ],
      },
    }),
  }
}

describe("update process step modal", () => {
  beforeEach(() => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          uuid: "qs-formal-id",
          name: "QS formal",
          abbreviation: "QS",
        },
      }),
    )
    vi.spyOn(processStepService, "getProcessSteps").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          { uuid: "neu-id", name: "Neu", abbreviation: "N" },
          { uuid: "blockiert-id", name: "Blockiert", abbreviation: "B" },
          { uuid: "qs-formal-id", name: "QS formal", abbreviation: "QS" },
        ],
      }),
    )
    vi.spyOn(documentationUnitSerice, "update").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          documentationUnitVersion: 1,
          patch: [],
          errorPaths: [],
        },
      }),
    )
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  test("shows next step and closes after updating process step", async () => {
    const { user, emitted } = renderComponent()

    // Dropdown is preselected with next step
    expect(await screen.findByLabelText("Neuer Schritt")).toHaveTextContent(
      "QS formal",
    )

    // Buttons
    expect(await screen.findByText("Weitergeben")).toBeVisible()
    expect(await screen.findByText("Abbrechen")).toBeVisible()

    // Data Table
    expect(await screen.findByRole("table")).toBeVisible()
    expect(screen.getByRole("cell", { name: "Neu" })).toBeVisible()
    expect(screen.getByRole("cell", { name: "Blockiert" })).toBeVisible()
    expect(screen.getByRole("cell", { name: "Fertig" })).toBeVisible()

    // Dialog is closed on Submit
    await user.click(await screen.findByLabelText("Weitergeben"))
    expect(emitted().onProcessStepUpdated).toBeTruthy()
  })

  test("closes after cancel", async () => {
    const { user, emitted } = renderComponent()

    // Dialog is closed on Cancel
    await user.click(await screen.findByLabelText("Abbrechen"))
    expect(emitted().onCancelled).toBeTruthy()
  })

  test("shows error modal by error on next process step endpoint", async () => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED,
      }),
    )

    renderComponent()

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Der nächste Schritt konnte nicht geladen werden.",
      ),
    ).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Wählen Sie einen Schritt aus der Liste aus oder öffnen Sie den Dialog erneut.",
      ),
    ).toBeInTheDocument()
  })

  test("shows error modal by error on get process steps endpoint", async () => {
    vi.spyOn(processStepService, "getProcessSteps").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      }),
    )

    renderComponent()

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(
      await screen.findByText("Die Schritte konnten nicht geladen werden."),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Öffnen Sie den Dialog erneut."),
    ).toBeInTheDocument()
  })

  test("shows error modal by error on bot process step endpoints", async () => {
    vi.spyOn(processStepService, "getNextProcessStep").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED,
      }),
    )
    vi.spyOn(processStepService, "getProcessSteps").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      }),
    )

    renderComponent()

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Der nächste Schritt konnte nicht geladen werden.",
      ),
    ).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Wählen Sie einen Schritt aus der Liste aus oder öffnen Sie den Dialog erneut.",
      ),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Die Schritte konnten nicht geladen werden."),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Öffnen Sie den Dialog erneut."),
    ).toBeInTheDocument()
  })

  test("shows error modal by error on update of documentation unit (Weitergeben)", async () => {
    vi.spyOn(documentationUnitSerice, "update").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error:
          errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      }),
    )

    const { user } = renderComponent()
    await user.click(await screen.findByLabelText("Weitergeben"))

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Die Dokumentationseinheit konnte nicht weitergegeben werden.",
      ),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Versuchen Sie es erneut."),
    ).toBeInTheDocument()
  })
})
