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
        replacements: options?.replacements ?? [],
      },
    }),
  }
}

describe("Match replacement bar single and multiple test", () => {
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
