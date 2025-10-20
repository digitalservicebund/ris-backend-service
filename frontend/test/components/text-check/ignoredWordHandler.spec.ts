import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { Match } from "@/types/textCheck"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

async function renderComponent(match: Match) {
  const user = userEvent.setup()

  await flushPromises()
  return {
    user,
    ...render(IgnoredWordHandler, {
      props: {
        match: match,
        ignoredLocally: false,
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

  it("emits add local ignore word event when 'In Dokeinheit ignorieren' button is clicked", async () => {
    const { emitted, user } = await renderComponent(baseMatch)

    expect(screen.getByText("In Dokeinheit ignorieren")).toBeInTheDocument()
    await user.click(screen.getByText("In Dokeinheit ignorieren"))
    expect(emitted()["ignored-word:add"]).toBeTruthy()
  })

  it("does not render any button when ignoredTextCheckWords is undefined", async () => {
    await renderComponent({ ...baseMatch, ignoredTextCheckWords: undefined })
    expect(
      screen.queryByText("Aus Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Nicht in Dokeinheit ignorieren"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()
  })

  it("emits remove local ignore word event when 'Nicht in Dokeinheit ignorieren' button is clicked", async () => {
    const { emitted, user } = await renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "documentation_unit", word: "testword" }],
    })
    // other options should not be rendered
    expect(
      screen.getByText("Nicht in Dokeinheit ignorieren"),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Aus Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()

    await user.click(screen.getByText("Nicht in Dokeinheit ignorieren"))
    expect(emitted()["globally-ignored-word:remove"]).toBeUndefined()
  })

  it("renders 'Von jDV ignoriert' when word is globally ignored by jDV", async () => {
    await renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "global_jdv", word: "testword" }],
    })
    expect(screen.getByText("Von jDV ignoriert")).toBeInTheDocument()
    expect(
      screen.queryByText("Aus Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Nicht in Dokeinheit ignorieren"),
    ).not.toBeInTheDocument()
  })

  test("emits remove global ignore word event when 'Aus Wörterbuch entfernen' button is clicked", async () => {
    const { emitted, user } = await renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "global", word: "testword" }],
    })
    // other options should not be rendered
    expect(screen.getByText("Aus Wörterbuch entfernen")).toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()
    expect(screen.queryByText("Nicht Ignorieren")).not.toBeInTheDocument()

    await user.click(screen.getByText("Aus Wörterbuch entfernen"))
    expect(emitted()["ignored-word:remove"]).toBeUndefined()
  })
})
