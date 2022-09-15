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
  "import/no-unresolved": 0,
  "@typescript-eslint/array-type": "error",
  /*
   * This rules restricts our flexibility to write hierarchically separated
   * components with labels and inputs as it sometimes "can't see" the relation.
   * We decided to rather depend on the end-to-end accessibility tests which test
   * this relation too, but are code independent.
   */
  "vuejs-accessibility/label-has-for": "off",
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
        "plugin:vue-scoped-css/vue3-recommended",
        "@vue/typescript/recommended",
        "@vue/prettier",
        "@vue/eslint-config-prettier",
      ],
      rules: {
        ...moduleImportRules,
        "vue/no-static-inline-styles": "error",
        "vue/component-tags-order": [
          "error",
          {
            order: ["script", "template", "style"],
          },
        ],
      },
    }, // ...and avoid linting TypeScript files with ES rules for JavaScript config files!
    {
      files: ["**/*.js"],
      extends: ["eslint:recommended", "plugin:import/recommended"],
      rules: { ...moduleImportRules },
    },
    {
      files: ["test/**/*.ts"],
      extends: ["plugin:jest-dom/recommended"],
    },
  ],
}
