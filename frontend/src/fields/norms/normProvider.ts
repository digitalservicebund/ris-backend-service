import LabelPosition from "@/shared/components/input/InputField.vue"
import { InputField, InputType } from "@/shared/components/input/types"

export const normProvider: InputField[] = [
  {
    name: "providerEntity",
    id: "providerEntity",
    type: InputType.TEXT,
    label: "Staat, Land, Stadt, Landkreis oder juristische Person",
    inputAttributes: {
      ariaLabel: "Staat, Land, Stadt, Landkreis oder juristische Person",
    },
  },
  {
    name: "providerDecidingBody",
    id: "providerDecidingBody",
    type: InputType.TEXT,
    label: "Beschließendes Organ",
    inputAttributes: {
      ariaLabel: "Beschließendes Organ",
    },
  },
  {
    name: "providerIsResolutionMajority",
    id: "providerIsResolutionMajority",
    type: InputType.CHECKBOX,
    label: "Beschlussfassung mit qualifizierter Mehrheit",
    inputAttributes: {
      ariaLabel: "Beschlussfassung mit qualifizierter Mehrheit",
      labelPosition: LabelPosition.RIGHT,
    },
  },
]
