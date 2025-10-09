import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import TextCheckModal from "@/components/text-check/TextCheckModal.vue"
import { Match } from "@/types/textCheck"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

function renderComponent(match: Match) {
  const user = userEvent.setup()

  return {
    user,
    ...render(TextCheckModal, {
      props: {
        match: match,
      },
    }),
  }
}

const baseMatch: Match = {
  word: "testword",
  offset: 0,
  length: 8,
  replacements: [{ value: "suggestion1" }, { value: "suggestion2" }],
  category: "",
  message: "This is a test message.",
  context: { text: "test context", length: 12, offset: 0 },
  sentence: "This is a test sentence.",
  shortMessage: "Test message.",
  type: { typeName: "UnknownWord" },
  rule: {
    id: "123",
    category: { id: "abc", name: "Test Category" },
    description: "Test rule description",
    issueType: "grammar",
  },
  ignoreForIncompleteSentence: true,
  id: 1,
  contextForSureMatch: 1,
}

describe("TextCheckModal", () => {
  test.skip("renders the word, message, replacements and options correctly when not ignored", () => {
    renderComponent(baseMatch)
    expect(screen.getByTestId("text-check-modal")).toBeInTheDocument()
    expect(screen.getByTestId("text-check-modal-word")).toHaveTextContent(
      "testword",
    )
    expect(screen.getByText("Test message.")).toBeInTheDocument() // shortMessage
    expect(screen.getByText("suggestion1")).toBeInTheDocument() // first suggestion
    expect(screen.getByText("suggestion2")).toBeInTheDocument() // second suggestion

    expect(
      screen.getByText("Zum globalen Wörterbuch hinzufügen"),
    ).toBeInTheDocument()
  })

  it("renders the full message if shortMessage is not available", () => {
    renderComponent({ ...baseMatch, shortMessage: "" })
    expect(screen.getByText(baseMatch.message)).toBeInTheDocument()
  })

  it("emits 'word:remove' event when word is ignored locally and remove ignored word button is clicked ", async () => {
    const { emitted, user } = renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [
        {
          type: "documentation_unit",
          word: "testword",
          id: "1",
        },
      ],
    })

    // don't render the other options
    expect(screen.queryByText("Ignorieren")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Zum globalen Wörterbuch hinzufügen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Aus globalem Wörterbuch entfernen"),
    ).not.toBeInTheDocument()

    await user.click(screen.getByText("Nicht in Dokeinheit ignorieren"))
    expect(emitted()["word:remove"]).toEqual([["testword"]])
  })

  test.skip("emits 'globalWord:add' event when the 'Zum globalen Wörterbuch hinzufügen' button is clicked", async () => {
    const { emitted, user } = renderComponent(baseMatch)
    await user.click(screen.getByText("Zum globalen Wörterbuch hinzufügen"))
    expect(emitted()["globalWord:add"]).toEqual([["testword"]])
  })

  test.skip("emits 'globalWord:remove' event when remove global ignore button is clicked", async () => {
    const { emitted, user } = renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "global", word: "testword", id: "1" }],
    })

    // don't render the other options
    expect(screen.queryByText("Ignorieren")).not.toBeInTheDocument()
    expect(screen.queryByText("Nicht ignorieren")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Zum globalen Wörterbuch hinzufügen"),
    ).not.toBeInTheDocument()

    await user.click(screen.getByText("Aus globalem Wörterbuch entfernen"))
    expect(emitted()["globalWord:remove"]).toEqual([["testword"]])
  })
})
