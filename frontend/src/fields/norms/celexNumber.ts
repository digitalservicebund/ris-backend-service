import { InputField, InputType } from "@/domain"

export const celexNumber: InputField[] = [
  {
    name: "celexNumber",
    type: InputType.TEXT,
    label: "CELEX-Nummer",
    inputAttributes: {
      ariaLabel: "CELEX-Nummer",
    },
  },
]
