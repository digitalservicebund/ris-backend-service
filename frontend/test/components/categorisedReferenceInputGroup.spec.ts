import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import CategorizedReferenceInputGroup from "@/components/CategorizedReferenceInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(CategorizedReferenceInputGroup, { props })
}

function getControls() {
  const categorisedReferenceText = screen.queryByRole("textbox", {
    name: "Aktivverweisung",
  }) as HTMLInputElement

  return {
    categorisedReferenceText,
  }
}

describe("DocumentTypeInputGroup", () => {
  it("should render the components with the initial state of the modelvalue", () => {
    renderComponent({
      modelValue: {
        TEXT: ["test value"],
      },
    })

    const { categorisedReferenceText } = getControls()

    expect(categorisedReferenceText).toBeInTheDocument()
    expect(categorisedReferenceText).toHaveValue("test value")
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { categorisedReferenceText } = getControls()

    await user.type(categorisedReferenceText, "foo")

    expect(modelValue).toEqual({
      TEXT: ["foo"],
    })
  })

  it("should change the modelvalue when clearing the input", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      TEXT: ["foo"],
    }
    renderComponent({ modelValue })

    const { categorisedReferenceText } = getControls()

    expect(categorisedReferenceText).toHaveValue("foo")
    await user.clear(categorisedReferenceText)
    expect(modelValue.TYPE_NAME).toBeUndefined()
  })
})
