import { fireEvent, render, RenderResult, waitFor } from "@testing-library/vue"
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
    const { getByRole } = renderComponent({
      slot: "<span>Select File</span>",
    })

    const button = getByRole("link")

    expect(button.innerHTML).toContain("<span>Select File</span>")
  })

  it("includes a hidden file input element", () => {
    const { getByLabelText } = renderComponent({ slot: "Select File" })

    const input: HTMLInputElement = getByLabelText("Select File", {
      selector: "input",
    })

    expect(input.type).toBe("file")
    expect(input.hidden).toBe(true)
  })

  it("clicking the button triggers the file input", () => {
    const { getByRole, getByLabelText } = renderComponent({
      slot: "Select File",
    })
    const button = getByRole("link")
    const input = getByLabelText("Select File")
    input.onclick = vi.fn()

    button.click()

    expect(input.onclick).toHaveBeenCalledOnce()
  })

  it("emits input event when user uploads files", async () => {
    const { emitted, getByLabelText } = renderComponent({ slot: "Select File" })
    const input: HTMLInputElement = getByLabelText("Select File", {
      selector: "input",
    })
    const file = new File(["test"], "text.txt")
    Object.defineProperty(input, "files", { value: [file] })

    await waitFor(() => fireEvent.update(input))

    expect(emitted()["input"]).toHaveLength(1)
    expect(emitted()["input"]).toEqual([[expect.any(Event)]])
  })
})
