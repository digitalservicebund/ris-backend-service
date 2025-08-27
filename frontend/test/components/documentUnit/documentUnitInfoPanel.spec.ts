import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Tooltip from "primevue/tooltip"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import { CoreData } from "@/domain/coreData"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import { DuplicateRelation, DuplicateRelationStatus } from "@/domain/managementData"
import routes from "~/test-helper/routes"

const currentDocumentationUnitProcessStep: DocumentationUnitProcessStep = {
  id: "c-id",
  createdAt: new Date(),
  processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
  user: { id: "user-id", name: "Test User", initials: "TU" },
}

const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
  currentDocumentationUnitProcessStep,
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

function renderComponent(options?: {
  documentNumber?: string
  coreData?: CoreData
  duplicateRelations?: DuplicateRelation[]
  isExternalUser?: boolean
}) {
  const user = userEvent.setup()
  vi.mock("primevue/usetoast", () => ({
    useToast: () => ({ add: vi.fn() }),
  }))
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
    currentDocumentationUnitProcessStep: currentDocumentationUnitProcessStep,
    processSteps: docUnitProcessSteps,
  })

  return {
    user,
    ...render(DocumentUnitInfoPanel, {
      props: { documentUnit: documentUnit },
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

  it("renders proccess steps, userand move button", async () => {
    renderComponent()
    expect(screen.queryByText("N")).not.toBeInTheDocument()
    expect(screen.queryByText("Neu")).not.toBeInTheDocument()
    expect(await screen.findByText("B")).toBeInTheDocument()
    expect(screen.queryByText("Blockiert")).not.toBeInTheDocument()
    expect(await screen.findByText("Fertig")).toBeInTheDocument()
    expect(screen.queryByText("F")).not.toBeInTheDocument()
    expect(await screen.findByText("TU")).toBeInTheDocument()
  })
})
