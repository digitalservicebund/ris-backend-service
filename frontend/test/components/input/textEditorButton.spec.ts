import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { markRaw } from "vue"
import TextEditorButton from "@/components/TextEditorButton.vue"
import IconTest from "~icons/ic/baseline-clear"

describe("text editor button", async () => {
  test("renders with child components", async () => {
    render(TextEditorButton, {
      props: {
        type: "menu",
        icon: markRaw(IconTest),
        ariaLabel: "test editor button",
        childButtons: [
          {
            type: "test child 1 type",
            icon: markRaw(IconTest),
            ariaLabel: "test child button 1",
          },
          {
            type: "test child 2 type",
            icon: markRaw(IconTest),
            ariaLabel: "test child button 2",
          },
        ],
      },
    })
    const button = screen.getByLabelText("test editor button")
    expect(button).toBeInTheDocument()
    await userEvent.click(button)
    expect(screen.getByLabelText("test child button 1")).toBeInTheDocument()
    expect(screen.getByLabelText("test child button 2")).toBeInTheDocument()
  })

  test("emits event to parent when user clicks on button without child buttons", async () => {
    const { emitted } = render(TextEditorButton, {
      props: {
        type: "test type",
        icon: IconTest,
        ariaLabel: "test editor button",
      },
    })
    const button = screen.getByLabelText("test editor button")
    expect(button).toBeInTheDocument()
    await userEvent.click(button)
    expect(emitted().toggle).toBeTruthy()
  })

  test("toggles dropdown menu when user clicks on button with child buttons", async () => {
    const user = userEvent.setup()

    render(TextEditorButton, {
      props: {
        type: "menu",
        icon: IconTest,
        ariaLabel: "menu",
        childButtons: [
          {
            type: "test child 1 type",
            icon: IconTest,
            ariaLabel: "test child 1 aria",
          },
          {
            type: "test child 2 type",
            icon: IconTest,
            ariaLabel: "test child 2 aria",
          },
        ],
      },
    })
    const button = screen.getByLabelText("menu")
    expect(button).toBeInTheDocument()
    await user.click(button)

    expect(screen.getByLabelText("test child 1 aria")).toBeInTheDocument()
    expect(screen.getByLabelText("test child 2 aria")).toBeInTheDocument()
  })
})
