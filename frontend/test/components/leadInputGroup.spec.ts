import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"

function renderComponent(options?: { modelValue?: unknown }) {
  const props = {
    modelValue: options?.modelValue,
  }
  const utils = render(LeadInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

async function getInputFields() {
  const input1 = screen.getByLabelText("Ressort") as HTMLInputElement
  const input2 = screen.getByLabelText(
    "Organisationseinheit"
  ) as HTMLInputElement

  return { input1, input2 }
}

describe("LeadInputGroup", () => {
  it("renders an InputGroup with the given input fields", async () => {
    renderComponent({ modelValue: { leadJurisdiction: "", leadUnit: "" } })

    const { input1, input2 } = await getInputFields()

    expect(input1).toBeInTheDocument()
    expect(input2).toBeInTheDocument()
  })

  it("renders input field with given data ", async () => {
    renderComponent({
      modelValue: { leadJurisdiction: "foo", leadUnit: "baz" },
    })

    const { input1, input2 } = await getInputFields()

    expect(input1.value).toBe("foo")
    expect(input2.value).toBe("baz")
  })

  it("should emit update:modelValue when input values with the new input values", async () => {
    const { emitted, user } = renderComponent({
      modelValue: { leadJurisdiction: "foo", leadUnit: "baz" },
    })

    const { input1, input2 } = await getInputFields()

    expect(input1.value).toBe("foo")
    expect(input2.value).toBe("baz")

    await user.clear(input2)

    await user.type(input1, "bar")
    await user.type(input2, "bar")

    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toHaveLength(3)
    expect(emitted()["update:modelValue"]).toEqual([
      [{ leadJurisdiction: "foobar", leadUnit: "bar" }],
      [{ leadJurisdiction: "foobar", leadUnit: "bar" }],
      [{ leadJurisdiction: "foobar", leadUnit: "bar" }],
    ])
  })
})
