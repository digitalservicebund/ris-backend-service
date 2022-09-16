import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("Texts", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders all text fields with labels", async () => {
    const { getByText } = render(DocumentUnitTexts, {
      global: { plugins: [vuetify] },
      props: {
        texts: new DocumentUnit("foo").texts,
        updateStatus: 0,
      },
    })

    getByText("Entscheidungsname")
    getByText("Titelzeile")
    getByText("Leitsatz")
    getByText("Orientierungssatz")
    getByText("Tenor")
    getByText("Gründe")
    getByText("Tatbestand")
    getByText("Entscheidungsgründe")
  })

  test("emits update DocumentUnit event", async () => {
    const user = userEvent.setup()
    const { getByLabelText, emitted } = render(DocumentUnitTexts, {
      global: { plugins: [vuetify] },
      props: {
        texts: new DocumentUnit("foo").texts,
        updateStatus: 0,
      },
    })

    await user.click(getByLabelText("Kurz- und Langtexte Speichern Button"))
    expect(emitted().updateDocumentUnit).toBeTruthy()
  })

  test.todo("change in value emits updateValue event", async () => {
    // const user = userEvent.setup()
    // const { getByText, getByLabelText, emitted } = render(DocumentUnitTexts, {
    //   global: { plugins: [vuetify] },
    //   props: {
    //     texts: new DocumentUnit("foo", { titelzeile: "foo Titelzeile" }).texts,
    //     updateStatus: 0,
    //   },
    // })
    // await user.click(getByLabelText("Titelzeile Editor Feld"))
    // await user.type(getByText("foo Titelzeile"), "new titel{tab}")
    // expect(emitted().updateValue).toBeCalledWith([
    //   "titelzeile" as keyof Texts,
    //   "new titel",
    // ])
    // expect(emitted().updateDocumentUnit).toBeFalsy()
  })
})
