import { InputField, InputType } from "@/shared/components/input/types"

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
