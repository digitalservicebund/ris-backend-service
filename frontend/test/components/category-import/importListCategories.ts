import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { beforeEach } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import ImportListCategories from "@/components/category-import/ImportListCategories.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import NormReference from "@/domain/normReference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

function renderComponent(
  keywords?: string[],
  fieldsOfLaw?: FieldOfLaw[],
  norms?: NormReference[],
) {
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
        importableNorms: norms,
      },
      global: {
        plugins: [[router]],
      },
    }),
  }
}

describe("ImportKeywords", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })
  it("renders component", () => {
    renderComponent()
    expect(screen.getByText("Schlagwörter")).toBeInTheDocument()
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeInTheDocument()
    expect(screen.getByText("Sachgebiete")).toBeInTheDocument()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeInTheDocument()
    expect(screen.getByText("Normen")).toBeInTheDocument()
    expect(screen.getByLabelText("Normen übernehmen")).toBeInTheDocument()
  })

  it("enables buttons with importable data", () => {
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
      [
        new NormReference({
          normAbbreviation: {
            abbreviation: "abc",
          },
          singleNorms: [],
          normAbbreviationRawValue: "",
        }),
      ],
    )
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeEnabled()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeEnabled()
    expect(screen.getByLabelText("Normen übernehmen")).toBeEnabled()
  })

  it("disables buttons without importable data", () => {
    renderComponent()
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeDisabled()
    expect(screen.getByLabelText("Sachgebiete übernehmen")).toBeDisabled()
    expect(screen.getByLabelText("Normen übernehmen")).toBeDisabled()
  })

  it("displays empty data badges without importable data", () => {
    renderComponent()
    expect(screen.getByTestId("Schlagwörter-empty")).toBeInTheDocument()
    expect(screen.getByTestId("Sachgebiete-empty")).toBeInTheDocument()
    expect(screen.getByTestId("Normen-empty")).toBeInTheDocument()
  })

  it("shows error modal if data could not be saved", async () => {
    const mockedStore = useDocumentUnitStore()
    vi.spyOn(mockedStore, "updateDocumentUnit").mockResolvedValue({
      status: 400,
      error: { title: "error" },
    })
    const { user } = renderComponent(["one"])
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeEnabled()
    await user.click(screen.getByLabelText("Schlagwörter übernehmen"))
    expect(
      await screen.findByText("Fehler beim Speichern der Schlagwörter"),
    ).toBeVisible()
  })

  it("shows success badge on successful import", async () => {
    const mockedStore = useDocumentUnitStore()
    vi.spyOn(mockedStore, "updateDocumentUnit").mockResolvedValue({
      status: 200,
      data: { documentationUnitVersion: 1, patch: [], errorPaths: [] },
    })
    const { user } = renderComponent(["one"])
    expect(screen.getByLabelText("Schlagwörter übernehmen")).toBeEnabled()
    await user.click(screen.getByLabelText("Schlagwörter übernehmen"))
    expect(await screen.findByText("Übernommen")).toBeVisible()
  })
})
