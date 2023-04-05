import { InputField, InputType } from "@/shared/components/input/types"

export const documentType: InputField[] = [
  {
    name: "documentTypeName",
    id: "documentTypeName",
    type: InputType.TEXT,
    label: "Typbezeichnung",
    inputAttributes: {
      ariaLabel: "Typbezeichnung",
    },
  },
  {
    name: "documentNormCategory",
    id: "documentNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
  {
    name: "documentTemplateName",
    id: "documentTemplateName",
    type: InputType.TEXT,
    label: "Bezeichnung gemäß Vorlage",
    inputAttributes: {
      ariaLabel: "Bezeichnung gemäß Vorlage",
    },
  },
]
