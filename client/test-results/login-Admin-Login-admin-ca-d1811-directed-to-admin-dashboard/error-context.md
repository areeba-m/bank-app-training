# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: login.spec.js >> Admin Login >> admin can login and is redirected to admin dashboard
- Location: src/tests/e2e/login.spec.js:4:3

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByText('ADMIN', { exact: true })
Expected: visible
Error: strict mode violation: getByText('ADMIN', { exact: true }) resolved to 2 elements:
    1) <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold tracking-wide uppercase shadow-xs bg-burgundy-700 text-white shadow-burgundy-glow">…</span> aka getByRole('banner').getByText('ADMIN')
    2) <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-burgundy-700 text-white">ADMIN</span> aka getByRole('cell', { name: 'ADMIN', exact: true }).locator('span')

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByText('ADMIN', { exact: true })

```

# Page snapshot

```yaml
- generic [ref=e3]:
    - banner [ref=e4]:
        - generic [ref=e5]:
            - link "AURA BANK" [ref=e6] [cursor=pointer]:
                - /url: /admin/dashboard
                - generic [ref=e17]:
                    - text: AURA
                    - generic [ref=e18]: BANK
            - generic [ref=e20]:
                - generic [ref=e21]: ADMIN
                - button "Logout" [ref=e24] [cursor=pointer]
    - generic [ref=e30]:
        - complementary [ref=e31]:
            - generic [ref=e33]:
                - paragraph [ref=e34]: Admin Portal
                - navigation [ref=e35]:
                    - link "Accounts Management" [ref=e36] [cursor=pointer]:
                        - /url: /admin/dashboard
        - main [ref=e43]:
            - generic [ref=e44]:
                - generic [ref=e45]:
                    - generic [ref=e46]:
                        - generic [ref=e47]: ADMIN PORTAL
                        - heading "Accounts Directory" [level=1] [ref=e50]
                    - button "New Account" [ref=e51] [cursor=pointer]
                - generic [ref=e55]:
                    - generic [ref=e63]:
                        - paragraph [ref=e64]: Total Accounts
                        - paragraph [ref=e65]: "2"
                    - generic [ref=e71]:
                        - paragraph [ref=e72]: Total Deposits
                        - paragraph [ref=e73]: $500.00
                - generic [ref=e74]:
                    - generic [ref=e75]:
                        - textbox "Search by ID, Name, or Email..." [ref=e77]
                        - button "Refresh" [ref=e81] [cursor=pointer]
                    - table [ref=e88]:
                        - rowgroup [ref=e89]:
                            - row [ref=e90]:
                                - columnheader "Account ID" [ref=e91]
                                - columnheader "Name" [ref=e92]
                                - columnheader "Email" [ref=e93]
                                - columnheader "Address" [ref=e94]
                                - columnheader "Role" [ref=e95]
                                - columnheader "Balance ($)" [ref=e96]
                                - columnheader "Actions" [ref=e97]
                        - rowgroup [ref=e98]:
                            - row [ref=e99]:
                                - cell "1" [ref=e100]
                                - cell "Admin" [ref=e101]
                                - cell "admin@bank.com" [ref=e102]
                                - cell "Bank Head Office" [ref=e103]
                                - cell "ADMIN" [ref=e104]
                                - cell "$0.00(CR)" [ref=e105]:
                                    - text: $0.00
                                    - generic [ref=e106]: (CR)
                                - cell [ref=e107]:
                                    - button "View Details" [ref=e108] [cursor=pointer]
                                    - button "Edit Account" [ref=e112] [cursor=pointer]
                                    - button "Cannot delete admin" [disabled] [ref=e116]
                            - row [ref=e123]:
                                - cell "2" [ref=e124]
                                - cell "Standard User" [ref=e125]
                                - cell "user@bank.com" [ref=e126]
                                - cell "123 Main Street" [ref=e127]
                                - cell "USER" [ref=e128]
                                - cell "$500.00(CR)" [ref=e129]:
                                    - text: $500.00
                                    - generic [ref=e130]: (CR)
                                - cell [ref=e131]:
                                    - button "View Details" [ref=e132] [cursor=pointer]
                                    - button "Edit Account" [ref=e136] [cursor=pointer]
                                    - button "Delete Account" [ref=e140] [cursor=pointer]
                    - generic [ref=e147]:
                        - generic [ref=e148]:
                            - text: Showing page 1 of 1
                            - generic [ref=e149]: (2 accounts)
                        - generic [ref=e150]:
                            - button "← Previous" [disabled] [ref=e151]
                            - button "Next →" [disabled] [ref=e152]
```

# Test source

```ts
  1  | import { expect, test } from "@playwright/test";
  2  |
  3  | test.describe("Admin Login", () => {
  4  |   test("admin can login and is redirected to admin dashboard", async ({
  5  |     page,
  6  |   }) => {
  7  |     await page.goto("/login");
  8  |     await expect(
  9  |       page.getByRole("heading", {
  10 |         name: /portal sign in/i,
  11 |       }),
  12 |     ).toBeVisible();
  13 |     await page.getByLabel("Email Address *").fill("admin@bank.com");
  14 |
  15 |     await page.getByLabel("Password *").fill("admin");
  16 |     await page
  17 |       .getByRole("button", {
  18 |         name: /^sign in$/i,
  19 |       })
  20 |       .click();
  21 |     await expect(page).toHaveURL(/\/admin\/dashboard/);
> 22 |     await expect(page.getByText("ADMIN", { exact: true })).toBeVisible();
     |                                                            ^ Error: expect(locator).toBeVisible() failed
  23 |   });
  24 | });
  25 |
```
