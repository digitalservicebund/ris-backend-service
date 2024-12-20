import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import ImportListCategories from "@/components/category-import/ImportListCategories.vue"
import DocumentUnit from "@/domain/documentUnit"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import routes from "~/test-helper/routes"

function renderComponent(keywords?: string[], fieldsOfLaw?: FieldOfLaw[]) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(ImportListCategories, {
      props: {
        importableKeywords: keywords,
        importableFieldsOfLaw: fieldsOfLaw,
      },
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      keywords: ["foo", "bar"],
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
          [router],
        ],
      },
    }),
  }
}

describe("ImportKeywords", () => {
  it("renders component", () => {
    renderComponent([], [])
    expect(screen.getByText("Schlagwörter")).toBeInTheDocument()
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeInTheDocument()
    expect(screen.getByText("Sachgebiete")).toBeInTheDocument()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeInTheDocument()
  })

  it("enables button with importable keywords", () => {
    renderComponent(
      ["one"],
      [
        {
          identifier: "AB-01",
          text: "Sachgebiet 1-2-3",
          norms: [],
          children: [],
          hasChildren: false,
        },
      ],
    )
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeEnabled()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeEnabled()
  })

  it("disables buttons without importable data", () => {
    renderComponent([], [])
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeDisabled()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeDisabled()
  })

  it("displays error badges without importable data", () => {
    renderComponent([], [])
    expect(screen.getByTestId("Schlagwörter-empty")).toBeInTheDocument()
    expect(screen.getByTestId("Sachgebiete-empty")).toBeInTheDocument()
  })
})
