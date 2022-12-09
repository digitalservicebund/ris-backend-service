import { fireEvent, render, RenderResult, screen } from "@testing-library/vue"
import FileInputButton from "@/components/FileInput.vue"

function renderComponent(options?: { slot?: string }): RenderResult {
  const slots = { default: options?.slot ?? "" }
  const props = {
    id: "identifier",
    ariaLabel: "aria-label",
  }

  return render(FileInputButton, { slots, props })
}

describe("FileInputButton", () => {
  it("shows button with default slot as content", () => {
    renderComponent({
      slot: "<span>Select File</span>",
    })

    const button = screen.getByRole("link")

    expect(button.innerHTML).toContain("<span>Select File</span>")
  })

  it("includes a hidden file input element", () => {
    renderComponent({ slot: "Select File" })

    const input: HTMLInputElement = screen.getByLabelText("Select File", {
      selector: "input",
    })

    expect(input.type).toBe("file")
    expect(input.hidden).toBe(true)
  })

  it("emits input event when user uploads files", async () => {
    const { emitted } = renderComponent({ slot: "Select File" })
    const input: HTMLInputElement = screen.getByLabelText("Select File", {
      selector: "input",
    })
    const file = new File(["test"], "text.txt")
    Object.defineProperty(input, "files", { value: [file] })

    await fireEvent.update(input)

    expect(emitted()["input"]).toHaveLength(1)
    expect(emitted()["input"]).toEqual([[expect.any(Event)]])
  })
})
