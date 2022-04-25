import type { Config } from "@jest/types"

const config: Config.InitialOptions = {
  moduleFileExtensions: ["js", "ts", "json"],
  transform: {
    "^.+\\.ts$": "ts-jest",
  },
  testEnvironment: "jsdom",
  testPathIgnorePatterns: [
    "<rootDir>/node_modules/",
    "<rootDir>/test/e2e",
    "<rootDir>/test/a11y",
  ],
  setupFilesAfterEnv: ["<rootDir>/jest.setup.ts"],
  collectCoverageFrom: [
    "src/**/*.ts",
    "!src/main.ts", // No need to cover bootstrap file
    "!**/*.d.ts",
  ],
}

export default config
