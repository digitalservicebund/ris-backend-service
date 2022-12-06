import { InputField, InputType } from "@/domain"

export const definition: InputField[] = [
  {
    name: "definition",
    type: InputType.TEXT,
    label: "Definition",
    inputAttributes: {
      ariaLabel: "Definition",
    },
  },
]
