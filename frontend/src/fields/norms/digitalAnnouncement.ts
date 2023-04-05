import { InputField, InputType } from "@/shared/components/input/types"

export const digitalAnnouncement: InputField[] = [
  {
    name: "digitalAnnouncementMedium",
    id: "digitalAnnouncementMedium",
    type: InputType.TEXT,
    label: "Verkündungsmedium",
    inputAttributes: {
      ariaLabel: "Verkündungsmedium",
    },
  },
  {
    name: "digitalAnnouncementDate",
    id: "digitalAnnouncementDate",
    type: InputType.DATE,
    label: "Verkündungsdatum",
    inputAttributes: {
      ariaLabel: "Verkündungsdatum",
      isFutureDate: true,
    },
  },
  {
    name: "digitalAnnouncementEdition",
    id: "digitalAnnouncementEdition",
    type: InputType.TEXT,
    label: "Ausgabenummer",
    inputAttributes: {
      ariaLabel: "Ausgabenummer",
    },
  },
  {
    name: "digitalAnnouncementYear",
    id: "digitalAnnouncementYear",
    type: InputType.TEXT,
    label: "Jahr",
    inputAttributes: {
      ariaLabel: "Jahr",
    },
  },
  {
    name: "digitalAnnouncementPage",
    id: "digitalAnnouncementPage",
    type: InputType.TEXT,
    label: "Seitenzahlen",
    inputAttributes: {
      ariaLabel: "Seitenzahlen",
    },
  },
  {
    name: "digitalAnnouncementArea",
    id: "digitalAnnouncementArea",
    type: InputType.TEXT,
    label: "Bereich der Veröffentlichung",
    inputAttributes: {
      ariaLabel: "Bereich der Veröffentlichung",
    },
  },
  {
    name: "digitalAnnouncementAreaNumber",
    id: "digitalAnnouncementAreaNumber",
    type: InputType.TEXT,
    label: "Nummer der Veröffentlichung im jeweiligen Bereich",
    inputAttributes: {
      ariaLabel: "Nummer der Veröffentlichung im jeweiligen Bereich",
    },
  },
  {
    name: "digitalAnnouncementInfo",
    id: "digitalAnnouncementInfo",
    type: InputType.TEXT,
    label: "Zusatzangaben",
    inputAttributes: {
      ariaLabel: "Zusatzangaben",
    },
  },
  {
    name: "digitalAnnouncementExplanations",
    id: "digitalAnnouncementExplanations",
    type: InputType.TEXT,
    label: "Erläuterungen",
    inputAttributes: {
      ariaLabel: "Erläuterungen",
    },
  },
]
