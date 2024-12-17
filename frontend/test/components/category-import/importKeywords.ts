import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import ImportKeywords from "@/components/category-import/ImportKeywords.vue"
import DocumentUnit from "@/domain/documentUnit"
import routes from "~/test-helper/routes"

function renderComponent(keywords: string[]) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(ImportKeywords, {
      props: { importableKeywords: keywords },
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
    renderComponent([])
    expect(screen.getByText("Schlagwörter")).toBeInTheDocument()
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeInTheDocument()
  })

  it("enables button with importable keywords", () => {
    renderComponent(["one"])
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeEnabled()
  })

  it("disables button without importable keywords", () => {
    renderComponent([])
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeDisabled()
  })

  it("displays error badge without importable keywords", () => {
    renderComponent([])
    expect(screen.getByText("Quellrubrik leer")).toBeInTheDocument()
  })
})
