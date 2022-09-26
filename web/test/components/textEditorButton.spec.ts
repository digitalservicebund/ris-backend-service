import userEvent from "@testing-library/user-event"
import { render, fireEvent } from "@testing-library/vue"
import { mount } from "@vue/test-utils"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import TextEditorButton from "../../src/components/TextEditorButton.vue"

describe("text editor button", async () => {
  const vuetify = createVuetify({ components, directives })

  it("renders with mandatory props", () => {
    const wrapper = mount(TextEditorButton, {
      global: {
        plugins: [vuetify],
      },
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
      global: {
        plugins: [vuetify],
      },
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
    const { emitted, getByLabelText } = render(TextEditorButton, {
      props: {
        type: "test type",
        icon: "test icon",
        ariaLabel: "test aria",
      },
    })
    const button = getByLabelText("test aria")
    expect(button).toBeInTheDocument()
    await fireEvent.click(button)
    expect(emitted().toggle).toBeTruthy()
  })

  test("toggles dropdown menu when user clicks on button with child buttons", async () => {
    const user = userEvent.setup()

    const { getByLabelText, getByText } = render(TextEditorButton, {
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
    const button = getByText("test icon")
    expect(button).toBeInTheDocument()
    await user.click(button)

    expect(getByLabelText("test child 1 aria")).toBeInTheDocument()
    expect(getByLabelText("test child 2 aria")).toBeInTheDocument()
  })

  test("if more button clicked, don't show childButtons but emit event", async () => {
    const { emitted, getByLabelText } = render(TextEditorButton, {
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
    const button = getByLabelText("test aria")
    expect(button).toBeInTheDocument()
    await fireEvent.click(button)
    expect(emitted().toggle).toBeTruthy()
  })
})
