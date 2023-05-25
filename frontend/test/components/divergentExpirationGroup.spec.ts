import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { setActivePinia, createPinia } from "pinia"
import DivergentExpirationGroup from "@/components/DivergentExpirationGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
  UndefinedDate,
} from "@/domain/Norm"
import { getNormByGuid } from "@/services/norms"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import { generateNorm } from "~/test-helper/dataGenerators"

vi.mock("@/services/norms/operations")
function renderComponent(options?: { modelValue?: MetadataSections }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(DivergentExpirationGroup, { props })
}

describe("DivergentExpirationGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    flushPromises()
  })

  it("should render the component with 2 radio buttons each for different sections ", async () => {
    renderComponent()

    const divergentExpirationDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    const divergentExpirationUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    expect(divergentExpirationDefinedSelection).toBeInTheDocument()
    expect(divergentExpirationDefinedSelection).toBeVisible()

    expect(divergentExpirationUndefinedSelection).toBeInTheDocument()
    expect(divergentExpirationUndefinedSelection).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    renderComponent()

    const divergentExpirationDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    const divergentExpirationUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    expect(divergentExpirationDefinedSelection).toBeChecked()
    expect(divergentExpirationUndefinedSelection).not.toBeChecked()

    await fireEvent.click(divergentExpirationUndefinedSelection)
    expect(divergentExpirationUndefinedSelection).toBeChecked()
    expect(divergentExpirationDefinedSelection).not.toBeChecked()

    const dropDownInputField = screen.getByLabelText(
      "Unbestimmtes abweichendes Außerkrafttretedatum Dropdown"
    ) as HTMLInputElement

    expect(dropDownInputField).toBeInTheDocument()
    expect(dropDownInputField).toBeVisible()
  })

  it("clears the child section data when a different radio button is selected ", async () => {
    renderComponent()

    const divergentExpirationUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    const Date = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(Date).toBeInTheDocument()
    expect(Date).toBeVisible()
    expect(Date?.type).toBe("date")

    await userEvent.type(Date, "2020-05-12")
    await userEvent.tab()

    expect(Date).toHaveValue("2020-05-12")

    await fireEvent.click(divergentExpirationUndefinedSelection)

    const dropDownInputField = screen.getByLabelText(
      "Unbestimmtes abweichendes Außerkrafttretedatum Dropdown"
    ) as HTMLInputElement

    await userEvent.click(dropDownInputField)
    await userEvent.click(screen.getByText("unbestimmt (unbekannt)"))
    expect(dropDownInputField).toHaveValue("unbestimmt (unbekannt)")

    const divergentExpirationDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement

    await fireEvent.click(divergentExpirationDefinedSelection)

    const dateNew = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(dateNew).not.toHaveValue()
  })

  it("initialises with the correct child section based on the modelvalue prop", function () {
    renderComponent({
      modelValue: {
        [MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED]: [
          {
            [MetadatumType.DATE]: ["2020-05-12"],
          },
        ],
      },
    })

    const divergentExpirationDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement
    expect(divergentExpirationDefinedSelection).toBeChecked()

    const divergentExpirationDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(divergentExpirationDefinedDate).toBeVisible()
    expect(divergentExpirationDefinedDate).toHaveValue("2020-05-12")
  })

  it("should by default render the  DivergentExpirationDefinedInputGroup if modelValue is empty", function () {
    renderComponent({ modelValue: {} })

    const divergentExpirationDefinedSelection = screen.queryByLabelText(
      "bestimmt"
    ) as HTMLInputElement

    expect(divergentExpirationDefinedSelection).toBeChecked()

    const divergentExpirationDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
    ) as HTMLInputElement

    expect(divergentExpirationDefinedDate).toBeInTheDocument()
    expect(divergentExpirationDefinedDate).toBeVisible()
  })

  it("should disable the DivergentExpirationUndefined Radio Button if there is an UNDEFINED_DATE entry already ", async () => {
    renderComponent()

    const norm = generateNorm({
      metadataSections: {
        [MetadataSectionName.DIVERGENT_EXPIRATION]: [
          {
            [MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED]: [
              {
                [MetadatumType.UNDEFINED_DATE]: [
                  UndefinedDate.UNDEFINED_UNKNOWN,
                ],
              },
            ],
          },
        ],
      },
    })

    const response = { status: 200, data: norm }
    vi.mocked(getNormByGuid).mockResolvedValue(response)
    const store = useLoadedNormStore()
    await store.load("guid")

    expect(getNormByGuid).toHaveBeenCalledOnce()
    expect(getNormByGuid).toHaveBeenLastCalledWith("guid")
    expect(store.loadedNorm).toEqual(norm)

    const divergentExpirationUndefinedSelection = screen.queryByLabelText(
      "unbestimmt"
    ) as HTMLInputElement

    await expect(divergentExpirationUndefinedSelection).toBeDisabled()
  })
})
