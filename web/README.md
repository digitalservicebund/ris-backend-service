# frontend

Built with Vue

## Prerequisites

**Node.js 16.14.2**

The repository contains a `.node-version` file to set up the required Node.js version automatically with [nodenv](https://github.com/nodenv/nodenv).

Install dependencies:

```bash
npm i
```

For E2E and a11y testing with [Playwright](https://playwright.dev/docs/intro) you will need to install the supported browsers:

```bash
npx playwright install
npx playwright install msedge
```

For the provided Git hooks you will need:

```bash
brew install lefthook talisman
```

## Development

### Dev server

The project uses [Vite](https://vitejs.dev/guide/) to provide a fast bundler-less [dev server](http://localhost:3000/mitra-frontend/).

**Start server**:

```bash
npm run dev
```

### Testing

The application has

- unit tests (using [Vitest](https://github.com/vitest-dev/vitest))
- end-to-end tests (using [Playwright](https://playwright.dev/docs/intro))
- accessibility tests (using [Axe](https://github.com/abhinaba-ghosh/axe-playwright#readme) and [Playwright](https://playwright.dev/docs/intro))

**To run the unit tests:**

```bash
npm test
```

**With watcher:**

```bash
npx vitest
```

**Gather coverage**:

```bash
npm run coverage
```

**To run the E2E tests:**

```bash
npm run test:e2e
```

**To run the a11y tests:**

```bash
npm run test:a11y
```

### Style (linting & formatting)

Linting is done via [ESLint](https://eslint.org/docs/user-guide/getting-started); consistent formatting for a variety of source code files is being enforced using [Prettier](https://prettier.io/docs/en/index.html). ESLint and Prettier work in conjunction.

**Check style:**

```bash
npm run style:check
```

**Autofix issues:**

```bash
npm run style:fix
```

(Some problems might not autofix.)
