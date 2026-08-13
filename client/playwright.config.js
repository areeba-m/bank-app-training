import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./src/tests/e2e",

  fullyParallel: false,

  use: {
    baseURL: "http://localhost:3000",

    browserName: "firefox",

    trace: "on-first-retry",

    screenshot: "only-on-failure",

    video: "retain-on-failure",
  },

  webServer: {
    command: "npm run dev -- --host 0.0.0.0",
    url: "http://localhost:3000",
    reuseExistingServer: true,
  },

  reporter: "html",
});
