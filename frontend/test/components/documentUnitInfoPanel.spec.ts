import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit, { CoreData } from "@/domain/documentUnit"

function renderComponent(options?: { heading?: string; coreData?: CoreData }) {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw/documentUnit/new",
        name: "new",
        component: {},
      },
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {
          template: "<div data-testid='files'>Categories</div>",
        },
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {
          template: "<div data-testid='files'>Files</div>",
        },
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/publication",
        name: "caselaw-documentUnit-documentNumber-publication",
        component: {
          template: "<div data-testid='publication'>Publication</div>",
        },
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/preview",
        name: "caselaw-documentUnit-documentNumber-preview",
        component: {
          template: "<div data-testid='preview'>Preview</div>",
        },
      },
    ],
  })
  return {
    ...render(DocumentUnitInfoPanel, {
      props: { heading: options?.heading ?? "" },
      global: {
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              docunitStore: {
                documentUnit: new DocumentUnit("foo", {
                  documentNumber: "1234567891234",
                  coreData: options?.coreData ?? {
                    court: {
                      type: "AG",
                      location: "Test",
                      label: "AG Test",
                    },
                  },
                }),
              },
            },
          }),
        ],
      },
    }),
  }
}

describe("documentUnit InfoPanel", () => {
  it("renders heading if given", async () => {
    renderComponent({ heading: "test heading" })

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
})
