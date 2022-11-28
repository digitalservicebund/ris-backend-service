import { InputField, InputType } from "./types"

export const normDocumentTypeFields: InputField[] = [
  {
    name: "documentTypeName",
    type: InputType.TEXT,
    label: "Typbezeichnung",
    required: true,
    inputAttributes: {
      ariaLabel: "Typbezeichnung",
    },
  },
  {
    name: "documentNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    required: true,
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
  {
    name: "documentTemplateName",
    type: InputType.TEXT,
    label: "Bezeichnung gemäß Vorlage",
    inputAttributes: {
      ariaLabel: "Bezeichnung gemäß Vorlage",
    },
  },
]
