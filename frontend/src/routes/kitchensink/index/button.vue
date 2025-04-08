<script lang="ts" setup>
import Button from "primevue/button"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import IconClear from "~icons/ic/baseline-clear"
</script>

<template>
  <KitchensinkPage name="Button">
    <KitchensinkStory name="Icons">
      <div class="flex gap-20">
        <p>
          All icons in the
          <a
            class="ris-link1-bold"
            href="https://icon-sets.iconify.design/ic/"
            rel="noopener"
            target="_blank"
            >Google Material</a
          >,
          <a
            class="ris-link1-bold"
            href="https://icon-sets.iconify.design/material-symbols"
            rel="noopener"
            target="_blank"
            >Material Symbols</a
          >
          and
          <a
            class="ris-link1-bold"
            href="https://icon-sets.iconify.design/mdi/"
            rel="noopener"
            target="_blank"
            >Material Design Icons</a
          >
          sets can be used
        </p>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Primary">
      <div class="flex gap-20">
        <Button label="Button Primary"> </Button>
        <Button label="Button Primary With Icon">
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button label="Button Primary With Icon on the Right">
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button>
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button disabled label="Disabled"> </Button>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Secondary">
      <div class="flex gap-20">
        <Button label="Button Secondary" severity="secondary"> </Button>
        <Button label="Button Secondary With Icon" severity="secondary">
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button
          label="Button Secondary With Icon on the Right"
          severity="secondary"
        >
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button severity="secondary">
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button disabled label="Disabled" severity="secondary"> </Button>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Ghost">
      <div class="flex gap-20">
        <Button label="Button Ghost" text> </Button>
        <Button label="Button Ghost With Icon" text>
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button label="Button Ghost With Icon on the Right" text>
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button text>
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button disabled label="Disabled" text> </Button>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Danger">
      <div class="flex gap-20">
        <Button label="Button Danger" severity="danger"> </Button>
        <Button label="Button Danger With Icon" severity="danger">
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button label="Button Danger With Icon on the Right" severity="danger">
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button severity="danger">
          <template #icon>
            <IconClear class="order-last" />
          </template>
        </Button>
        <Button disabled label="Disabled" severity="danger"> </Button>
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Sizes">
      <div class="flex gap-20">
        <Button label="Button Small" size="small">
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button label="Button Normal" size="normal">
          <template #icon>
            <IconClear />
          </template>
        </Button>
        <Button label="Button Large" size="large">
          <template #icon>
            <IconClear />
          </template>
        </Button>
      </div>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
