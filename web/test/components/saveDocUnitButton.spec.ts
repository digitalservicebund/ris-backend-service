import { fireEvent, render, screen } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import SaveDocUnitButton from "@/components/SaveDocUnitButton.vue"

describe("SaveDocUnitButton", () => {
  const vuetify = createVuetify({ components, directives })
  it("renders frist time", async () => {
    const { emitted } = render(SaveDocUnitButton, {
      global: { plugins: [vuetify] },
      props: {
        ariaLabel: "Foo",
        updateStatus: 0,
      },
    })
    const saveButton = screen.getByRole("button")
    expect(saveButton).toBeTruthy()
    expect(saveButton.textContent?.replace(/\s/g, "")).toEqual("Speichern")
    expect(saveButton.getAttribute("aria-label")).toEqual("Foo")
    await fireEvent.click(saveButton)
    expect(emitted().updateDocUnit).toBeTruthy()
  })
  it("renders with on update status", async () => {
    const { getByText } = await render(SaveDocUnitButton, {
      global: { plugins: [vuetify] },
      props: {
        ariaLabel: "Foo",
        updateStatus: 1,
      },
    })
    getByText("Daten werden gespeichert")
  })
  it("renders with has update error status", async () => {
    const { getByText } = await render(SaveDocUnitButton, {
      global: { plugins: [vuetify] },
      props: {
        ariaLabel: "Foo",
        updateStatus: 400,
      },
    })
    getByText("Fehler beim Speichern")
  })
  it("renders with update succeed status", async () => {
    const { getByText } = await render(SaveDocUnitButton, {
      global: { plugins: [vuetify] },
      props: {
        ariaLabel: "Foo",
        updateStatus: 200,
      },
    })
    const label = getByText("Zuletzt gespeichert um")
    expect(label.firstElementChild?.className).toEqual("on-succeed")
  })
})
