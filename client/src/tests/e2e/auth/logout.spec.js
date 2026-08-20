import { expect, test } from "@playwright/test";
import {
    loginAsAdmin,
    loginAsUser,
    logout,
} from "../helper/auth.helpers";


test.describe("Authentication - Logout", () => {
    test("admin can logout successfully", async ({
                                                     page,
                                                 }) => {
        await loginAsAdmin(page);

        await expect(page).toHaveURL(
            /\/admin\/dashboard/,
        );

        await logout(page);

        await expect(page).toHaveURL(/\/login/);
    });

    test("user can logout successfully", async ({
                                                    page,
                                                }) => {
        await loginAsUser(page);

        await expect(page).toHaveURL(
            /\/user\/dashboard/,
        );

        await logout(page);

        await expect(page).toHaveURL(/\/login/);
    });

    test("logged out user cannot access previous protected page", async ({
                                                                             page,
                                                                         }) => {
        await loginAsUser(page);

        await expect(page).toHaveURL(
            /\/user\/dashboard/,
        );

        await logout(page);

        // Try accessing the protected route again
        await page.goto("/user/dashboard");

        await expect(page).toHaveURL(/\/login/);
    });
});