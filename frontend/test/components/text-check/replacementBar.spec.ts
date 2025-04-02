import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"

function renderComponent(options?: {
  replacementMode?: "single" | "multiple" | undefined
  replacements?: string[]
}) {
  const user = userEvent.setup()

  return {
    user,
    ...render(ReplacementBar, {
      props: {
        replacementMode: options?.replacementMode ?? "single",
        replacements: options?.replacements ?? [],
      },
    }),
  }
}

describe("Match replacement bar single and multiple test", () => {
  test.each([
    {
      mode: "single" as const,
      expectedLabel: "Ignorieren",
      expectedAriaLabel: "Vorschlag ignorieren",
    },
    {
      mode: "multiple" as const,
      expectedLabel: "Alle ignorieren",
      expectedAriaLabel: "Vorschläge ignorieren",
    },
  ])(
    "$mode ignore button renders $expectedLabel",
    ({
      mode,
      expectedLabel,
      expectedAriaLabel,
    }: {
      mode: "single" | "multiple"
      expectedLabel: string
      expectedAriaLabel: string
    }) => {
      renderComponent({ replacementMode: mode })
      expect(screen.getByTestId("ignored-word-add-button")).toHaveTextContent(
        expectedLabel,
      )
      expect(screen.getByLabelText(expectedAriaLabel)).toBeInTheDocument()
    },
  )

  it("accept replacement emits update", async () => {
    const { emitted, user } = renderComponent({
      replacements: ["Rechtschreibfehler"],
    })

    const acceptButton = screen.getByLabelText("Rechtschreibfehler übernehmen")
    await user.click(acceptButton)
    expect(emitted()["suggestion:update"]).toEqual([["Rechtschreibfehler"]])
  })

  it("accept replacement emits ignore", async () => {
    const { emitted, user } = renderComponent()
    const ignoreButton = screen.getByTestId("ignored-word-add-button")
    await user.click(ignoreButton)
    expect(emitted()["ignored-word:add"]).toHaveLength(1)
  })
})
