import { expect } from "@playwright/test";

import {
    waitForUserEmail,
    getLatestMail,
    extractOtp,
    extractVerificationLink,
} from "./mailpit.helpers";

export const USER_PASSWORD = "Playwright@123";

export async function onboardUser(page, user) {

    await waitForUserEmail(page, user.email);

    const email = await getLatestMail(
        page,
        user.email,
    );
    const otp = extractOtp(email);

    expect(otp).toMatch(/^\d{6}$/);


    const verificationLink = extractVerificationLink(email);

    expect(verificationLink).toContain(
        "/activate?email=",
    );

    await page.goto(verificationLink);

    await expect(page).toHaveURL(
        /\/activate\?email=/,
    );

    await expect(
        page.getByRole("heading", {
            name: "Enter Verification Code",
        }),
    ).toBeVisible();

    const otpInputs = page.getByRole(
        "textbox",
        {
            name: /OTP digit/,
        },
    );

    await expect(otpInputs).toHaveCount(6);

    for (let i = 0; i < 6; i++) {
        await otpInputs
            .nth(i)
            .fill(otp[i]);
    }

    await page
        .getByRole("button", {
            name: "Verify Email",
        })
        .click();

    await expect(
        page.getByRole("heading", {
            name: "Set Your Password",
        }),
    ).toBeVisible({
        timeout: 10_000,
    });

    await expect(
        page.getByRole("heading", {
            name: "Create Your Password",
        }),
    ).toBeVisible();

    const newPasswordInput =
        page.getByPlaceholder(
            "Enter your new password",
        );

    const confirmPasswordInput =
        page.getByPlaceholder(
            "Confirm your password",
        );

    await expect(newPasswordInput).toBeVisible();
    await expect(confirmPasswordInput).toBeVisible();

    await newPasswordInput.fill(USER_PASSWORD);
    await confirmPasswordInput.fill(USER_PASSWORD);

    await page
        .getByRole("button", {
            name: "Set Password",
        })
        .click();

    await expect(page).toHaveURL(
        /\/login/,
        {
            timeout: 15_000,
        },
    );
}

export async function loginAsUser(page, user) {
    await page.goto("/login");

    await page
        .getByLabel("Email Address *")
        .fill(user.email);

    await page
        .getByLabel("Password *")
        .fill(USER_PASSWORD);

    await page
        .getByRole("button", {
            name: /^sign in$/i,
        })
        .click();

    await expect(page).toHaveURL(
        /\/user\/dashboard/,
        {
            timeout: 10_000,
        },
    );
}