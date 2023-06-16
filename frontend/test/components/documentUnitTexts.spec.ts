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
})
