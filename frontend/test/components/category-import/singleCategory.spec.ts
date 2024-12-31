import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import SingleCategory from "@/components/category-import/SingleCategory.vue"
import { ValidationError } from "@/components/input/types"
import routes from "~/test-helper/routes"

function renderComponent(options?: {
  label?: string
  errorMessage?: ValidationError | undefined
  hasContent?: boolean
  handleImport?: () => void
  importable?: boolean
}) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(SingleCategory, {
      props: {
        label: options?.label ?? "Testrubrik",
        errorMessage: options?.errorMessage ?? undefined,
        hasContent: options?.hasContent ?? true,
        handleImport: options?.handleImport ?? vi.fn(),
        importable: options?.importable ?? true,
      },
      global: { plugins: [[router]] },
    }),
  }
}

describe("SingleCategory", () => {
  it("renders component initial state", async () => {
    renderComponent()

    expect(screen.getByText("Testrubrik")).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Testrubrik übernehmen" }),
    ).toBeInTheDocument()
  })

  it("button disabled, if no content", async () => {
    renderComponent({ hasContent: false })

    expect(
      screen.getByRole("button", { name: "Testrubrik übernehmen" }),
    ).toBeDisabled()
    expect(screen.getByText("Quellrubrik leer")).toBeInTheDocument()
  })

  it("displays error message", async () => {
    renderComponent({ errorMessage: { instance: "test", message: "Error" } })

    expect(screen.getByText("Error")).toBeInTheDocument()
  })

  it("displays success", async () => {
    const { user } = renderComponent()
    await user.click(
      screen.getByRole("button", { name: "Testrubrik übernehmen" }),
    )
    expect(screen.getByText("Übernommen")).toBeInTheDocument()
  })

  it("displays target category filled alert", async () => {
    renderComponent({ importable: false })
    expect(screen.getByText("Zielrubrik ausgefüllt")).toBeInTheDocument()
  })
})
