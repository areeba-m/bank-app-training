import { expect, test } from "@playwright/test";

test.describe("Authentication", () => {
  test("admin can login and is redirected to admin dashboard", async ({
    page,
  }) => {
    await page.goto("/login");

    await expect(
      page.getByRole("heading", {
        name: /portal sign in/i,
      }),
    ).toBeVisible();

    await page.getByLabel("Email Address *").fill("admin@bank.com");
    await page.getByLabel("Password *").fill("admin");

    await page
      .getByRole("button", {
        name: /^sign in$/i,
      })
      .click();

    await expect(page).toHaveURL(/\/admin\/dashboard/);
    await expect(page.getByText("ADMIN", { exact: true })).toBeVisible();
  });

  test("user can login and is redirected to user dashboard", async ({
    page,
  }) => {
    await page.goto("/login");

    await expect(
      page.getByRole("heading", {
        name: /portal sign in/i,
      }),
    ).toBeVisible();

    await page.getByLabel("Email Address *").fill("user@bank.com");
    await page.getByLabel("Password *").fill("user123");

    await page
      .getByRole("button", {
        name: /^sign in$/i,
      })
      .click();

    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByText("USER", { exact: true })).toBeVisible();
  });
});
