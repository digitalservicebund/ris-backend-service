import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DocumentStatusGroup from "@/components/documentStatus/DocumentStatusGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
} from "@/domain/norm"
import { getNormByGuid } from "@/services/norms"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import { generateNorm } from "~/test-helper/dataGenerators"

vi.mock("@/services/norms/operations")

type DocumentStatusGroupProps = InstanceType<
  typeof DocumentStatusGroup
>["$props"]

function renderComponent(props?: Partial<DocumentStatusGroupProps>) {
  const effectiveProps: DocumentStatusGroupProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
  }

  return render(DocumentStatusGroup, { props: effectiveProps })
}

describe("DocumentStatusGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("should render the component with 3 radio buttons each for different sections ", async () => {
    renderComponent()

    const documentStatusSelection = screen.queryByLabelText(
      "Stand der dokumentarischen Bearbeitung",
    )
    expect(documentStatusSelection).toBeInTheDocument()
    expect(documentStatusSelection).toBeVisible()

    const documentTextProofSelection = screen.queryByLabelText("Textnachweis")
    expect(documentTextProofSelection).toBeInTheDocument()
    expect(documentTextProofSelection).toBeVisible()

    const documentOtherSelection = screen.queryByLabelText("Sonstiger Hinweis")
    expect(documentOtherSelection).toBeInTheDocument()
    expect(documentOtherSelection).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {}
    const updateModelValue = vi
      .fn()
      .mockImplementation((data: MetadataSections) => {
        modelValue = data
      })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const documentTextProofSelection = screen.getByLabelText("Textnachweis")
    const documentOtherSelection = screen.getByLabelText("Sonstiger Hinweis")
    const documentStatusSelection = screen.getByLabelText(
      "Stand der dokumentarischen Bearbeitung",
    )

    expect(documentStatusSelection).toBeChecked()
    expect(documentTextProofSelection).not.toBeChecked()
    expect(documentOtherSelection).not.toBeChecked()

    const workNoteTextInput = screen.getByRole("textbox", {
      name: "Bearbeitungshinweis",
    })
    expect(workNoteTextInput).toBeInTheDocument()
    expect(workNoteTextInput).toBeVisible()

    await user.click(documentTextProofSelection)
    await rerender({ modelValue })
    expect(documentTextProofSelection).toBeChecked()
    expect(documentStatusSelection).not.toBeChecked()
    expect(documentOtherSelection).not.toBeChecked()

    const proofTextInput = screen.getByLabelText("Textnachweis Text")
    expect(proofTextInput).toBeInTheDocument()
    expect(proofTextInput).toBeVisible()

    await user.click(documentOtherSelection)
    await rerender({ modelValue })
    expect(documentOtherSelection).toBeChecked()
    expect(documentStatusSelection).not.toBeChecked()
    expect(documentTextProofSelection).not.toBeChecked()

    const otherTextInput = screen.getByLabelText("Sonstiger Hinweis Text")
    expect(otherTextInput).toBeInTheDocument()
    expect(otherTextInput).toBeVisible()
  })

  it("restores the original data after switching types", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {
      DOCUMENT_STATUS: [
        {
          WORK_NOTE: ["foo"],
          DESCRIPTION: ["bar"],
          DATE: [],
          REFERENCE: ["baz"],
        },
      ],
    }

    const updateModelValue = vi
      .fn()
      .mockImplementation((data: MetadataSections) => {
        modelValue = data
      })

    const { rerender, emitted } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const documentOtherSelection = screen.getByLabelText("Sonstiger Hinweis")
    await user.click(documentOtherSelection)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[0]).toEqual([{ DOCUMENT_OTHER: [{}] }])

    const documentStatusSelection = screen.getByLabelText(
      "Stand der dokumentarischen Bearbeitung",
    )
    await user.click(documentStatusSelection)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[1]).toEqual([
      {
        DOCUMENT_STATUS: [
          {
            WORK_NOTE: ["foo"],
            DESCRIPTION: ["bar"],
            DATE: [],
            REFERENCE: ["baz"],
          },
        ],
      },
    ])
  })

  it("should by default render the document status", function () {
    renderComponent({ modelValue: {} })

    const description = screen.getByLabelText(
      "Bezeichnung der Änderungsvorschrift",
    )

    expect(description).toBeInTheDocument()
    expect(description).toBeVisible()
  })

  it("should disable the text proof if a text proof already exists ", async () => {
    renderComponent()

    const norm = generateNorm({
      metadataSections: {
        [MetadataSectionName.DOCUMENT_STATUS_SECTION]: [
          {
            [MetadataSectionName.DOCUMENT_TEXT_PROOF]: [
              {
                [MetadatumType.TEXT]: ["Textnachweis ab 16.07.2008"],
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

    const documentTextProofSelection = screen.queryByLabelText("Textnachweis")

    expect(documentTextProofSelection).toBeDisabled()
  })
})
