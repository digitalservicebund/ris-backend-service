// See https://github.com/import-js/eslint-plugin-import
const moduleImportRules = {
  "import/exports-last": 2,
  "import/first": 2,
  "import/newline-after-import": 2,
  "import/no-duplicates": 2,
  "import/order": [
    "error",
    {
      alphabetize: {
        order: "asc",
        caseInsensitive: true,
      },
    },
  ],
  "import/no-unresolved": [0, { caseSensitive: false }],
}
module.exports = {
  root: true,
  env: {
    node: true,
    es6: true,
  },
  parser: "@typescript-eslint/parser",
  parserOptions: {
    ecmaVersion: 2022,
  },
  overrides: [
    // Avoid linting JavaScript config files with TypeScript rules...
    {
      files: ["**/*.{ts,vue}"],
      extends: [
        "plugin:import/recommended",
        "plugin:import/typescript",
        "plugin:vue/vue3-recommended",
        "plugin:vuejs-accessibility/recommended",
        "@vue/typescript/recommended",
        "@vue/prettier",
        "@vue/eslint-config-prettier",
      ],
      rules: { ...moduleImportRules },
    }, // ...and avoid linting TypeScript files with ES rules for JavaScript config files!
    {
      files: ["**/*.js"],
      extends: ["eslint:recommended", "plugin:import/recommended"],
      rules: { ...moduleImportRules },
    },
  ],
}
