import { InputField, InputType } from "@/domain"

export const otherFootnote: InputField[] = [
  {
    name: "otherFootnote",
    type: InputType.TEXT,
    label: "Sonstige Fußnote",
    inputAttributes: {
      ariaLabel: "Sonstige Fußnote",
    },
  },
]
