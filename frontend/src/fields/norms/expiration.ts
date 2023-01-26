import { LabelPosition } from "@/components/InputField.vue"
import { InputField, InputType } from "@/domain"
import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"

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
  {
    name: "divergentExpirationDate",
    type: InputType.DATE,
    label: "Bestimmtes abweichendes Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Bestimmtes abweichendes Außerkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "divergentExpirationDateState",
    "Unbestimmtes abweichendes Außerkrafttretdatum",
    undefinedDropDownItems
  ),
  {
    name: "expirationNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
]
