import { fireEvent, render, RenderResult, waitFor } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileInputButton from "@/components/FileInputButton.vue"

function renderComponent(options?: { slot?: string }): RenderResult {
  const vuetify = createVuetify({ components, directives })
  const global = { plugins: [vuetify] }
  const slots = { default: options?.slot ?? "" }
  const props = {
    id: "identifier",
    ariaLabel: "aria-label",
  }

  return render(FileInputButton, { global, slots, props })
}

describe("FileInputButton", () => {
  it("shows button with default slot as content", () => {
    const { getByRole } = renderComponent({
      slot: "<span>Select File</span>",
    })

    const button = getByRole("button")

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
    const button = getByRole("button")
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
