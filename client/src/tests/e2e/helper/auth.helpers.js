import { expect } from "@playwright/test";

export const ADMIN = {
    email: "admin@bank.com",
    password: "admin",
};

export const USER = {
    email: "user@bank.com",
    password: "user123",
};

export const USER_PASSWORD = "Playwright@123";

export async function login(page, email, password) {
    await page.goto("/login");

    await expect(
        page.getByRole("heading", {
            name: /portal sign in/i,
        }),
    ).toBeVisible();

    await page.getByLabel("Email Address *").fill(email);
    await page.getByLabel("Password *").fill(password);

    await page
        .getByRole("button", {
            name: /^sign in$/i,
        })
        .click();
}

export async function loginAsAdmin(page) {
    await login(page, ADMIN.email, ADMIN.password);

    await expect(page).toHaveURL(/\/admin\/dashboard/);
}

export async function loginAsUser(page) {
    await login(page, USER.email, USER.password);

    await expect(page).toHaveURL(/\/user\/dashboard/);
}

export async function loginAsCreatedUser(page, user) {
    await login(page, user.email, USER_PASSWORD);

    await expect(page).toHaveURL(/\/user\/dashboard/);
}

export async function logout(page) {
    const logoutButton = page.getByRole("button", {
        name: /^logout$/i,
    });

    await expect(logoutButton).toBeVisible();

    await logoutButton.click();

    await expect(page).toHaveURL(/\/login/);
}