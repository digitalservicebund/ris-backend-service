import { fireEvent, render, screen } from "@testing-library/vue"
import { it } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import documentUnitService from "@/services/documentUnitService"
import routes from "~/test-helper/routes"

describe("DocumentUnitDeleteButton", () => {
  const deleteMock = vi.spyOn(documentUnitService, "delete")
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should show a confirm dialog when the delete button is clicked", async () => {
    renderDeleteButton()

    const button = screen.getByRole("button", {
      name: "Dokumentationseinheit löschen",
    })
    expect(button).toBeVisible()
    await fireEvent.click(button)

    screen.getByText(
      "Möchten Sie die Dokumentationseinheit DS123 wirklich dauerhaft löschen?",
    )

    expect(deleteMock).not.toHaveBeenCalled()
  })

  it("should not delete the doc unit when the dialog is cancelled", async () => {
    renderDeleteButton()

    const button = screen.getByRole("button", {
      name: "Dokumentationseinheit löschen",
    })
    await fireEvent.click(button)

    const cancelButton = screen.getByRole("button", { name: "Abbrechen" })
    expect(cancelButton).toBeVisible()

    await fireEvent.click(cancelButton)

    expect(
      screen.queryByRole("dialog", { name: "Dokumentationseinheit löschen" }),
    ).not.toBeInTheDocument()

    expect(deleteMock).not.toHaveBeenCalled()
  })

  it("should delete the doc unit when the dialog is confirmed", async () => {
    const { router } = renderDeleteButton()
    const routerPushSpy = vi.spyOn(router, "push")

    deleteMock.mockResolvedValue({ status: 200, data: "success" })

    const button = screen.getByRole("button", {
      name: "Dokumentationseinheit löschen",
    })
    await fireEvent.click(button)

    const confirmDeleteButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmDeleteButton).toBeVisible()

    await fireEvent.click(confirmDeleteButton)

    expect(deleteMock).toHaveBeenCalledOnce()
    expect(deleteMock).toHaveBeenCalledWith("123456")
    expect(routerPushSpy).toHaveBeenCalledWith({ path: "/" })
  })

  it("should show an alert if the deletion fails", async () => {
    renderDeleteButton()
    deleteMock.mockResolvedValue({
      status: 500,
      data: "error explained" as never,
      error: { title: "" },
    })

    const button = screen.getByRole("button", {
      name: "Dokumentationseinheit löschen",
    })
    await fireEvent.click(button)

    const confirmDeleteButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmDeleteButton).toBeVisible()

    await fireEvent.click(confirmDeleteButton)

    expect(deleteMock).toHaveBeenCalledOnce()
    expect(deleteMock).toHaveBeenCalledWith("123456")

    expect(
      screen.queryByRole("dialog", { name: "Dokumentationseinheit löschen" }),
    ).not.toBeInTheDocument()

    expect(
      screen.getByRole("dialog", {
        name: "Fehler beim Löschen der Dokumentationseinheit",
      }),
    ).toHaveTextContent("error explained")

    await fireEvent.click(screen.getByRole("button", { name: "OK" }))

    expect(
      screen.queryByRole("dialog", {
        name: "Fehler beim Löschen der Dokumentationseinheit",
      }),
    ).not.toBeInTheDocument()
  })

  function renderDeleteButton() {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    render(DocumentUnitDeleteButton, {
      props: {
        documentNumber: "DS123",
        uuid: "123456",
      },
      global: {
        plugins: [router],
      },
    })
    return { router }
  }
})
