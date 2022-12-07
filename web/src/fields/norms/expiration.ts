import { InputField, InputType } from "@/domain"

export const expiration: InputField[] = [
  {
    name: "expirationDate",
    type: InputType.DATE,
    label: "Datum des Außerkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Außerkrafttretens",
    },
  },
  {
    name: "expirationDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Datum des Außerkrafttretens",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Datum des Außerkrafttretens",
    },
  },
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
    },
  },
  {
    name: "principleExpirationDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Grundsätzliches Außerkrafttretdatum",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Grundsätzliches Außerkrafttretdatum",
    },
  },
  {
    name: "divergentExpirationDate",
    type: InputType.DATE,
    label: "Abweichendes Außerkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Abweichendes Außerkrafttretedatum",
    },
  },
  {
    name: "divergentExpirationDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Abweichendes Außerkrafttretdatum",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Abweichendes Außerkrafttretdatum",
    },
  },
  {
    name: "expirationNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
]
