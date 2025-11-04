import {
  DocumentationType,
  IgnoredTextCheckWord,
  Match,
  Suggestion,
} from "@/types/textCheck"

function generateIgnoredWord(type: DocumentationType): IgnoredTextCheckWord {
  return {
    id: "0dd15ae7-bece-4133-9eb3-e01563a39102",
    type: type,
    word: "Rechtshcreibfehler",
  } as IgnoredTextCheckWord
}

function generateMatch(id?: number): Match {
  return {
    id: id ?? 1,
    word: "Rechtshcreibfehler",
    message: "Möglicher Tippfehler gefunden.",
    shortMessage: "Rechtschreibfehler",
    category: "reasons",
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
    ignoredTextCheckWords: [
      {
        word: "Rechtshcreibfehler",
        type: "documentation_unit",
      },
      {
        word: "Rechtshcreibfehler",
        type: "global",
      },
    ],
  }
}

const matches: Match[] = [
  {
    id: 4,
    word: "Rechtshcreibfehler",
    message: "Möglicher Tippfehler gefunden.",
    shortMessage: "Rechtschreibfehler",
    category: "tenor",
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

export { suggestions, matches, generateMatch, generateIgnoredWord }
