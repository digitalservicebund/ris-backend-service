import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia, createPinia } from "pinia"
import DivergentExpirationGroup from "@/components/divergentGroup/divergentExpiration/DivergentExpirationGroup.vue"
import {
  MetadataSectionName,
  MetadatumType,
  UndefinedDate,
  MetadataSections,
  NormCategory,
} from "@/domain/norm"
import { getNormByGuid } from "@/services/norms"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import { generateNorm } from "~/test-helper/dataGenerators"

vi.mock("@/services/norms/operations")

type DivergentExpirationGroupProps = InstanceType<
  typeof DivergentExpirationGroup
>["$props"]

function renderComponent(props?: Partial<DivergentExpirationGroupProps>) {
  const effectiveProps: DivergentExpirationGroupProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
  }

  return render(DivergentExpirationGroup, { props: effectiveProps })
}

describe("DivergentExpirationGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("should render the component with 2 radio buttons each for different sections ", async () => {
    renderComponent()

    const definedRadio = screen.getByLabelText("bestimmt")
    expect(definedRadio).toBeInTheDocument()
    expect(definedRadio).toBeVisible()

    const undefinedRadio = screen.getByLabelText("unbestimmt")
    expect(undefinedRadio).toBeInTheDocument()
    expect(undefinedRadio).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    const user = userEvent.setup()

    let modelValue = {}
    const updateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const definedRadio = screen.getByLabelText("bestimmt")
    const undefinedRadio = screen.getByLabelText("unbestimmt")

    expect(definedRadio).toBeChecked()
    expect(undefinedRadio).not.toBeChecked()

    await user.click(undefinedRadio)
    await rerender({ modelValue })
    expect(undefinedRadio).toBeChecked()
    expect(definedRadio).not.toBeChecked()

    const dropDownInputField = screen.getByLabelText(
      "Unbestimmtes abweichendes Außerkrafttretedatum Dropdown",
    )

    expect(dropDownInputField).toBeInTheDocument()
    expect(dropDownInputField).toBeVisible()
  })

  it("restores the original data after switching types", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {
      DIVERGENT_EXPIRATION_DEFINED: [
        {
          DATE: ["2023-01-01"],
          NORM_CATEGORY: [NormCategory.AMENDMENT_NORM],
        },
      ],
    }

    const updateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender, emitted } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const undefinedRadio = screen.getByLabelText("unbestimmt")
    await user.click(undefinedRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[0]).toEqual([
      {
        DIVERGENT_EXPIRATION_UNDEFINED: [{}],
      },
    ])

    const definedRadio = screen.getByLabelText("bestimmt")
    await user.click(definedRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[1]).toEqual([
      {
        DIVERGENT_EXPIRATION_DEFINED: [
          {
            DATE: ["2023-01-01"],
            NORM_CATEGORY: [NormCategory.AMENDMENT_NORM],
          },
        ],
      },
    ])
  })

  it("initialises with the correct child section based on the modelvalue prop", () => {
    renderComponent({
      modelValue: {
        [MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED]: [
          {
            [MetadatumType.DATE]: ["2020-05-12"],
          },
        ],
      },
    })

    const definedRadio = screen.getByLabelText("bestimmt")
    expect(definedRadio).toBeChecked()

    const undefinedRadio = screen.getByLabelText(
      "Bestimmtes abweichendes Außerkrafttretedatum Date Input",
    )

    expect(undefinedRadio).toBeVisible()
    expect(undefinedRadio).toHaveValue("12.05.2020")
  })

  it("should by default render the  DivergentExpirationDefinedInputGroup if modelValue is empty", () => {
    renderComponent({ modelValue: {} })

    const definedRadio = screen.getByLabelText("bestimmt")

    expect(definedRadio).toBeChecked()

    const undefinedRadio = screen.getByLabelText(
      "Bestimmtes abweichendes Außerkrafttretedatum Date Input",
    )

    expect(undefinedRadio).toBeInTheDocument()
    expect(undefinedRadio).toBeVisible()
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

    const undefinedRadio = screen.getByLabelText("unbestimmt")

    expect(undefinedRadio).toBeDisabled()
  })
})
