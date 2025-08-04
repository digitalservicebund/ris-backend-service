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
  userId: "user2-id",
  createdAt: new Date(),
  processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
}

const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
  currentProcessStep,
  {
    id: "b-id",
    userId: "user1-id",
    createdAt: new Date(),
    processStep: {
      uuid: "blockiert-id",
      name: "Blockiert",
      abbreviation: "B",
    },
  },
  {
    id: "a-id",
    userId: "user1-id",
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

    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(await screen.findByText("QS formal")).toBeInTheDocument()

    await user.click(await screen.findByLabelText("Weitergeben"))
    expect(emitted().closeDialog).toBeTruthy()
  })

  test("closes after cancel", async () => {
    const { user, emitted } = renderComponent()

    await user.click(await screen.findByLabelText("Abbrechen"))
    expect(emitted().closeDialog).toBeTruthy()
  })
})
