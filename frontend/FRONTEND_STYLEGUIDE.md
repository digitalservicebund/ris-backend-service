# Frontend Styleguide 🎨

## Style

### (Linting & formatting)

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

### CSS

In order to make our code more readable, we decided to switch from using scss to use [Tailwind](https://tailwindcss.com/) utility classes in our components, so we could build design directly in our markup:

```html
// ❌ Bad
<div class="container">some text</div>

<style lang="scss" scoped>
  .container {
    background-color: "red";
  }
</style>
```

```html
// ✅ Good
<div class="bg-red-500">some text</div>
```

By adding the tailwind directives to our [global.scss](https://github.com/digitalservicebund/ris-backend-service/blob/main/frontend/src/styles/global.scss), we are able to use tailwind classes all over the project. Adding custom classes is possible by configuring the [tailwind.config.js](https://github.com/digitalservicebund/ris-backend-service/blob/main/frontend/tailwind.config.js).

However we have still some leftover scss classes (and in some cases it might be useful or more readable to use). In that case, we could also add tailwind utility classes, by using the @apply anotation. Using tailwind with preprocessors has some particularities, you could read about it [here](https://tailwindcss.com/docs/using-with-preprocessors). For example:

```css
// ❌ Bad
.alert {
  @apply bg-red-500 !important;
}

// ✅ Good
.alert {
  @apply bg-red-500 #{!important};
}
```

### Colors

Colors are defined in [tailwind.config.js](https://github.com/digitalservicebund/ris-backend-service/blob/main/frontend/tailwind.config.js) and should be always in sync with the [Angie Foundation Colors](https://www.figma.com/file/nMUUyvtI2vQxiC5hW2bjCS/Angie-Foundation?node-id=32%3A764&t=Kj3TtRomXvy1lrKI-0).

### Fonts

Fontstyles are defined in [\_font_styles.scss](https://github.com/digitalservicebund/ris-backend-service/blob/main/frontend/src/styles/_font_styles.scss) and should be always in sync with the [Angie Foundation Font Styles](https://www.figma.com/file/nMUUyvtI2vQxiC5hW2bjCS/Angie-Foundation?node-id=10%3A118&t=Kj3TtRomXvy1lrKI-0).

Please do not use other colors or fonts, than that ones defined in our styles. If you come across any unexpected colors or fonts do not hesitate to reach out to our designers, to always stay in sync with the Figma designs.

### Tests

For our tests we use playwright.
Our decision for the structure of tests are non-nested tests. Means every test setup the needed test values and clear they the after the test.

```typescript
// ❌ Bad
test.describe("test component", () => {
  test.describe("test something", () => {
    var value = 1
    ...
    test("test1", () => {
      expect(value, ...)
    })

    test("test2", () => {
      expect(value, ...)
    })
  })

  test("test3", () => {
    var xy = 1
    ...
    expect(xy, ...)
  })
})

// ✅ Good
test.describe("test component", () => {
  test("test1", () => {
    var value = 1
    ...
    expect(value, ...)
  })

  test("test2", () => {
    var value = 1
    ...
    expect(value, ...)
  })

  test("test3", () => {
    var xy = 1
    ...
    expect(xy, ...)
  })
})
```
