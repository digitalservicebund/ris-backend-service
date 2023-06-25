import userEvent from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { createPinia, setActivePinia } from "pinia"
import DocumentStatusGroup from "@/components/DocumentStatusGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
  ProofType,
} from "@/domain/Norm"
import { getNormByGuid } from "@/services/norms"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import { generateNorm } from "~/test-helper/dataGenerators"

vi.mock("@/services/norms/operations")
function renderComponent(options?: { modelValue?: MetadataSections }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(DocumentStatusGroup, { props })
}

describe("DocumentStatusGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    flushPromises()
  })

  it("should render the component with 3 radio buttons each for different sections ", async () => {
    renderComponent()

    const documentStatusSelection = screen.queryByLabelText(
      "Stand der dokumentarischen Bearbeitung"
    ) as HTMLInputElement
    const documentTextProofSelection = screen.queryByLabelText(
      "Textnachweis"
    ) as HTMLInputElement
    const documentOtherSelection = screen.queryByLabelText(
      "Sonstiger Hinweis"
    ) as HTMLInputElement

    expect(documentStatusSelection).toBeInTheDocument()
    expect(documentStatusSelection).toBeVisible()

    expect(documentTextProofSelection).toBeInTheDocument()
    expect(documentTextProofSelection).toBeVisible()

    expect(documentOtherSelection).toBeInTheDocument()
    expect(documentOtherSelection).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    renderComponent()

    const documentStatusSelection = screen.queryByLabelText(
      "Stand der dokumentarischen Bearbeitung"
    ) as HTMLInputElement
    const documentTextProofSelection = screen.queryByLabelText(
      "Textnachweis"
    ) as HTMLInputElement
    const documentOtherSelection = screen.queryByLabelText(
      "Sonstiger Hinweis"
    ) as HTMLInputElement

    expect(documentStatusSelection).toBeChecked()
    expect(documentTextProofSelection).not.toBeChecked()
    expect(documentOtherSelection).not.toBeChecked()

    await fireEvent.click(documentTextProofSelection)
    expect(documentTextProofSelection).toBeChecked()
    expect(documentStatusSelection).not.toBeChecked()
    expect(documentOtherSelection).not.toBeChecked()

    const dropDownInputField = screen.getByLabelText(
      "Textnachweis Dropdown"
    ) as HTMLInputElement

    expect(dropDownInputField).toBeInTheDocument()
    expect(dropDownInputField).toBeVisible()

    await fireEvent.click(documentOtherSelection)
    expect(documentOtherSelection).toBeChecked()
    expect(documentStatusSelection).not.toBeChecked()
    expect(documentTextProofSelection).not.toBeChecked()

    const newDropDownInputField = screen.getByLabelText(
      "Sonstiger Hinweis Dropdown"
    ) as HTMLInputElement

    expect(newDropDownInputField).toBeInTheDocument()
    expect(newDropDownInputField).toBeVisible()
  })

  it("clears the child section data when a different radio button is selected ", async () => {
    renderComponent()

    const documentOtherSelection = screen.queryByLabelText(
      "Sonstiger Hinweis"
    ) as HTMLInputElement

    const textInput = screen.getByLabelText(
      "Bezeichnung der Änderungsvorschrift Description"
    ) as HTMLInputElement

    expect(textInput).toBeInTheDocument()
    expect(textInput).toBeVisible()

    await userEvent.type(textInput, "test text")
    await userEvent.tab()

    expect(textInput).toHaveValue("test text")

    await fireEvent.click(documentOtherSelection)

    const dropDownInputField = screen.getByLabelText(
      "Sonstiger Hinweis Dropdown"
    ) as HTMLInputElement

    await userEvent.click(dropDownInputField)
    await userEvent.click(screen.getByText("Text in Bearbeitung"))
    expect(dropDownInputField).toHaveValue("Text in Bearbeitung")

    const documentStatusSelection = screen.queryByLabelText(
      "Stand der dokumentarischen Bearbeitung"
    ) as HTMLInputElement

    await fireEvent.click(documentStatusSelection)

    const textInputNew = screen.getByLabelText(
      "Bezeichnung der Änderungsvorschrift Description"
    ) as HTMLInputElement

    expect(textInputNew).not.toHaveValue()
  })

  it("should by default render the  DocumentStatus Input Group if modelValue is empty", function () {
    renderComponent({ modelValue: {} })

    const description = screen.getByLabelText(
      "Bezeichnung der Änderungsvorschrift Description"
    ) as HTMLInputElement

    expect(description).toBeInTheDocument()
    expect(description).toBeVisible()
  })

  it("should disable the DivergentExpirationUndefined Radio Button if there is an UNDEFINED_DATE entry already ", async () => {
    renderComponent()

    const norm = generateNorm({
      metadataSections: {
        [MetadataSectionName.DOCUMENT_STATUS_SECTION]: [
          {
            [MetadataSectionName.DOCUMENT_TEXT_PROOF]: [
              {
                [MetadatumType.PROOF_TYPE]: [
                  ProofType.TEXT_PROOF_VALIDITY_FROM,
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

    const documentTextProofSelection = screen.queryByLabelText(
      "Textnachweis"
    ) as HTMLInputElement

    await expect(documentTextProofSelection).toBeDisabled()
  })
})
