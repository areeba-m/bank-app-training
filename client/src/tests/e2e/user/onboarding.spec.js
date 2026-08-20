import { expect, test } from "@playwright/test";

import {
    loginAsAdmin,
    loginAsCreatedUser,
    logout,
} from "../helper/auth.helpers";

import {
    createAccountAsAdmin,
} from "../helper/admin.helpers";

import {
    onboardUser,
} from "../helper/user-onboarding.helpers";


test.describe("User Onboarding", () => {
    test(
        "new user can complete OTP onboarding and login",
        async ({ page }) => {
            test.setTimeout(60_000);

            const timestamp = Date.now();

            const newUser = {
                name: `Playwright User ${timestamp}`,
                email: `playwright-${timestamp}@bank.com`,
                address: "Lahore, Pakistan",
            };

            await loginAsAdmin(page);

            await createAccountAsAdmin(page, newUser);

            await logout(page);

            await onboardUser(page, newUser);

            await loginAsCreatedUser(page, newUser);

            await expect(
                page.getByText(newUser.name, {
                    exact: true,
                }),
            ).toBeVisible();

            await expect(
                page.getByText(newUser.email, {
                    exact: true,
                }),
            ).toBeVisible();
        },
    );
});