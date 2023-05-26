import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"
import { LabelPosition } from "@/shared/components/input/InputField.vue"
import { InputField, InputType } from "@/shared/components/input/types"

export const expiration: InputField[] = [
  {
    name: "expirationDate",
    type: InputType.DATE,
    label: "Datum des Außerkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Außerkrafttretens",
      isFutureDate: true,
    },
  },
  dropdown(
    "expirationDateState",
    "Unbestimmtes Datum des Außerkrafttretens",
    undefinedDropDownItems
  ),
  {
    name: "isExpirationDateTemp",
    type: InputType.CHECKBOX,
    label: "Befristet",
    inputAttributes: {
      ariaLabel: "Befristet",
      labelPosition: LabelPosition.RIGHT,
    },
  },
  {
    name: "principleExpirationDate",
    type: InputType.DATE,
    label: "Grundsätzliches Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Grundsätzliches Außerkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "principleExpirationDateState",
    "Unbestimmtes grundsätzliches Außerkrafttretdatum",
    undefinedDropDownItems
  ),
]
