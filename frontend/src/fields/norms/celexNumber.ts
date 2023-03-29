import { InputField, InputType } from "@/shared/components/input/types"

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
