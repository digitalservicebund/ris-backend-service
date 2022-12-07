import { InputField, InputType } from "@/domain"

export const status: InputField[] = [
  {
    name: "statusNote",
    type: InputType.TEXT,
    label: "Änderungshinweis",
    inputAttributes: {
      ariaLabel: "Änderungshinweis",
    },
  },
  {
    name: "statusDescription",
    type: InputType.TEXT,
    label: "Bezeichnung der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Bezeichnung der Änderungsvorschrift",
    },
  },
  {
    name: "statusDate",
    type: InputType.DATE,
    label: "Datum der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Datum der Änderungsvorschrift",
    },
  },
  {
    name: "statusReference",
    type: InputType.TEXT,
    label: "Fundstellen der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Fundstellen der Änderungsvorschrift",
    },
  },
]
