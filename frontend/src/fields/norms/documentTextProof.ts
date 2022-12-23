import { InputField, InputType } from "@/domain"

export const documentTextProof: InputField[] = [
  {
    name: "documentTextProof",
    type: InputType.TEXT,
    label: "Textnachweis",
    inputAttributes: {
      ariaLabel: "Textnachweis",
    },
  },
]
