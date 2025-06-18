import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import {
  CoreData,
  DocumentUnit,
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/documentUnit"
import routes from "~/test-helper/routes"

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
  const documentUnit = new DocumentUnit("foo", {
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
  return {
    ...render(DocumentUnitInfoPanel, {
      props: { documentUnit: documentUnit },
      global: {
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
})
