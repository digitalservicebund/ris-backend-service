import RisButton from "../components/RisButton.vue"

export default {
  title: "Components/Button",
  component: RisButton,
  argTypes: {
    backgroundColor: { control: "color" },
    onClick: {},
    size: {
      control: { type: "select" },
      options: ["small", "medium", "large"],
    },
  },
}

const Template = (args) => ({
  components: { RisButton },
  setup() {
    return { args }
  },
  template: '<ris-button v-bind="args" />',
})

export const Primary = Template.bind({})
Primary.args = {
  primary: true,
  label: "Button",
}
