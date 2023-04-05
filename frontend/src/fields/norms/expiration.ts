import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"
import LabelPosition from "@/shared/components/input/InputField.vue"
import { InputField, InputType } from "@/shared/components/input/types"

export const expiration: InputField[] = [
  {
    name: "expirationDate",
    id: "expirationDate",
    type: InputType.DATE,
    label: "Datum des Außerkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Außerkrafttretens",
      isFutureDate: true,
    },
  },
  dropdown(
    "expirationDateState",
    "expirationDateState",
    "Unbestimmtes Datum des Außerkrafttretens",
    undefinedDropDownItems
  ),
  {
    name: "isExpirationDateTemp",
    id: "isExpirationDateTemp",
    type: InputType.CHECKBOX,
    label: "Befristet",
    inputAttributes: {
      ariaLabel: "Befristet",
      labelPosition: LabelPosition.RIGHT,
    },
  },
  {
    name: "principleExpirationDate",
    id: "principleExpirationDate",
    type: InputType.DATE,
    label: "Grundsätzliches Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Grundsätzliches Außerkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "principleExpirationDateState",
    "principleExpirationDateState",
    "Unbestimmtes grundsätzliches Außerkrafttretdatum",
    undefinedDropDownItems
  ),
  {
    name: "divergentExpirationDate",
    id: "divergentExpirationDate",
    type: InputType.DATE,
    label: "Bestimmtes abweichendes Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Bestimmtes abweichendes Außerkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "divergentExpirationDateState",
    "divergentExpirationDateState",
    "Unbestimmtes abweichendes Außerkrafttretdatum",
    undefinedDropDownItems
  ),
  {
    name: "expirationNormCategory",
    id: "expirationNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
]
