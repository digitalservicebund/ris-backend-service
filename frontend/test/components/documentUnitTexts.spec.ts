import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("Texts", () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  test("renders all text fields with labels", async () => {
    render(DocumentUnitTexts, {
      props: {
        texts: new DocumentUnit("foo").texts,
        updateStatus: 0,
      },
    })

    screen.getByText("Entscheidungsname")
    screen.getByText("Titelzeile")
    screen.getByText("Leitsatz")
    screen.getByText("Orientierungssatz")
    screen.getByText("Tenor")
    screen.getByText("Gründe")
    screen.getByText("Tatbestand")
    screen.getByText("Entscheidungsgründe")
  })

  test("emits update DocumentUnit event", async () => {
    const user = userEvent.setup()
    const { emitted } = render(DocumentUnitTexts, {
      props: {
        texts: new DocumentUnit("foo").texts,
        updateStatus: 0,
      },
    })

    await user.click(
      screen.getByLabelText("Kurz- und Langtexte Speichern Button")
    )
    expect(emitted().updateDocumentUnit).toBeTruthy()
  })

  test.todo("change in value emits updateValue event", async () => {
    // const user = userEvent.setup()
    // const { getByText, getByLabelText, emitted } = render(DocumentUnitTexts, {
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
