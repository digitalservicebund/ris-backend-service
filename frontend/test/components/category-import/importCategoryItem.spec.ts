import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import ImportCategoryItem from "@/components/category-import/ImportCategoryItem.vue"
import routes from "~/test-helper/routes"

function renderComponent(
  label: string,
  importable: boolean,
  importSuccess: boolean,
  errorMessage?: string,
) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(ImportCategoryItem, {
      props: {
        label: label,
        importable: importable,
        importSuccess: importSuccess,
        errorMessage: errorMessage,
      },
      global: { plugins: [[router]] },
    }),
  }
}

describe("ImportCategoryItem", () => {
  it("renders component initial state", async () => {
    renderComponent("Importrubrik", true, false)

    expect(screen.getByText("Importrubrik")).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Importrubrik übernehmen" }),
    ).toBeInTheDocument()
  })

  it("emits on button click", async () => {
    const { emitted, user } = renderComponent("Importrubrik", true, false)

    await user.click(
      screen.getByRole("button", { name: "Importrubrik übernehmen" }),
    )

    expect(emitted()["import"]).toBeTruthy()
  })

  it("displays success", async () => {
    renderComponent("Importrubrik", true, true)
    expect(screen.getByText("Übernommen")).toBeInTheDocument()
  })

  it("displays non importable", async () => {
    renderComponent("Importrubrik", false, false)
    expect(screen.getByText("Quellrubrik leer")).toBeInTheDocument()
  })

  it("displays error", async () => {
    renderComponent("Importrubrik", true, false, "error message")
    expect(screen.getByText("error message")).toBeInTheDocument()
  })
})
