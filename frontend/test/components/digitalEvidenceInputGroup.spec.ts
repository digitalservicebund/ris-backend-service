import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DigitalEvidenceInputGroup from "@/components/DigitalEvidenceInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DigitalEvidenceInputGroup, { props })
}

function getControls() {
  const linkInput = screen.queryByRole("textbox", {
    name: "Verlinkung",
  }) as HTMLInputElement

  const relatedDataInput = screen.queryByRole("textbox", {
    name: "Zugehörige Daten",
  }) as HTMLInputElement

  const noteInput = screen.queryByRole("textbox", {
    name: "Hinweis auf fremde Verlinkung oder Daten",
  }) as HTMLInputElement

  const appendixInput = screen.queryByRole("textbox", {
    name: "Zusatz zum Nachweis",
  }) as HTMLInputElement

  return {
    linkInput,
    relatedDataInput,
    noteInput,
    appendixInput,
  }
}

describe("DigitalEvidenceInputGroup", () => {
  it("should render the components with the initial state of the modelvalue", () => {
    renderComponent({
      modelValue: {
        LINK: ["link"],
        RELATED_DATA: ["related data"],
        EXTERNAL_DATA_NOTE: ["external data note"],
        APPENDIX: ["appendix"],
      },
    })

    const { linkInput, relatedDataInput, noteInput, appendixInput } =
      getControls()

    expect(linkInput).toBeInTheDocument()
    expect(linkInput).toHaveValue("link")

    expect(relatedDataInput).toBeInTheDocument()
    expect(relatedDataInput).toHaveValue("related data")

    expect(noteInput).toBeInTheDocument()
    expect(noteInput).toHaveValue("external data note")

    expect(appendixInput).toBeInTheDocument()
    expect(appendixInput).toHaveValue("appendix")
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { linkInput, relatedDataInput, noteInput, appendixInput } =
      getControls()

    await user.type(linkInput, "foo")
    await user.type(relatedDataInput, "bar")
    await user.type(noteInput, "baz")
    await user.type(appendixInput, "qux")

    expect(modelValue).toEqual({
      LINK: ["foo"],
      RELATED_DATA: ["bar"],
      EXTERNAL_DATA_NOTE: ["baz"],
      APPENDIX: ["qux"],
    })
  })

  it("should change the modelvalue when clearing the input", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      LINK: ["foo"],
      RELATED_DATA: ["bar"],
      EXTERNAL_DATA_NOTE: ["baz"],
      APPENDIX: ["qux"],
    }
    renderComponent({ modelValue })

    const { linkInput, relatedDataInput, noteInput, appendixInput } =
      getControls()

    expect(linkInput).toHaveValue("foo")
    await user.clear(linkInput)
    expect(modelValue.LINK).toBeUndefined()

    expect(relatedDataInput).toHaveValue("bar")
    await user.clear(relatedDataInput)
    expect(modelValue.RELATED_DATA).toBeUndefined()

    expect(noteInput).toHaveValue("baz")
    await user.clear(noteInput)
    expect(modelValue.EXTERNAL_DATA_NOTE).toBeUndefined()

    expect(appendixInput).toHaveValue("qux")
    await user.clear(appendixInput)
    expect(modelValue.APPENDIX).toBeUndefined()
  })
})
