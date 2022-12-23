import { InputField, InputType } from "@/domain"

export const ageOfMajorityIndication: InputField[] = [
  {
    name: "ageOfMajorityIndication",
    type: InputType.TEXT,
    label: "Angaben zur Volljährigkeit",
    inputAttributes: {
      ariaLabel: "Angaben zur Volljährigkeit",
    },
  },
]
