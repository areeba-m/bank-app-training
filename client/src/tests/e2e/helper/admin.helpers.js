import { expect } from "@playwright/test";

export async function createAccountAsAdmin(page, user) {
    await page
        .getByRole("button", {
            name: /new account/i,
        })
        .click();

    await expect(
        page.getByRole("heading", {
            name: "Create New Account",
        }),
    ).toBeVisible();

    await page
        .locator('input[name="new-account-name"]')
        .fill(user.name);

    await page
        .locator('input[name="new-account-email"]')
        .fill(user.email);

    await page
        .locator('input[name="new-account-address"]')
        .fill(user.address);

    await page
        .getByRole("button", {
            name: "Create Account",
        })
        .click();

    await expect(
        page.getByRole("heading", {
            name: "Create New Account",
        }),
    ).not.toBeVisible();

    await expect(page).toHaveURL(
        /\/admin\/dashboard/,
    );
}