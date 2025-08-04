import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Tooltip from "primevue/tooltip"
import { createRouter, createWebHistory } from "vue-router"
import UpdateProcessStepDialog from "@/components/UpdateProcessStepDialog.vue"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import processStepService from "@/services/processStepService"
import routes from "~/test-helper/routes"

const user = userEvent.setup()

function renderComponent() {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
    {
      id: "c-id",
      userId: "user2-id",
      createdAt: new Date(),
      processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
    },
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

  const documentUnit = new Decision("foo", {
    documentNumber: "1234567891234",
    coreData: {
      court: {
        type: "AG",
        location: "Test",
        label: "AG Test",
      },
    },
    managementData: {
      borderNumbers: [],
      duplicateRelations: [],
    },
    currentProcessStep: docUnitProcessSteps.at(0),
    processSteps: docUnitProcessSteps,
  })

  return {
    ...render(UpdateProcessStepDialog, {
      global: {
        directives: { tooltip: Tooltip },
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              session: {
                user: {
                  roles: ["Internal"],
                },
              },
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

it("renders update proccess step button", async () => {
  renderComponent()
  expect(
    await screen.findByLabelText("Dokumentationseinheit weitergeben"),
  ).toBeInTheDocument()
})

it("renders proccess steps modal", async () => {
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

  renderComponent()
  // Open update proccess step dialog
  await user.click(
    await screen.findByLabelText("Dokumentationseinheit weitergeben"),
  )
  expect(
    await screen.findByText("Dokumentationseinheit weitergeben"),
  ).toBeInTheDocument()
  expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
  expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
  expect(await screen.findByText("QS formal")).toBeInTheDocument()

  // Move process -> Dialog is closed
  await user.click(await screen.findByLabelText("Weitergeben"))
  expect(
    screen.queryByText("Dokumentationseinheit weitergeben"),
  ).not.toBeInTheDocument()

  // Cancel  -> Dialog is closed
  await user.click(
    await screen.findByLabelText("Dokumentationseinheit weitergeben"),
  )
  expect(
    await screen.findByText("Dokumentationseinheit weitergeben"),
  ).toBeInTheDocument()
  await user.click(await screen.findByLabelText("Abbrechen"))
  expect(
    screen.queryByText("Dokumentationseinheit weitergeben"),
  ).not.toBeInTheDocument()
})
