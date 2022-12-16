import { InputField, InputType } from "@/domain"

export const citationDate: InputField[] = [
  {
    name: "citationDate",
    type: InputType.DATE,
    label: "Zitierdatum",
    inputAttributes: {
      ariaLabel: "Zitierdatum",
    },
  },
]
