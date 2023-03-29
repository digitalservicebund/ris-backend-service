import userEvent from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import { mount } from "@vue/test-utils"
import TextEditorButton from "../../src/shared/components/input/TextEditorButton.vue"

describe("text editor button", async () => {
  it("renders with mandatory props", () => {
    const wrapper = mount(TextEditorButton, {
      props: {
        type: "test type",
        icon: "test icon",
        ariaLabel: "test aria",
      },
    })

    expect(wrapper.props().type).toBe("test type")
    expect(wrapper.props().icon).toBe("test icon")
    expect(wrapper.props().ariaLabel).toBe("test aria")
  })

  test("renders with child components", async () => {
    const wrapper = mount(TextEditorButton, {
      props: {
        type: "test type",
        icon: "test icon",
        ariaLabel: "test aria",
        childButtons: [
          {
            type: "test child 1 type",
            icon: "test child 1 icon",
            ariaLabel: "test child 1 aria",
          },
          {
            type: "test child 2 type",
            icon: "test child 2 icon",
            ariaLabel: "test child 2 aria",
          },
        ],
      },
    })

    expect(wrapper.props().type).toBe("test type")
    expect(wrapper.props().icon).toBe("test icon")
    expect(wrapper.props().ariaLabel).toBe("test aria")
    expect(wrapper.props().childButtons).toStrictEqual([
      {
        type: "test child 1 type",
        icon: "test child 1 icon",
        ariaLabel: "test child 1 aria",
      },
      {
        type: "test child 2 type",
        icon: "test child 2 icon",
        ariaLabel: "test child 2 aria",
      },
    ])
  })

  test("emits event to parent when user clicks on button without child buttons", async () => {
    const { emitted } = render(TextEditorButton, {
      props: {
        type: "test type",
        icon: "test icon",
        ariaLabel: "test aria",
      },
    })
    const button = screen.getByLabelText("test aria")
    expect(button).toBeInTheDocument()
    await fireEvent.click(button)
    expect(emitted().toggle).toBeTruthy()
  })

  test("toggles dropdown menu when user clicks on button with child buttons", async () => {
    const user = userEvent.setup()

    render(TextEditorButton, {
      props: {
        type: "menu",
        icon: "test icon",
        ariaLabel: "menu",
        childButtons: [
          {
            type: "test child 1 type",
            icon: "test child 1 icon",
            ariaLabel: "test child 1 aria",
          },
          {
            type: "test child 2 type",
            icon: "test child 2 icon",
            ariaLabel: "test child 2 aria",
          },
        ],
      },
    })
    const button = screen.getByText("test icon")
    expect(button).toBeInTheDocument()
    await user.click(button)

    expect(screen.getByLabelText("test child 1 aria")).toBeInTheDocument()
    expect(screen.getByLabelText("test child 2 aria")).toBeInTheDocument()
  })

  test("if more button clicked, don't show childButtons but emit event", async () => {
    const { emitted } = render(TextEditorButton, {
      props: {
        type: "more",
        icon: "test icon",
        ariaLabel: "test aria",
        childButtons: [
          {
            type: "test child 1 type",
            icon: "test child 1 icon",
            ariaLabel: "test child 1 aria",
          },
          {
            type: "test child 2 type",
            icon: "test child 2 icon",
            ariaLabel: "test child 2 aria",
          },
        ],
      },
    })
    const button = screen.getByLabelText("test aria")
    expect(button).toBeInTheDocument()
    await fireEvent.click(button)
    expect(emitted().toggle).toBeTruthy()
  })
})
