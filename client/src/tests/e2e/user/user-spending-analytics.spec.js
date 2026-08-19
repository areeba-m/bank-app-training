import { expect, test } from "@playwright/test";
import { loginAsUser } from "../helper/auth.helpers";

test.describe("User Spending Analytics", () => {
    test("user can perform transactions and view spending analytics", async ({
                                                                                 page,
                                                                             }) => {
        test.setTimeout(60_000);

        await loginAsUser(page);

        await expect(page).toHaveURL(/\/user\/dashboard/);

        await expect(
            page.getByRole("heading", {
                name: /welcome,/i,
            }),
        ).toBeVisible();

        const viewAllLink = page.getByRole("link", {
            name: /view all/i,
        });

        await expect(viewAllLink).toBeVisible();

        await viewAllLink.click();

        await expect(page).toHaveURL(/\/user\/transactions/);

        await expect(
            page.getByRole("heading", {
                name: "Transaction History",
            }),
        ).toBeVisible();

        await page.getByRole("button", {
            name: /new transaction/i,
        }).click();

        await expect(
            page.getByRole("heading", {
                name: "New Transaction",
            }),
        ).toBeVisible();

        await page.getByRole("button", {
            name: /deposit \(credit \/ cr\)/i,
        }).click();

        await expect(
            page.getByRole("heading", {
                name: "New Transaction",
            }),
        ).toBeVisible();

        const amountInput = page.locator('input[type="number"]');

        await expect(amountInput).toBeVisible();

        await amountInput.fill("100");

        await page
            .getByPlaceholder(
                "e.g. Utility bill payment or cash deposit",
            )
            .fill("Playwright analytics deposit");

        await page.getByRole("button", {
            name: /confirm transaction/i,
        }).click();

        await expect(
            page.getByText("Transaction executed successfully."),
        ).toBeVisible();

        await page.getByRole("button", {
            name: /new transaction/i,
        }).click();

        await expect(
            page.getByRole("heading", {
                name: "New Transaction",
            }),
        ).toBeVisible();

        await page.getByRole("button", {
            name: /withdraw \(debit \/ db\)/i,
        }).click();

        // Verify Withdraw is selected
        await expect(
            page.getByRole("button", {
                name: /withdraw \(debit \/ db\)/i,
            }),
        ).toHaveClass(/border-rose-500/);

        await amountInput.fill("50");

        await page
            .getByPlaceholder(
                "e.g. Utility bill payment or cash deposit",
            )
            .fill("Playwright analytics withdrawal");

        await page.getByRole("button", {
            name: /confirm transaction/i,
        }).click();

        await expect(
            page.getByText("Transaction executed successfully."),
        ).toBeVisible();

        await expect(
            page.getByRole("heading", {
                name: "Spending Analytics",
            }),
        ).toBeVisible();


        const today = new Date()
            .toISOString()
            .split("T")[0];

        const dateInputs = page.locator(
            'input[type="date"]',
        );

        await expect(dateInputs).toHaveCount(2);

        await dateInputs.nth(0).fill(today);
        await dateInputs.nth(1).fill(today);

        await expect(
            page.getByText("Total Spending"),
        ).toBeVisible({
            timeout: 30_000,
        });

        const totalSpendingSection = page
            .getByText("Total Spending")
            .locator("..");

        await expect(
            totalSpendingSection.locator("p").nth(1),
        ).toHaveText("$50.00");

        await expect(
            page.getByText("Spending Categories"),
        ).toBeVisible();

        await expect(
            page.getByText("Spending by Category"),
        ).toBeVisible();

        await expect(
            page.getByRole("columnheader", {
                name: "Category",
            }),
        ).toBeVisible();

        await expect(
            page.getByRole("columnheader", {
                name: "Spending",
            }),
        ).toBeVisible();

        await expect(
            page.getByRole("columnheader", {
                name: "Percentage",
            }),
        ).toBeVisible();

        const spendingTable = page
            .getByRole("columnheader", { name: "Category" })
            .locator("xpath=ancestor::table");

        const spendingRow = spendingTable.locator("tbody tr").filter({
            hasText: "$50.00",
        });

        await expect(spendingRow).toBeVisible();

        await expect(
            spendingRow.getByText("$50.00", { exact: true }),
        ).toBeVisible();

        await expect(
            spendingRow.getByText("100.0%", { exact: true }),
        ).toBeVisible();
    });
});