import { Match, Suggestion } from "@/types/textCheck"

const matches: Match[] = [
  {
    id: 1,
    word: "Rechtshcreibfehler",
    message: "Möglicher Tippfehler gefunden.",
    shortMessage: "Rechtschreibfehler",
    category: "reasons",
    replacements: [
      {
        value: "Rechtschreibfehler",
      },
    ],
    offset: 3,
    htmlOffset: 0,
    length: 18,
    context: {
      text: "<p>Rechtshcreibfehler werden rot markirt</p>",
      offset: 3,
      length: 18,
    },
    sentence: "<p>Rechtshcreibfehler werden rot markirt</p>",
    type: {
      typeName: "UnknownWord",
    },
    rule: {
      id: "GERMAN_SPELLER_RULE",
      description: "Möglicher Rechtschreibfehler",
      issueType: "misspelling",
      category: {
        id: "TYPOS",
        name: "Mögliche Tippfehler",
      },
    },
    ignoreForIncompleteSentence: false,
    contextForSureMatch: 0,
  },
  {
    id: 4,
    word: "Rechtshcreibfehler",
    message: "Möglicher Tippfehler gefunden.",
    shortMessage: "Rechtschreibfehler",
    category: "tenor",
    replacements: [
      {
        value: "Rechtschreibfehler",
      },
    ],
    offset: 3,
    length: 18,
    context: {
      text: "<p>Rechtshcreibfehler werden rot markirt</p>",
      offset: 3,
      length: 18,
    },
    sentence: "<p>Rechtshcreibfehler werden rot markirt</p>",
    type: {
      typeName: "UnknownWord",
    },
    rule: {
      id: "GERMAN_SPELLER_RULE",
      description: "Möglicher Rechtschreibfehler",
      issueType: "misspelling",
      category: {
        id: "TYPOS",
        name: "Mögliche Tippfehler",
      },
    },
    ignoreForIncompleteSentence: false,
    contextForSureMatch: 0,
  },
]

const suggestions: Suggestion[] = [
  {
    word: "Rechtshcreibfehler",
    matches: matches,
  },
]

export { suggestions, matches }
