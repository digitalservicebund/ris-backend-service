import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { beforeEach } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import CategoryImport from "@/components/category-import/CategoryImport.vue"
import { DocumentUnit } from "@/domain/documentUnit"
import { PublicationState } from "@/domain/publicationStatus"
import documentUnitService from "@/services/documentUnitService"
import routes from "~/test-helper/routes"

function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(CategoryImport, { global: { plugins: [[router]] } }),
  }
}

describe("CategoryImport", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })
  it("renders component initial state", () => {
    renderComponent()

    expect(screen.getByText("Rubriken importieren")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeInTheDocument()
  })

  it("enables button when typing document number with valid length", async () => {
    const { user } = renderComponent()

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeDisabled()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
  })

  it("displays error when no document unit found", async () => {
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () => Promise.resolve({ status: 400, error: { title: "error" } }),
    )

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    await user.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    expect(
      screen.getByText("Keine Dokumentationseinheit gefunden."),
    ).toBeInTheDocument()
  })

  it("displays core data when document unit found", async () => {
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () =>
        Promise.resolve({
          status: 200,
          data: new DocumentUnit("foo", {
            documentNumber: "XXRE123456789",
            status: {
              publicationStatus: PublicationState.UNPUBLISHED,
            },
            coreData: {
              court: {
                label: "AG Aachen",
              },
              fileNumbers: ["file-123"],
              decisionDate: "2022-02-01",
            },
          }),
        }),
    )

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    await user.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    expect(screen.getByText("XXRE123456789")).toBeInTheDocument()
    expect(screen.getByText(/AG Aachen, 01.02.2022, file-123,/)).toBeVisible()
    expect(screen.getByText("Unveröffentlicht")).toBeInTheDocument()
  })
})
