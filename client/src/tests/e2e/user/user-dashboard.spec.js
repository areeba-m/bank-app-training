import {expect, test} from "@playwright/test";

import {loginAsAdmin, logout,} from "../helper/auth.helpers";

import {createAccountAsAdmin,} from "../helper/admin.helpers";

import {loginAsUser, onboardUser,} from "../helper/user-onboarding.helpers";

test.describe("User Dashboard", () => {
    test(
        "user can view account, manage balance and transfer money",
        async ({page}) => {
            test.setTimeout(90_000);

            const timestamp = Date.now();

            const user1 = {
                name: `Playwright Sender ${timestamp}`,
                email: `sender-${timestamp}@bank.com`,
                address: "Lahore, Pakistan",
            };

            const user2 = {
                name: `Playwright Recipient ${timestamp}`,
                email: `recipient-${timestamp}@bank.com`,
                address: "Lahore, Pakistan",
            };

            await loginAsAdmin(page);

            await createAccountAsAdmin(page, user1);
            await createAccountAsAdmin(page, user2);

            await logout(page);

            await onboardUser(page, user1);
            await onboardUser(page, user2);

            await loginAsUser(page, user1);

            await expect(
                page.getByText(user1.name, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText(user1.email, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText(user1.address, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText("Account Balance Statement"),
            ).toBeVisible();

            await expect(
                page.getByText("Account Attributes"),
            ).toBeVisible();

            await expect(
                page.getByText("Available Balance"),
            ).toBeVisible();

            const balance = page
                .locator("p")
                .filter({
                    hasText: /^\$\d/,
                })
                .first();

            await expect(balance).toBeVisible();

            const initialBalance = Number(
                (await balance.textContent()).replace(
                    /[$,]/g,
                    "",
                ),
            );


            await expect(
                page.getByText("Recent Ledger"),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Txn ID",
                }),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Date & Time",
                }),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Description",
                }),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Counter Party",
                }),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Indicator",
                }),
            ).toBeVisible();

            await expect(
                page.getByRole("columnheader", {
                    name: "Amount ($)",
                }),
            ).toBeVisible();

            await page.getByRole("button", {
                name: /^deposit$/i,
            }).click();

            await expect(
                page.getByRole("heading", {
                    name: /new transaction/i,
                }),
            ).toBeVisible();

            await page
                .locator('input[type="number"]')
                .fill("100");

            await page
                .locator('input[type="text"]')
                .fill("Playwright deposit");

            await page.getByRole("button", {
                name: /confirm transaction/i,
            }).click();

            await expect(
                page.getByText(
                    "Transaction executed successfully.",
                ),
            ).toBeVisible();

            const balanceAfterDeposit =
                initialBalance + 100;

            await expect
                .poll(async () => {
                    const text =
                        await balance.textContent();

                    return Number(
                        text.replace(/[$,]/g, ""),
                    );
                })
                .toBe(balanceAfterDeposit);

            await page.getByRole("button", {
                name: /^withdraw$/i,
            }).click();

            await expect(
                page.getByRole("heading", {
                    name: /new transaction/i,
                }),
            ).toBeVisible();

            await page
                .locator('input[type="number"]')
                .fill("50");

            await page
                .locator('input[type="text"]')
                .fill("Playwright withdrawal");

            await page.getByRole("button", {
                name: /confirm transaction/i,
            }).click();

            await expect(
                page.getByText(
                    "Transaction executed successfully.",
                ),
            ).toBeVisible();

            const balanceAfterWithdrawal =
                balanceAfterDeposit - 50;

            await expect
                .poll(async () => {
                    const text =
                        await balance.textContent();

                    return Number(
                        text.replace(/[$,]/g, ""),
                    );
                })
                .toBe(balanceAfterWithdrawal);

            const transferAmount = 25;
            await page
                .locator("button")
                .filter({hasText: /^Transfer Money$/})
                .first()
                .click();

            await expect(
                page.getByRole("heading", {
                    name: /transfer money/i,
                }),
            ).toBeVisible();
            await page
                .locator('input[type="email"]')
                .fill(user2.email);
            await page
                .locator('input[type="number"]')
                .fill(String(transferAmount));

            await page
                .locator('input[type="text"]')
                .fill("Playwright transfer");

            await page
                .getByRole("button", {
                    name: "Transfer Money",
                    exact: true,
                })
                .last()
                .click();

            await expect(
                page.getByText(
                    "Money transferred successfully.",
                ),
            ).toBeVisible();

            const expectedSenderBalance =
                balanceAfterWithdrawal -
                transferAmount;

            await expect
                .poll(async () => {
                    const text =
                        await balance.textContent();

                    return Number(
                        text.replace(/[$,]/g, ""),
                    );
                })
                .toBe(expectedSenderBalance);

            await expect(
                page.getByText("Playwright transfer"),
            ).toBeVisible();

            await expect(
                page.getByText(user2.email),
            ).toBeVisible();

            await logout(page);
            await loginAsUser(page, user2);
            await expect(
                page.getByText(user2.name, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText(user2.email, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText(user2.address, {
                    exact: true,
                }),
            ).toBeVisible();

            const recipientBalance = page
                .locator("p")
                .filter({
                    hasText: /^\$\d/,
                })
                .first();

            await expect(
                recipientBalance,
            ).toBeVisible();
            await expect
                .poll(async () => {
                    const text =
                        await recipientBalance.textContent();

                    return Number(
                        text.replace(/[$,]/g, ""),
                    );
                })
                .toBe(transferAmount);
            await expect(
                page.getByText("Playwright transfer"),
            ).toBeVisible();

            await expect(
                page.getByText(user1.email),
            ).toBeVisible();
        },
    );
});