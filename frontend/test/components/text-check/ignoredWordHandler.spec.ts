import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { Match } from "@/types/textCheck"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

async function renderComponent(match: Match, ignoredLocally = false) {
  const user = userEvent.setup()

  await flushPromises()
  return {
    user,
    ...render(IgnoredWordHandler, {
      props: {
        match: match,
        ignoredLocally: ignoredLocally,
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

  it("emits add local ignore word event when 'In Dokumentationseinheit ignorieren' button is clicked", async () => {
    const { emitted, user } = await renderComponent(baseMatch)

    expect(
      screen.getByText("In Dokumentationseinheit ignorieren"),
    ).toBeInTheDocument()
    await user.click(screen.getByText("In Dokumentationseinheit ignorieren"))
    expect(emitted()["ignored-word:add"]).toBeTruthy()
  })

  it("does not render any button when ignoredTextCheckWords is undefined", async () => {
    await renderComponent({ ...baseMatch, ignoredTextCheckWords: undefined })
    expect(
      screen.queryByText("Aus Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Nicht in Dokumentationseinheit ignorieren"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()
  })

  it("emits remove local ignore word event when 'Nicht in Dokumentationseinheit ignorieren' button is clicked", async () => {
    const { emitted, user } = await renderComponent({
      ...baseMatch,
      ignoredTextCheckWords: [{ type: "documentation_unit", word: "testword" }],
    })
    // other options should not be rendered
    expect(
      screen.getByText("Nicht in Dokumentationseinheit ignorieren"),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Aus Wörterbuch entfernen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Von jDV ignoriert")).not.toBeInTheDocument()

    await user.click(
      screen.getByText("Nicht in Dokumentationseinheit ignorieren"),
    )
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
      screen.queryByText("Nicht in Dokumentationseinheit ignorieren"),
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

  test("when ignoring locally check the emitted event and word location information", async () => {
    const { emitted, user } = await renderComponent(baseMatch)
    const expected = [[baseMatch.offset]]

    await user.click(screen.getByText("Hier ignorieren"))
    expect(emitted()["ignore-once:toggle"]).toEqual(expected)
  })

  test("when ignoredLocally is true, 'In Dokumentationseinheit ignorieren' emits ignore-once:toggle then ignored-word:add", async () => {
    const { emitted, user } = await renderComponent(baseMatch, true)

    await user.click(screen.getByText("In Dokumentationseinheit ignorieren"))

    expect(emitted()["ignore-once:toggle"]).toEqual([[baseMatch.offset]])
    expect(emitted()["ignored-word:add"]).toEqual([[]])
  })

  test("when ignored locally then unignore option is present", async () => {
    await renderComponent(baseMatch, true)

    expect(screen.getByText("Hier nicht ignorieren")).toBeInTheDocument()
    expect(
      screen.getByText("In Dokumentationseinheit ignorieren"),
    ).toBeInTheDocument()
    expect(screen.getByText("Zum Wörterbuch hinzufügen")).toBeInTheDocument()
  })

  test("when ignoredLocally is true, ignoring in document removes local ignore before ignoring in document", async () => {
    const { emitted, user } = await renderComponent(baseMatch, true)

    await user.click(screen.getByText("In Dokumentationseinheit ignorieren"))

    expect(emitted()["ignore-once:toggle"]).toEqual([[baseMatch.offset]])
    expect(emitted()["ignored-word:add"]).toBeTruthy()
  })

  test("when ignoredLocally is true, ignoring on global level removes local ignore before ignoring globally", async () => {
    const { emitted, user } = await renderComponent(baseMatch, true)

    await user.click(screen.getByText("Zum Wörterbuch hinzufügen"))

    expect(emitted()["ignore-once:toggle"]).toEqual([[baseMatch.offset]])
    expect(emitted()["globally-ignored-word:add"]).toBeTruthy()
  })

  test("when ignored in document, ignoring globally removes document ignore before ignoring globally", async () => {
    const { emitted, user } = await renderComponent(
      {
        ...baseMatch,
        ignoredTextCheckWords: [
          { type: "documentation_unit", word: "testword" },
        ],
      },
      false,
    )

    expect(
      screen.getByText("Nicht in Dokumentationseinheit ignorieren"),
    ).toBeInTheDocument()
    expect(screen.getByText("Zum Wörterbuch hinzufügen")).toBeInTheDocument()

    await user.click(screen.getByText("Zum Wörterbuch hinzufügen"))

    expect(emitted()["ignored-word:remove"]).toBeTruthy()
    expect(emitted()["globally-ignored-word:add"]).toBeTruthy()
  })
})
