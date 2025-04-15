import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { Match } from "@/types/textCheck"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

function renderComponent(match: Match) {
  const user = userEvent.setup()

  return {
    user,
    ...render(IgnoredWordHandler, {
      props: {
        match: match,
      },
    }),
  }
}

describe("IgnoredWordHandler", () => {
  const baseMatch: Match = {
    word: "testword",
    offset: 0,
    length: 8,
    replacements: [],
    category: "",
    message: "",
    context: { text: "", length: 0, offset: 0 },
    sentence: "",
    shortMessage: "",
    type: { typeName: "" },
    rule: {
      id: "",
      category: { id: "", name: "" },
      description: "",
      issueType: "",
    },
    ignoreForIncompleteSentence: true,
    id: 1,
    contextForSureMatch: 1,
  }

  it("does not render any button when ignoredTextCheckWords is undefined", () => {
    renderComponent({ ...baseMatch, ignoredTextCheckWords: undefined })
    expect(
      screen.queryByText("Aus globalem Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Nicht Ignorieren")).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()
  })

  it("emits remove local ignore word event when 'Nicht ignorieren' button is clicked", async () => {
    const { emitted, user } = renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "documentation_unit", word: "testword" }],
    })
    // other options should not be rendered
    expect(screen.getByText("Nicht ignorieren")).toBeInTheDocument()
    expect(
      screen.queryByText("Aus globalem Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()

    await user.click(screen.getByText("Nicht ignorieren"))
    expect(emitted()["ignored-word:remove"]).toEqual([["testword"]])
    expect(emitted()["globally-ignored-word:remove"]).toBeUndefined()
  })

  it("renders 'Von jDV ignoriert' when word is globally ignored by jDV", () => {
    renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "global_jdv", word: "testword" }],
    })
    expect(screen.getByText("Von jDV ignoriert")).toBeInTheDocument()
    expect(
      screen.queryByText("Aus globalem Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Nicht Ignorieren")).not.toBeInTheDocument()
  })

  // unable to mock feature toggle
  test.skip("emits remove global ignore word event when 'Aus globalem Wörterbuch entfernen' button is clicked", async () => {
    const { emitted, user } = renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "global", word: "testword" }],
    })
    // other options should not be rendered
    expect(
      screen.getByText("Aus globalem Wörterbuch entfernen"),
    ).toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()
    expect(screen.queryByText("Nicht Ignorieren")).not.toBeInTheDocument()

    await user.click(screen.getByText("Aus globalem Wörterbuch entfernen"))
    expect(emitted()["globally-ignored-word:remove"]).toEqual([["testword"]])
    expect(emitted()["ignored-word:remove"]).toBeUndefined()
  })
})
