import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Tooltip from "primevue/tooltip"
import { createRouter, createWebHistory } from "vue-router"
import UpdateProcessStepDialog from "@/components/UpdateProcessStepDialog.vue"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
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
    currentProcessStep: currentProcessStep,
    processSteps: docUnitProcessSteps,
  })

  return {
    user,
    ...render(UpdateProcessStepDialog, {
      props: { showDialog: true },
      global: {
        directives: { tooltip: Tooltip },
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              docunitStore: {
                documentUnit: documentUnit,
              },
            },
          }),
        ],
      },
    }),
  }
}

describe("update process step modal", () => {
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
    await expect(screen.getByRole("cell", { name: "Neu" })).toBeVisible()
    await expect(screen.getByRole("cell", { name: "Blockiert" })).toBeVisible()
    await expect(screen.getByRole("cell", { name: "Fertig" })).toBeVisible()

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
})
