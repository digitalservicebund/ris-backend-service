import { InputField, InputType } from "@/domain"

export const documentType: InputField[] = [
  {
    name: "documentTypeName",
    type: InputType.TEXT,
    label: "Typbezeichnung",
    inputAttributes: {
      ariaLabel: "Typbezeichnung",
    },
  },
  {
    name: "documentNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
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
