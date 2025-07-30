import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Tooltip from "primevue/tooltip"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import { CoreData } from "@/domain/coreData"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/managementData"
import processStepService from "@/services/processStepService"
import routes from "~/test-helper/routes"

const user = userEvent.setup()

function renderComponent(options?: {
  documentNumber?: string
  coreData?: CoreData
  duplicateRelations?: DuplicateRelation[]
  isExternalUser?: boolean
}) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  const documentUnit = new Decision("foo", {
    documentNumber: options?.documentNumber ?? "1234567891234",
    coreData: options?.coreData ?? {
      court: {
        type: "AG",
        location: "Test",
        label: "AG Test",
      },
    },
    managementData: {
      borderNumbers: [],
      duplicateRelations: options?.duplicateRelations ?? [],
    },
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

  return {
    ...render(DocumentUnitInfoPanel, {
      props: { documentUnit: documentUnit, processSteps: docUnitProcessSteps },
      global: {
        directives: { tooltip: Tooltip },
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              session: {
                user: {
                  roles: [options?.isExternalUser ? "External" : "Internal"],
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

describe("documentUnit InfoPanel", () => {
  it("renders documentNumber as heading", async () => {
    renderComponent({ documentNumber: "test heading" })

    screen.getAllByText("test heading")
  })

  it("renders all given property infos in correct order", async () => {
    const coreData = {
      decisionDate: "2024-01-01",
      fileNumbers: ["AZ123"],
      court: {
        type: "AG",
        location: "Test",
        label: "AG Test",
      },
    }
    renderComponent({ coreData: coreData })

    expect(
      await screen.findByText("AG Test, AZ123, 01.01.2024"),
    ).toBeInTheDocument()
  })

  it("omits incomplete coredata fields from rendering", async () => {
    renderComponent()

    expect(await screen.findByText("AG Test")).toBeInTheDocument()
  })

  it("renders a duplicate warning with link if there are pending duplicates", async () => {
    renderComponent({
      duplicateRelations: [
        {
          status: DuplicateRelationStatus.PENDING,
          documentNumber: "doc",
          isJdvDuplicateCheckActive: true,
        },
      ],
    })

    expect(await screen.findByText("Dublettenverdacht")).toBeInTheDocument()
    expect(screen.getByRole("link")).toHaveTextContent("Bitte prüfen")
  })

  it("renders a duplicate warning without link for external user", async () => {
    renderComponent({
      isExternalUser: true,
      duplicateRelations: [
        {
          status: DuplicateRelationStatus.PENDING,
          documentNumber: "doc",
          isJdvDuplicateCheckActive: true,
        },
      ],
    })

    expect(await screen.findByText("Dublettenverdacht")).toBeInTheDocument()
    expect(screen.queryByRole("link")).not.toBeInTheDocument()
  })

  it("renders no duplicate warning if there are ignored duplicates", async () => {
    renderComponent({
      duplicateRelations: [
        {
          status: DuplicateRelationStatus.IGNORED,
          documentNumber: "doc",
          isJdvDuplicateCheckActive: true,
        },
      ],
    })

    expect(screen.queryByText("Dublettenverdacht")).not.toBeInTheDocument()
  })

  it("renders proccess steps and move button", async () => {
    renderComponent()
    expect(screen.queryByText("N")).not.toBeInTheDocument()
    expect(screen.queryByText("Neu")).not.toBeInTheDocument()
    expect(await screen.findByText("B")).toBeInTheDocument()
    expect(screen.queryByText("Blockiert")).not.toBeInTheDocument()
    expect(await screen.findByText("Fertig")).toBeInTheDocument()
    expect(screen.queryByText("F")).not.toBeInTheDocument()
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
    vi.spyOn(processStepService, "moveToNextProcessStep").mockImplementation(
      () =>
        Promise.resolve({
          status: 200,
          data: {
            id: "new-step-id",
            userId: "user1-id",
            createdAt: new Date(),
            processStep: {
              uuid: "qs-formal-id",
              name: "QS formal",
              abbreviation: "QS",
            },
          },
        }),
    )

    renderComponent()
    // Open move proccess step dialog
    await user.click(
      await screen.findByLabelText("Dokumentationseinheit weitergeben"),
    )
    expect(
      await screen.findByText("Dokumentationseinheit weitergeben"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Weitergeben")).toBeInTheDocument()
    expect(await screen.findByText("Abbrechen")).toBeInTheDocument()
    expect(await screen.findByText("QS formal")).toBeInTheDocument()

    // Move process
    await user.click(await screen.findByLabelText("Weitergeben"))
    expect(
      screen.queryByText("Dokumentationseinheit weitergeben"),
    ).not.toBeInTheDocument()
    // TODO new process step is not updated immediately
    // expect(screen.queryByText("Fertig")).not.toBeInTheDocument()
    // expect(await screen.findByText("F")).toBeInTheDocument()
    // expect(await screen.findByText("QS formal")).toBeInTheDocument()

    // Cancel
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
})
