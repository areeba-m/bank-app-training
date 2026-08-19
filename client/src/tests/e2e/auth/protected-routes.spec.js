import { expect, test } from "@playwright/test";
import {
    loginAsUser,
    loginAsAdmin,
} from "../helper/auth.helpers";

test.describe("Protected Routes", () => {
    test.describe("Unauthenticated user", () => {
        test("cannot access user dashboard", async ({
                                                        page,
                                                    }) => {
            await page.goto("/user/dashboard");

            await expect(page).toHaveURL(/\/login/);
        });

        test("cannot access admin dashboard", async ({
                                                         page,
                                                     }) => {
            await page.goto("/admin/dashboard");

            await expect(page).toHaveURL(/\/login/);
        });

        test("cannot access user profile", async ({
                                                      page,
                                                  }) => {
            await page.goto("/user/profile");

            await expect(page).toHaveURL(/\/login/);
        });

        test("cannot access user transactions", async ({
                                                           page,
                                                       }) => {
            await page.goto("/user/transactions");

            await expect(page).toHaveURL(/\/login/);
        });
    });

    test.describe("Authorization", () => {
        test("normal user cannot access admin dashboard", async ({
                                                                     page,
                                                                 }) => {
            await loginAsUser(page);

            await expect(page).toHaveURL(
                /\/user\/dashboard/,
            );

            await page.goto("/admin/dashboard");
            await expect(page).not.toHaveURL(
                /\/admin\/dashboard/,
            );
        });

        test("admin can access admin dashboard", async ({
                                                            page,
                                                        }) => {
            await loginAsAdmin(page);

            await expect(page).toHaveURL(
                /\/admin\/dashboard/,
            );

            await expect(
                page.getByText("ADMIN", {
                    exact: true,
                }),
            ).toBeVisible();
        });
    });
});