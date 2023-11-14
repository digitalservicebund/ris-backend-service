import { userEvent } from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { MetadatumType } from "@/domain/norm"

type DateOrYearInputGroupProps = InstanceType<
  typeof DateOrYearInputGroup
>["$props"]

function renderComponent(props?: Partial<DateOrYearInputGroupProps>) {
  const effectiveProps: DateOrYearInputGroupProps = {
    dateValue: props?.dateValue ?? undefined,
    yearValue: props?.yearValue ?? undefined,
    "onUpdate:selectedInputType": props?.["onUpdate:selectedInputType"],
    "onUpdate:dateValue": props?.["onUpdate:dateValue"],
    "onUpdate:yearValue": props?.["onUpdate:yearValue"],
    idPrefix: props?.idPrefix ?? "test-id-prefix",
    label: props?.label ?? "test-label",
    selectedInputType: props?.selectedInputType ?? MetadatumType.DATE,
  }
  return render(DateOrYearInputGroup, { props: effectiveProps })
}

async function changeToYearInput() {
  const yearRadioButton = screen.getByLabelText("Jahresangabe")
  expect(yearRadioButton).toBeInTheDocument()
  expect(yearRadioButton).toBeVisible()
  expect(yearRadioButton).not.toBeChecked()

  await fireEvent.click(yearRadioButton)

  expect(yearRadioButton).toBeChecked()
}

async function changeToDateInput() {
  const dateRadioButton = screen.getByLabelText("Datum")
  expect(dateRadioButton).toBeInTheDocument()
  expect(dateRadioButton).toBeVisible()
  expect(dateRadioButton).not.toBeChecked()

  await fireEvent.click(dateRadioButton)

  expect(dateRadioButton).toBeChecked()
}

describe("date/year field", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  describe("default date component", () => {
    it("shows 2 radio buttons with 1 selected by default and corresponding field displayed", () => {
      renderComponent({ label: "test-label" })
      const dateRadioButton = screen.getByLabelText("Datum")
      const yearRadioButton = screen.getByLabelText("Jahresangabe")
      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")

      expect(dateRadioButton).toBeChecked()
      expect(yearRadioButton).not.toBeChecked()
      expect(dateInputField).toBeVisible()
    })

    it("allows user to type in the date field", async () => {
      const user = userEvent.setup()
      let dateValue: string | undefined = undefined
      const onUpdateDateValue = vi.fn().mockImplementation((value) => {
        dateValue = value
      })

      const { rerender } = renderComponent({
        dateValue,
        "onUpdate:dateValue": onUpdateDateValue,
      })

      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")
      expect(dateInputField).toBeVisible()
      await user.type(dateInputField, "12.05.2020")
      await rerender({ dateValue })
      expect(dateInputField).toHaveValue("12.05.2020")
      expect(dateValue).toBe("2020-05-12")
    })

    it("user can delete the date input", async () => {
      const user = userEvent.setup()
      let dateValue: string | undefined = "2020-05-12"
      const onUpdateDateValue = vi.fn().mockImplementation((value) => {
        dateValue = value
      })

      const { rerender } = renderComponent({
        dateValue,
        "onUpdate:dateValue": onUpdateDateValue,
      })

      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")

      expect(dateInputField).toHaveValue("12.05.2020")
      await user.clear(dateInputField)
      await rerender({ dateValue })
      expect(dateInputField).toHaveValue("")
      expect(dateValue).toBeUndefined()
    })
  })

  describe("year input component", () => {
    it("switches to year input when year radio is selected", async () => {
      let selectedInputType = MetadatumType.DATE
      const onUpdateSelectedInputType = vi.fn().mockImplementation((value) => {
        selectedInputType = value
      })

      const { rerender } = renderComponent({
        selectedInputType,
        "onUpdate:selectedInputType": onUpdateSelectedInputType,
      })

      await changeToYearInput()
      expect(onUpdateSelectedInputType).toHaveBeenCalledWith(MetadatumType.YEAR)
      await rerender({ selectedInputType })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      expect(yearInputField).toBeVisible()
    })

    it("restricts year input to digits only", async () => {
      let yearValue = ""
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.type(yearInputField, "abcd")
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("")
    })

    it("restricts year input to four digits", async () => {
      let yearValue = ""
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.type(yearInputField, "12345")
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("1234")
    })

    it("allows user to clear the year field", async () => {
      let yearValue = "2003"
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.clear(yearInputField)
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("")
      expect(yearValue).toBeUndefined()
    })
  })

  describe("switching between date and year components", () => {
    it("restores original data when switching between types", async () => {
      let selectedInputType = MetadatumType.DATE
      let dateValue: string | undefined = "2020-05-12"

      const onUpdateSelectedInputType = vi.fn().mockImplementation((value) => {
        selectedInputType = value
      })

      const onUpdateDateValue = vi.fn().mockImplementation((value) => {
        dateValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType,
        "onUpdate:selectedInputType": onUpdateSelectedInputType,
        dateValue,
        "onUpdate:dateValue": onUpdateDateValue,
      })

      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")
      expect(dateInputField).toHaveValue("12.05.2020")

      await changeToYearInput()
      expect(onUpdateSelectedInputType).toHaveBeenCalledWith(MetadatumType.YEAR)
      await rerender({ selectedInputType })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await changeToDateInput()
      expect(onUpdateSelectedInputType).toHaveBeenCalledWith(MetadatumType.YEAR)
      await rerender({ selectedInputType })
      const dateInputFieldNew = screen.getByPlaceholderText("TT.MM.JJJJ")
      expect(dateInputFieldNew).toHaveValue("12.05.2020")
    })

    it("doesn't revert to date when year is cleared", async () => {
      let yearValue = "2023"
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.clear(yearInputField)
      await rerender({ yearValue })

      expect(yearInputField).toBeVisible()
    })
  })
})
