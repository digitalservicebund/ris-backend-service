import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { describe, test } from "vitest"
import FootnoteInput from "@/components/footnotes/FootnoteInput.vue"
import { Footnote } from "@/components/footnotes/types"

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

  test("renders footnote component with received footnotes", async () => {
    const footnote = {
      FOOTNOTE: [
        { FOOTNOTE_REFERENCE: ["§ 7 Abs. 1a Satz 1 u. 2"] },
        { FOOTNOTE_CHANGE: ["eine ganze Menge Text"] },
        { FOOTNOTE_EU_LAW: ["irgendwas halt"] },
        { FOOTNOTE_STATE_LAW: ["state law"] },
        { FOOTNOTE_COMMENT: ["einfach nur ein Kommentar"] },
        { FOOTNOTE_DECISION: ["das wurde halt so entschieden"] },
        { FOOTNOTE_OTHER: ["ach nochmal eben etwas"] },
        { FOOTNOTE_OTHER: ["ach nochmal eben etwas2"] },
      ],
    } as Footnote

    render(FootnoteInput, {
      props: {
        modelValue: footnote,
      },
    })

    await flushPromises()

    expect(
      screen.getByText("§ 7 Abs. 1a Satz 1 u. 2", { exact: false })
    ).toBeInTheDocument()

    expect(
      screen.getByText("eine ganze Menge Text", { exact: false })
    ).toBeInTheDocument()

    expect(
      screen.getByText("irgendwas halt", { exact: false })
    ).toBeInTheDocument()

    expect(screen.getByText("state law", { exact: false })).toBeInTheDocument()

    expect(
      screen.getByText("einfach nur ein Kommentar", { exact: false })
    ).toBeInTheDocument()

    expect(
      screen.getByText("das wurde halt so entschieden", { exact: false })
    ).toBeInTheDocument()

    expect(
      screen.getByText("ach nochmal eben etwas", { exact: false })
    ).toBeInTheDocument()

    expect(
      screen.getByText("ach nochmal eben etwas2", { exact: false })
    ).toBeInTheDocument()
  })
})
