import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { computed, defineComponent, markRaw } from "vue"
import ModelComponentRepeater from "@/components/ModelComponentRepeater.vue"

const TestModelComponent = defineComponent({
  props: {
    modelValue: {
      type: String,
      required: true,
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const value = computed({
      get: () => props.modelValue,
      set: (newValue) => emit("update:modelValue", newValue),
    })

    return { value }
  },
  template: "<input v-model='value' />",
})

function renderComponent(options?: {
  modelValue?: unknown[]
  defaultValue?: unknown
  addButtonSlot?: string
  removeButtonSlot?: string
}) {
  const props = {
    component: markRaw(TestModelComponent),
    modelValue: options?.modelValue ?? [],
    defaultValue: options?.defaultValue ?? null,
  }
  const slots = {
    addButton: options?.addButtonSlot ?? "",
    removeButton: options?.removeButtonSlot ?? "",
  }
  const renderResult = render(ModelComponentRepeater, { props, slots })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

describe("ModelComponentRepeater", () => {
  it("renders a component instance per model entry", () => {
    const modelValue = ["", ""]
    const { queryAllByRole } = renderComponent({ modelValue })

    const textboxes = queryAllByRole("textbox")

    expect(textboxes).toHaveLength(2)
  })

  it("injects model value entries into component instances", () => {
    const modelValue = ["foo", "bar"]
    const { queryByDisplayValue } = renderComponent({ modelValue })

    const fooInstance = queryByDisplayValue("foo")
    const barInstance = queryByDisplayValue("bar")

    expect(fooInstance).toBeInTheDocument()
    expect(barInstance).toBeInTheDocument()
  })

  it("renders components in the same order as the model value entries", () => {
    const modelValue = ["foo", "bar"]
    const { queryAllByRole } = renderComponent({ modelValue })

    const instances = queryAllByRole("textbox")

    expect(instances).toHaveLength(2)
    expect(instances[0]).toHaveValue("foo")
    expect(instances[1]).toHaveValue("bar")
  })

  it("emits update model value event when any component changes its value", async () => {
    const modelValue = ["a", "c"]
    const { emitted, getByDisplayValue, user } = renderComponent({ modelValue })
    const instance = getByDisplayValue("a")

    await user.type(instance, "b")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[["ab", "c"]]])
  })

  it("shows a single add button", () => {
    const { queryAllByRole } = renderComponent()

    const addButton = queryAllByRole("button", { name: "Eintrag Hinzufügen" })

    expect(addButton.length).toBe(1) // Small "hack" to test `single`.
  })

  it("adds another component instance when add button gets clicked", async () => {
    const modelValue = ["", ""]
    const { queryAllByRole, getByRole, user } = renderComponent({ modelValue })
    const addButton = getByRole("button", { name: "Eintrag Hinzufügen" })

    expect(queryAllByRole("textbox")).toHaveLength(2)

    await user.click(addButton)

    expect(queryAllByRole("textbox")).toHaveLength(3)
  })

  it("adds another model entry with default value when add button gets clicked", async () => {
    const { emitted, getByRole, user } = renderComponent({
      modelValue: ["old"],
      defaultValue: "new",
    })
    const addButton = getByRole("button", { name: "Eintrag Hinzufügen" })

    await user.click(addButton)

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[["old", "new"]]])
  })

  it("renders optional add button slot", async () => {
    const addButtonSlot =
      "<button aria-label='Add Button'>Custom Button</button>"
    const { queryByLabelText } = renderComponent({ addButtonSlot })

    const addButton = queryByLabelText("Add Button")

    expect(addButton).toBeInTheDocument()
    expect(addButton).toHaveTextContent("Custom Button")
  })

  it("shows a remove button for each entry except last", () => {
    const modelValue = ["", "", ""]
    const { queryAllByRole } = renderComponent({ modelValue })

    const removeButtons = queryAllByRole("button", {
      name: "Eintrag Entfernen",
    })

    expect(removeButtons).toHaveLength(2)
  })

  it("removes the related instance if a remove button gets clicked", async () => {
    const modelValue = ["", "", ""]
    const { getAllByRole, user } = renderComponent({ modelValue })
    const removeButtons = getAllByRole("button", { name: "Eintrag Entfernen" })

    expect(getAllByRole("textbox")).toHaveLength(3)

    await user.click(removeButtons[0])

    expect(getAllByRole("textbox")).toHaveLength(2)
  })

  it("emits update model value event when any instance gets removed", async () => {
    const modelValue = ["a", "b", "c"]
    const { emitted, getAllByRole, user } = renderComponent({ modelValue })
    const removeButtons = getAllByRole("button", { name: "Eintrag Entfernen" })

    await user.click(removeButtons[1])

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[["a", "c"]]])
  })

  it("renders optional remove button slot", async () => {
    const { queryByLabelText } = renderComponent({
      modelValue: ["", ""],
      removeButtonSlot:
        "<button aria-label='Remove Button'>Custom Button</button>",
    })

    const removeButton = queryByLabelText("Remove Button")

    expect(removeButton).toBeInTheDocument()
    expect(removeButton).toHaveTextContent("Custom Button")
  })
})
