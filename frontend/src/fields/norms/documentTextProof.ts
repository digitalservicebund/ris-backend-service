import { InputField, InputType } from "@/shared/components/input/types"

export const documentTextProof: InputField[] = [
  {
    name: "documentTextProof",
    id: "documentTextProof",
    type: InputType.TEXT,
    label: "Textnachweis",
    inputAttributes: {
      ariaLabel: "Textnachweis",
    },
  },
]
