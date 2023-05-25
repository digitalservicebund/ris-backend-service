import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import DivergentEntryIntoForceGroup from "@/components/DivergentEntryIntoForceGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
} from "@/domain/Norm"

function renderComponent(options?: { modelValue?: MetadataSections }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(DivergentEntryIntoForceGroup, { props })
}

describe("DivergentEntryIntoForceGroup", () => {
  it("should render the component with 2 radio buttons each for different sections ", () => {
    renderComponent()
    const divergentEntryIntoForceDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    const divergentEntryIntoForceUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedSelection).toBeInTheDocument()
    expect(divergentEntryIntoForceDefinedSelection).toBeVisible()

    expect(divergentEntryIntoForceUndefinedSelection).toBeInTheDocument()
    expect(divergentEntryIntoForceUndefinedSelection).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    renderComponent()

    const divergentEntryIntoForceDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    const divergentEntryIntoForceUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedSelection).toBeChecked()
    expect(divergentEntryIntoForceUndefinedSelection).not.toBeChecked()

    await fireEvent.click(divergentEntryIntoForceUndefinedSelection)
    expect(divergentEntryIntoForceUndefinedSelection).toBeChecked()
    expect(divergentEntryIntoForceDefinedSelection).not.toBeChecked()

    const dropDownInputField = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown"
    ) as HTMLInputElement

    expect(dropDownInputField).toBeInTheDocument()
    expect(dropDownInputField).toBeVisible()
  })

  it("clears the child section data when a different radio button is selected ", async () => {
    renderComponent()

    const divergentEntryIntoForceDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
    ) as HTMLInputElement

    const divergentEntryIntoForceUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedDate).toBeInTheDocument()
    expect(divergentEntryIntoForceDefinedDate).toBeVisible()
    expect(divergentEntryIntoForceDefinedDate?.type).toBe("date")

    await userEvent.type(divergentEntryIntoForceDefinedDate, "2020-05-12")
    await userEvent.tab()

    expect(divergentEntryIntoForceDefinedDate).toHaveValue("2020-05-12")

    await fireEvent.click(divergentEntryIntoForceUndefinedSelection)

    const dropDownInputFieldNew = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown"
    ) as HTMLInputElement

    await userEvent.click(dropDownInputFieldNew)
    await userEvent.click(screen.getByText("unbestimmt (unbekannt)"))
    expect(dropDownInputFieldNew).toHaveValue("unbestimmt (unbekannt)")

    const divergentEntryIntoForceDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement

    await fireEvent.click(divergentEntryIntoForceDefinedSelection)

    const divergentEntryIntoForceDefinedDateNew = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedDateNew).not.toHaveValue()
  })

  it("initialises with the correct child section based on the modelvalue prop", function () {
    renderComponent({
      modelValue: {
        [MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED]: [
          {
            [MetadatumType.DATE]: ["2020-05-12"],
          },
        ],
      },
    })

    const divergentEntryIntoForceDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    expect(divergentEntryIntoForceDefinedSelection).toBeChecked()

    const divergentEntryIntoForceDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
    ) as HTMLInputElement
    expect(divergentEntryIntoForceDefinedDate).toBeVisible()
    expect(divergentEntryIntoForceDefinedDate).toHaveValue("2020-05-12")
  })

  it("should by default render the  DivergentEntryIntoForceInputGroup if modelValue is empty", function () {
    renderComponent({ modelValue: {} })

    const divergentEntryIntoForceDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedSelection).toBeChecked()

    const divergentEntryIntoForceDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(divergentEntryIntoForceDefinedDate).toBeInTheDocument()
    expect(divergentEntryIntoForceDefinedDate).toBeVisible()
  })
})
