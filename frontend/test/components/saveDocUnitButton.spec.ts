import { fireEvent, render, screen } from "@testing-library/vue"
import SaveDocumentUnitButton from "@/components/SaveDocumentUnitButton.vue"

describe("SaveDocumentUnitButton", () => {
  it("renders frist time", async () => {
    const { emitted } = render(SaveDocumentUnitButton, {
      props: {
        ariaLabel: "Foo",
        updateStatus: 0,
      },
    })
    const saveButton = screen.getByRole("button")
    expect(saveButton).toBeInTheDocument()
    expect(saveButton.textContent?.replace(/\s/g, "")).toEqual("Speichern")
    expect(saveButton).toHaveAttribute("aria-label", "Foo")
    await fireEvent.click(saveButton)
    expect(emitted().updateDocumentUnit).toBeTruthy()
  })
  it("renders with on update status", async () => {
    await render(SaveDocumentUnitButton, {
      props: {
        ariaLabel: "Foo",
        updateStatus: 1,
      },
    })
    screen.getByText("Daten werden gespeichert")
  })
  it("renders with has update error status", async () => {
    await render(SaveDocumentUnitButton, {
      props: {
        ariaLabel: "Foo",
        updateStatus: 400,
      },
    })
    screen.getByText("Fehler beim Speichern")
  })
  it("renders feedback when saving was successful", async () => {
    await render(SaveDocumentUnitButton, {
      props: {
        ariaLabel: "Foo",
        updateStatus: 200,
      },
    })
    expect(screen.getByText("Zuletzt gespeichert um", { exact: false }))
      .toBeVisible
  })
})
