import { defineConfig } from 'vitest/config'

export default defineConfig({
    test: {
        exclude: [
            "**/node_modules/**",
            "**/e2e/**",
            "**/a11y/**",
        ],
        coverage: {
            reporter: ['text', 'json', 'html'],
        },
        environment: "jsdom",
        globals: true,
    },
})
