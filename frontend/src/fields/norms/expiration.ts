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
    "Unbestimmtes Grundsätzliches Außerkrafttretdatum",
    undefinedDropDownItems
  ),
  {
    name: "divergentExpirationDate",
    type: InputType.DATE,
    label: "Abweichendes Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Abweichendes Außerkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "divergentExpirationDateState",
    "Unbestimmtes Abweichendes Außerkrafttretdatum",
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
