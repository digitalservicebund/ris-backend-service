/* eslint-disable vue/one-component-per-file */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { Component, computed, defineComponent, markRaw } from "vue"
import EditableList from "@/shared/components/EditableList.vue"

const SimpleTextEditComponent = defineComponent({
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

const JsonStringifySummary = defineComponent({
  props: {
    data: {
      default: undefined,
      validator: () => true,
    },
  },
  template: "<span>{{ JSON.stringify(data) }}</span>",
})

function renderComponent(options?: {
  editComponent?: Component
  summaryComponent?: Component
  modelValue?: unknown[]
  defaultValue?: unknown
}) {
  const props = {
    editComponent: markRaw(options?.editComponent ?? SimpleTextEditComponent),
    summaryComponent: markRaw(
      options?.summaryComponent ?? JsonStringifySummary
    ),
    modelValue: options?.modelValue,
    defaultValue: options?.defaultValue ?? "",
  }

  render(EditableList, { props })
}

async function clickEditButtonOfEntry(
  entryIndex: number,
  user?: ReturnType<typeof userEvent.setup>
): Promise<void> {
  user = user ?? userEvent.setup()
  const allEditButtons = screen.getAllByRole("button", {
    name: "Eintrag bearbeiten",
  })

  await user.click(allEditButtons[entryIndex])
}

async function clickDeleteButtonOfEntry(
  entryIndex: number,
  user?: ReturnType<typeof userEvent.setup>
): Promise<void> {
  user = user ?? userEvent.setup()
  const allDeleteButtons = screen.getAllByRole("button", {
    name: "Eintrag löschen",
  })

  await user.click(allDeleteButtons[entryIndex])
}

describe("EditableList", () => {
  it("renders a summary per model entry on initial render", () => {
    renderComponent({
      summaryComponent: JsonStringifySummary,
      modelValue: ["entry 1", "entry 2"],
    })

    expect(screen.queryByText('"entry 1"')).toBeVisible()
    expect(screen.queryByText('"entry 2"')).toBeVisible()
  })

  it("shows edit component for default value when adding new new entry via button click", async () => {
    const user = userEvent.setup()
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue: ["entry 1"],
      defaultValue: "default entry",
    })

    expect(screen.queryByRole("textbox")).not.toBeInTheDocument()

    const addButton = screen.getByRole("button", { name: "Weitere Angabe" })
    await user.click(addButton)
    const input = screen.queryByRole("textbox") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("default entry")
  })

  it("shows edit component for correct entry when edit button is clicked", async () => {
    renderComponent({
      editComponent: SimpleTextEditComponent,
      summaryComponent: JsonStringifySummary,
      modelValue: ["entry 1", "entry 2", "entry 3"],
    })

    await clickEditButtonOfEntry(1)

    expect(screen.queryByText('"entry 1"')).toBeVisible()
    expect(screen.queryByText('"entry 2"')).not.toBeInTheDocument()
    expect(screen.queryByText('"entry 3"')).toBeVisible()

    const input = screen.queryByRole("textbox") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("entry 2")
  })

  it("deletes correct entry when delete button is clicked", async () => {
    const modelValue = ["entry 1", "entry 2"]
    renderComponent({
      summaryComponent: JsonStringifySummary,
      modelValue,
    })

    await clickDeleteButtonOfEntry(1)

    expect(screen.queryByText('"entry 1"')).toBeVisible()
    expect(screen.queryByText('"entry 2"')).not.toBeInTheDocument()
    expect(modelValue).toEqual(["entry 1"])
  })

  it("correctly maintains edit state if predecessor entry gets deleted", async () => {
    renderComponent({
      editComponent: SimpleTextEditComponent,
      summaryComponent: JsonStringifySummary,
      modelValue: ["entry 1", "entry 2", "entry 3"],
    })

    await clickEditButtonOfEntry(1)
    await clickDeleteButtonOfEntry(0)

    const input = screen.queryByRole("textbox") as HTMLInputElement
    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("entry 2")
  })

  it("correctly maintains edit state if successor entry gets deleted", async () => {
    renderComponent({
      editComponent: SimpleTextEditComponent,
      summaryComponent: JsonStringifySummary,
      modelValue: ["entry 1", "entry 2", "entry 3"],
    })

    await clickEditButtonOfEntry(1)
    await clickDeleteButtonOfEntry(2)

    const input = screen.queryByRole("textbox") as HTMLInputElement
    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("entry 2")
  })

  it("automatically adds a default entry in edit mode if list is empty on initial render", () => {
    const modelValue: string[] = []
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue,
      defaultValue: "default entry",
    })

    const input = screen.queryByRole("textbox") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("default entry")
    expect(modelValue).toEqual(["default entry"])
  })

  it("automatically adds a default entry in edit mode if list is undefined", () => {
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue: undefined,
      defaultValue: "default entry",
    })

    const input = screen.queryByRole("textbox") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("default entry")
  })

  it("automatically adds a default entry in edit mode if user deletes all entries", async () => {
    const modelValue: string[] = ["entry 1"]
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue,
      defaultValue: "default entry",
    })

    await clickDeleteButtonOfEntry(0)

    const input = screen.queryByRole("textbox") as HTMLInputElement
    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("default entry")
    expect(modelValue).toEqual(["default entry"])
  })

  it("automatically focuses the first input element of the edit component", async () => {
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue: ["entry 1"],
    })

    await clickEditButtonOfEntry(0)

    expect(screen.getByRole("textbox")).toHaveFocus()
  })

  it("updates the model value entry on editing it", async () => {
    const user = userEvent.setup()
    const modelValue = ["fo", "bar"]
    renderComponent({
      editComponent: SimpleTextEditComponent,
      modelValue,
    })

    await clickEditButtonOfEntry(0, user)
    const input = screen.getByRole("textbox") as HTMLInputElement
    await user.type(input, "o")

    expect(modelValue).toEqual(["foo", "bar"])
  })

  it("closes the editing component if user hits the enter key inside it", async () => {
    const user = userEvent.setup()
    renderComponent({
      editComponent: SimpleTextEditComponent,
      summaryComponent: JsonStringifySummary,
      modelValue: ["entry 1"],
    })

    await clickEditButtonOfEntry(0, user)
    const input = screen.getByRole("textbox") as HTMLInputElement
    await user.type(input, "{enter}")

    expect(input).not.toBeInTheDocument()
    expect(screen.queryByText('"entry 1"')).toBeVisible()
  })
})
