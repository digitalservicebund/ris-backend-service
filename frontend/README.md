# Frontend

Built with Vue

## Prerequisites

The repository contains a `.node-version` file to set up the required Node.js version automatically with [nodenv](https://github.com/nodenv/nodenv).

Install dependencies:

```bash
npm install
```

For E2E and a11y testing with [Playwright](https://playwright.dev/docs/intro) you will need to install the supported browsers, if you don't have them already:

```bash
npx playwright install chrome firefox
```

## Development

### Dev server

The project uses [Vite](https://vitejs.dev/guide/) to provide a fast bundler-less [dev server](http://127.0.0.1/).

You have two options:

1. Run all services together:

```bash
./run.sh dev
```

2. Start the frontend dev server in isolation:

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

**Gather coverage:**

```bash
npm run coverage
```

**To run the E2E tests:**

(Requires the application to run locally.)

```bash
# run all
npm run test:e2e

# run all from a specific file
npm run test:e2e filename.spec.ts

# run a specific test in a specific file
npx playwright test filename.spec.ts -g "test name"

# for a less cluttered terminal during dev, you can comment out a browser in playwright.config.ts
```

**To run the a11y tests:**

```bash
npm run test:a11y
```

### Style (linting & formatting)

Check our [Frontend Styleguide](FRONTEND_STYLEGUIDE.md) document.

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
