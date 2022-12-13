import { InputField, InputType } from "@/domain"

export const digitalAnnouncement: InputField[] = [
  {
    name: "digitalAnnouncementMedium",
    type: InputType.TEXT,
    label: "Verkündungsmedium",
    inputAttributes: {
      ariaLabel: "Verkündungsmedium",
    },
  },
  {
    name: "digitalAnnouncementDate",
    type: InputType.DATE,
    label: "Verkündungsdatum",
    inputAttributes: {
      ariaLabel: "Verkündungsdatum",
      isFutureDate: true,
    },
  },
  {
    name: "digitalAnnouncementEdition",
    type: InputType.TEXT,
    label: "Ausgabenummer",
    inputAttributes: {
      ariaLabel: "Ausgabenummer",
    },
  },
  {
    name: "digitalAnnouncementYear",
    type: InputType.TEXT,
    label: "Jahr",
    inputAttributes: {
      ariaLabel: "Jahr",
    },
  },
  {
    name: "digitalAnnouncementPage",
    type: InputType.TEXT,
    label: "Seitenzahlen",
    inputAttributes: {
      ariaLabel: "Seitenzahlen",
    },
  },
  {
    name: "digitalAnnouncementArea",
    type: InputType.TEXT,
    label: "Bereich der Veröffentlichung",
    inputAttributes: {
      ariaLabel: "Bereich der Veröffentlichung",
    },
  },
  {
    name: "digitalAnnouncementAreaNumber",
    type: InputType.TEXT,
    label: "Nummer der Veröffentlichung im jeweiligen Bereich",
    inputAttributes: {
      ariaLabel: "Nummer der Veröffentlichung im jeweiligen Bereich",
    },
  },
  {
    name: "digitalAnnouncementInfo",
    type: InputType.TEXT,
    label: "Zusatzangaben",
    inputAttributes: {
      ariaLabel: "Zusatzangaben",
    },
  },
  {
    name: "digitalAnnouncementExplanations",
    type: InputType.TEXT,
    label: "Erläuterungen",
    inputAttributes: {
      ariaLabel: "Erläuterungen",
    },
  },
]
