import { expect, test } from "@playwright/test";
import {ADMIN, USER, login,} from "../helper/auth.helpers";


test.describe("Authentication - Login", () => {
    test("admin can login and is redirected to admin dashboard", async ({
                                                                            page,
                                                                        }) => {
        await login(
            page,
            ADMIN.email,
            ADMIN.password,
        );

        await expect(page).toHaveURL(
            /\/admin\/dashboard/,
        );

        await expect(
            page.getByText("ADMIN", {
                exact: true,
            }),
        ).toBeVisible();
    });

    test("user can login and is redirected to user dashboard", async ({
                                                                          page,
                                                                      }) => {
        await login(
            page,
            USER.email,
            USER.password,
        );

        await expect(page).toHaveURL(
            /\/user\/dashboard/,
        );

        await expect(
            page.getByText("USER", {
                exact: true,
            }),
        ).toBeVisible();
    });

    test("user cannot login with invalid credentials", async ({
                                                                  page,
                                                              }) => {
        await page.goto("/login");

        await page
            .getByLabel("Email Address *")
            .fill("invalid@bank.com");

        await page
            .getByLabel("Password *")
            .fill("wrongpassword");

        await page
            .getByRole("button", {
                name: /^sign in$/i,
            })
            .click();

        await expect(
            page.getByText(
                /invalid email|invalid credentials|login failed/i,
            ),
        ).toBeVisible();

        await expect(page).toHaveURL(/\/login/);
    });
});