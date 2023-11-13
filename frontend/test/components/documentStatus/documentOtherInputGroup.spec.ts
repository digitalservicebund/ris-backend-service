import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DocumentOtherInputGroup from "@/components/documentStatus/DocumentOtherInputGroup.vue"
import { Metadata } from "@/domain/norm"

type DocumentOtherInputGroupProps = InstanceType<
  typeof DocumentOtherInputGroup
>["$props"]

function renderComponent(props: Partial<DocumentOtherInputGroupProps>) {
  const defaultProps: DocumentOtherInputGroupProps = {
    modelValue: {},
    ...props,
  }

  return render(DocumentOtherInputGroup, { props: defaultProps })
}

describe("DocumentOtherInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  test("should render", () => {
    renderComponent({})
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      modelValue: {
        TEXT: ["foo"],
      },
    })

    const textInput = screen.getByRole("textbox", {
      name: "Sonstiger Hinweis Text",
    })

    expect(textInput).toHaveValue("foo")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      TEXT: ["foo"],
    }
    const updateModelValue = vi.fn().mockImplementation((value: Metadata) => {
      modelValue = value
    })

    renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const input = screen.getByRole("textbox", {
      name: "Sonstiger Hinweis Text",
    })
    expect(input).toHaveValue("foo")
    await user.type(input, "bar")
    expect(modelValue.TEXT).toEqual(["foobar"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      TEXT: ["foo"],
    }
    const updateModelValue = vi.fn().mockImplementation((value: Metadata) => {
      modelValue = value
    })

    renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const input = screen.getByRole("textbox", {
      name: "Sonstiger Hinweis Text",
    })
    expect(input).toHaveValue("foo")
    await user.clear(input)
    expect(modelValue.TEXT).toBeUndefined()
  })
})
