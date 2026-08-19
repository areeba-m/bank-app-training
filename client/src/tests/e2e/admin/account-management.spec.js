import { expect, test } from "@playwright/test";
import { loginAsAdmin } from "../helper/auth.helpers";

test.describe("Admin Account Management", () => {
    test("admin can create, view, edit and delete an account", async ({
                                                                          page,
                                                                      }) => {
        const timestamp = Date.now();

        const testAccount = {
            name: `Playwright User ${timestamp}`,
            email: `playwright-${timestamp}@bank.com`,
            address: "Lahore, Pakistan",
        };

        const updatedAccount = {
            name: `Updated User ${timestamp}`,
            address: "Islamabad, Pakistan",
        };
        await loginAsAdmin(page);

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
            .fill(testAccount.name);

        await page
            .locator('input[name="new-account-email"]')
            .fill(testAccount.email);

        await page
            .locator('input[name="new-account-address"]')
            .fill(testAccount.address);

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

        await expect(page).toHaveURL(/\/admin\/dashboard/);

        const searchInput = page.getByPlaceholder(
            "Search by ID, Name, or Email...",
        );

        await searchInput.fill(testAccount.email);

        const accountRow = page
            .locator("tbody tr")
            .filter({
                hasText: testAccount.email,
            });

        await expect(accountRow).toBeVisible();
        await expect(accountRow).toContainText(testAccount.name);
        await expect(accountRow).toContainText(testAccount.email);
        await expect(accountRow).toContainText(testAccount.address);

        await accountRow.getByTitle("View Details").click();

        await expect(page).toHaveURL(/\/admin\/account\/.+/);

        await expect(
            page.getByText(testAccount.email, {
                exact: true,
            }),
        ).toBeVisible();

        await expect(
            page.getByText(testAccount.name, {
                exact: true,
            }),
        ).toBeVisible();

        await page.goBack();

        await expect(page).toHaveURL(/\/admin\/dashboard/);

        await searchInput.fill(testAccount.email);

        const rowToEdit = page
            .locator("tbody tr")
            .filter({
                hasText: testAccount.email,
            });

        await expect(rowToEdit).toBeVisible();

        await rowToEdit.getByTitle("Edit Account").click();

        await expect(
            page.getByRole("heading", {
                name: /edit account/i,
            }),
        ).toBeVisible();

        await page
            .locator('input[name="new-account-name"]')
            .fill(updatedAccount.name);

        await page
            .locator('input[name="new-account-address"]')
            .fill(updatedAccount.address);

        const emailInput = page.locator(
            'input[name="new-account-email"]',
        );

        await expect(emailInput).toHaveValue(testAccount.email);
        await expect(emailInput).toBeDisabled();

        await page
            .getByRole("button", {
                name: "Save Changes",
            })
            .click();

        await expect(
            page.getByRole("heading", {
                name: /edit account/i,
            }),
        ).not.toBeVisible();

        await searchInput.fill(testAccount.email);

        const updatedRow = page
            .locator("tbody tr")
            .filter({
                hasText: testAccount.email,
            });

        await expect(updatedRow).toBeVisible();
        await expect(updatedRow).toContainText(updatedAccount.name);
        await expect(updatedRow).toContainText(updatedAccount.address);

        await updatedRow.getByTitle("Delete Account").click();

        await expect(
            page.getByRole("heading", {
                name: "Delete Account?",
            }),
        ).toBeVisible();

        await page
            .getByRole("button", {
                name: /^delete$/i,
            })
            .click();

        await expect(
            page.getByText(testAccount.email, {
                exact: true,
            }),
        ).not.toBeVisible();
    });
});