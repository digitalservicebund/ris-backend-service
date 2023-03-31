import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"

function renderComponent(options?: { modelValue?: unknown }) {
  const props = {
    modelValue: options?.modelValue,
  }
  const utils = render(SubjectAreaInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

function getInputFields() {
  const input1 = screen.getByLabelText("FNA-Nummer") as HTMLInputElement
  const input2 = screen.getByLabelText("Frühere FNA-Nummer") as HTMLInputElement
  const input3 = screen.getByLabelText("GESTA-Nummer") as HTMLInputElement
  const input4 = screen.getByLabelText(
    "Bundesgesetzblatt Teil III"
  ) as HTMLInputElement

  return { input1, input2, input3, input4 }
}

describe("SubjectAreaInputGroup", () => {
  it("renders an InputGroup with the given input fields", async () => {
    renderComponent({
      modelValue: {
        subjectFna: "",
        subjectPreviousFna: "",
        subjectGesta: "",
        subjectBgb3: "",
      },
    })

    const { input1, input2, input3, input4 } = getInputFields()

    expect(input1).toBeInTheDocument()
    expect(input2).toBeInTheDocument()
    expect(input3).toBeInTheDocument()
    expect(input4).toBeInTheDocument()
  })

  it("renders input field with given data ", async () => {
    renderComponent({
      modelValue: {
        subjectFna: "foo",
        subjectPreviousFna: "bar",
        subjectGesta: "baz",
        subjectBgb3: "bun",
      },
    })

    const { input1, input2, input3, input4 } = getInputFields()

    expect(input1.value).toBe("foo")
    expect(input2.value).toBe("bar")
    expect(input3.value).toBe("baz")
    expect(input4.value).toBe("bun")
  })
})
