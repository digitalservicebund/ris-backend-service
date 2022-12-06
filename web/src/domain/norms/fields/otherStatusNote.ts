import { InputField, InputType } from "@/domain"

export const otherStatusNote: InputField[] = [
  {
    name: "otherStatusNote",
    type: InputType.TEXT,
    label: "Sonstiger Hinweis",
    inputAttributes: {
      ariaLabel: "Sonstiger Hinweis",
    },
  },
]
