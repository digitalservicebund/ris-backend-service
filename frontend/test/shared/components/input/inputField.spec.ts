import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { nextTick } from "vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import { MANDATORY_FIELD_MISSING } from "@/shared/i18n/errors.json"
import { useGlobalValidationErrorStore } from "@/stores/globalValidationErrorStore"

type InputFieldProps = InstanceType<typeof InputField>["$props"]

function renderComponent(
  props?: Partial<InputFieldProps>,
  slots?: Record<string, unknown>,
) {
  const id = props?.id ?? "identifier"

  const effectiveSlots = { default: `<input id="${id}" />`, ...slots }

  const effectiveProps: InputFieldProps = {
    id,
    label: props?.label ?? "",
    ...props,
  }

  return render(InputField, { props: effectiveProps, slots: effectiveSlots })
}

describe("InputField", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("renders the component with a label", () => {
    renderComponent({ label: "test label" })
    const element = screen.getByLabelText("test label")
    expect(element).toBeInTheDocument()
  })

  it("does not render an empty label", () => {
    renderComponent({})
    expect(screen.queryByTestId("label-wrapper")).not.toBeInTheDocument()
  })

  it("visually hides the label", () => {
    renderComponent({ label: "test", visuallyHideLabel: true })
    const element = screen.getByTestId("label-wrapper")
    expect(element).toBeInTheDocument()
    expect(element).toHaveClass("sr-only")
  })

  it("renders a label with multiple lines", () => {
    renderComponent({ label: ["test label 1", "test label 2"] })
    const element = screen.getByLabelText("test label 1 test label 2")
    expect(element).toBeInTheDocument()
  })

  it("renders a label with multiple lines and marks as required", () => {
    renderComponent({
      label: ["test label 1", "test label 2"],
      required: true,
    })

    const element = screen.getByLabelText("test label 1 test label 2 *")
    expect(element).toBeInTheDocument()
  })

  it("renders the label at the top by default", () => {
    renderComponent({ label: "test" })
    const element = screen.getByTestId("label-wrapper")
    expect(element).not.toHaveClass("order", { exact: false })
  })

  it("sets the correct order when rendering the label at the top", () => {
    renderComponent({ label: "test" })
    const element = screen.getByTestId("label-wrapper")
    expect(element).not.toHaveClass("order", { exact: false })
  })

  it("sets the correct order when rendering the label to the right", () => {
    renderComponent({ label: "test", labelPosition: LabelPosition.RIGHT })
    const element = screen.getByTestId("label-wrapper")
    expect(element).toHaveClass("order-1")
  })

  it("marks the component as requied", () => {
    renderComponent({ label: "test label", required: true })
    const element = screen.getByLabelText("test label *")
    expect(element).toBeInTheDocument()
  })

  it("renders the error message from a prop", () => {
    renderComponent({
      validationError: { message: "error message", instance: "identifier" },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("renders the error message from the store", () => {
    const { add } = useGlobalValidationErrorStore()
    add({ message: "error message", instance: "identifier" })
    renderComponent({ id: "identifier" })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("initially shows the error from the props if both are set", () => {
    const { add } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()
  })

  it("replaces the error message from the props if an error is set in the store", async () => {
    renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()

    const { add } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    await nextTick()

    expect(screen.queryByText("from props")).not.toBeInTheDocument()
    expect(screen.getByText("from store")).toBeInTheDocument()
  })

  it("replaces the error message from the store if a prop is set", async () => {
    const { add } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    const { rerender } = renderComponent({ id: "identifier" })

    expect(screen.getByText("from store")).toBeInTheDocument()

    await rerender({
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.queryByText("from store")).not.toBeInTheDocument()
    expect(screen.getByText("from props")).toBeInTheDocument()
  })

  it("falls back to the error message from the store if the prop is removed", async () => {
    const { add } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    const { rerender } = renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()

    await rerender({ validationError: undefined })

    expect(screen.queryByText("from props")).not.toBeInTheDocument()
    expect(screen.getByText("from store")).toBeInTheDocument()
  })

  it("falls back to the error message from the props if the store is reset", async () => {
    renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()

    const { reset, add } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    await nextTick()

    expect(screen.queryByText("from props")).not.toBeInTheDocument()
    expect(screen.getByText("from store")).toBeInTheDocument()

    reset()
    await nextTick()
    expect(screen.getByText("from props")).toBeInTheDocument()
    expect(screen.queryByText("from store")).not.toBeInTheDocument()
  })

  it("removes the error message when the prop is cleared", async () => {
    const { rerender } = renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()

    await rerender({ validationError: undefined })
    expect(screen.queryByText("from props")).not.toBeInTheDocument()
  })

  it("removes the error message when the store is reset", async () => {
    const { add, reset } = useGlobalValidationErrorStore()
    add({ message: "from store", instance: "identifier" })
    renderComponent({ id: "identifier" })

    expect(screen.getByText("from store")).toBeInTheDocument()

    reset()
    await nextTick()
    expect(screen.queryByText("from store")).not.toBeInTheDocument()
  })

  it("shows the error's own message if no code exists", () => {
    renderComponent({
      validationError: { message: "error message", instance: "identifier" },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("shows the error's own message if the code is not found", () => {
    renderComponent({
      validationError: {
        code: "NOT_FOUND",
        instance: "identifier",
        message: "error message",
      },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("maps the error code to a message if possible", () => {
    renderComponent({
      validationError: {
        code: "MANDATORY_FIELD_MISSING",
        instance: "identifier",
        message: "This should not be visible",
      },
    })

    expect(screen.getByText(MANDATORY_FIELD_MISSING.title)).toBeInTheDocument()
  })

  it("renders the slot content", () => {
    renderComponent(
      { id: "test-identifier", label: "test label" },
      {
        default: `
          <template v-slot="slotProps">
            <input type='radio' :id="slotProps.id" />
          </template>
        `,
      },
    )

    const element = screen.getByRole<HTMLInputElement>("radio")
    expect(element).toBeInTheDocument()
  })

  it("forwards the ID to the slot", () => {
    renderComponent(
      { id: "test-identifier", label: "test label" },
      {
        default: `
          <template v-slot="slotProps">
            ID is {{ slotProps.id }}
          </template>
        `,
      },
    )

    const element = screen.getByText("ID is test-identifier")
    expect(element).toBeInTheDocument()
  })

  it("tells the slot content that there is an error", () => {
    renderComponent(
      { validationError: { instance: "foo", message: "bar" } },
      {
        default: `
          <template v-slot="slotProps">
            Has error is {{ slotProps.hasError }}
          </template>
        `,
      },
    )

    const element = screen.getByText("Has error is true")
    expect(element).toBeInTheDocument()
  })

  it("doesn't tell the slot content that there is an error if there is none", () => {
    renderComponent(undefined, {
      default: `
          <template v-slot="slotProps">
            Has error is {{ slotProps.hasError }}
          </template>
        `,
    })

    const element = screen.getByText("Has error is false")
    expect(element).toBeInTheDocument()
  })

  it("sets the validation error based on the slot events", async () => {
    const user = userEvent.setup()
    renderComponent(undefined, {
      default: `
          <template v-slot="slotProps">
            <button @click="slotProps.updateValidationError({ instance: 'foo', message: 'bar' })">
              Create error
            </button>
          </template>
        `,
    })

    await user.click(screen.getByRole("button"))
    expect(screen.getByText("bar")).toBeInTheDocument()
  })

  it("removes the validation error based on the slot events", async () => {
    const user = userEvent.setup()
    renderComponent(
      { validationError: { instance: "foo", message: "bar" } },
      {
        default: `
          <template v-slot="slotProps">
            <button @click="slotProps.updateValidationError(undefined)">
              Remove error
            </button>
          </template>
        `,
      },
    )

    const element = screen.getByText("bar")
    expect(element).toBeInTheDocument()
    await user.click(screen.getByRole("button"))
    expect(element).not.toBeInTheDocument()
  })
})
