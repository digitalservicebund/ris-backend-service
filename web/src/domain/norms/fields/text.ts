import { InputField, InputType } from "@/domain"

export const text: InputField[] = [
  {
    name: "text",
    type: InputType.TEXT,
    label: "Text",
    inputAttributes: {
      ariaLabel: "Text",
    },
  },
]
