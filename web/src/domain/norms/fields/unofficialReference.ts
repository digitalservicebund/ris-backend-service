import { InputField, InputType } from "@/domain"

export const unofficialReference: InputField[] = [
  {
    name: "unofficialReference",
    type: InputType.TEXT,
    label: "Nichtamtliche Fundstelle",
    inputAttributes: {
      ariaLabel: "Nichtamtliche Fundstelle",
    },
  },
]
