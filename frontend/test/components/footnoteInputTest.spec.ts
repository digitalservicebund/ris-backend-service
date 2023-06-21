import { render, screen } from "@testing-library/vue"
import { describe, test } from "vitest"
import FootnoteInput from "@/components/footnotes/FootnoteInput.vue"

describe("FootnoteInput", () => {
  test("renders footnote component with default hint message", async () => {
    render(FootnoteInput, {
      props: {
        modelValue: undefined,
      },
    })

    screen.getByText(
      "Sie können mit # den Fußnoten-Typ wählen (z.B. Änderungsfußnote, Kommentierende Fußnote)"
    )
  })
})
