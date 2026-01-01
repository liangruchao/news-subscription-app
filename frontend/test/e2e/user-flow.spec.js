import { test, expect } from '@playwright/test';

test.describe('用户认证流程', () => {
  test.beforeEach(async ({ page }) => {
    // 清理 cookies
    await page.context().clearCookies();
  });

  test('完整注册流程', async ({ page }) => {
    await page.goto('/register.html');

    // 填写注册表单
    await page.fill('#username', 'e2e_test_user');
    await page.fill('#email', 'e2e@example.com');
    await page.fill('#password', 'Password123');
    await page.fill('#confirmPassword', 'Password123');

    // 提交表单
    await page.click('button[type="submit"]');

    // 验证注册成功
    await expect(page).toHaveURL(/.*index\.html/);
  });

  test('注册失败 - 密码不一致', async ({ page }) => {
    await page.goto('/register.html');

    await page.fill('#username', 'testuser');
    await page.fill('#email', 'test@example.com');
    await page.fill('#password', 'Password123');
    await page.fill('#confirmPassword', 'DifferentPassword');

    await page.click('button[type="submit"]');

    // 应该看到错误消息
    const message = page.locator('.message.error');
    await expect(message).toBeVisible();
    await expect(message).toContainText('两次输入的密码不一致');
  });

  test('登录流程', async ({ page }) => {
    // 先注册用户
    await page.goto('/register.html');
    await page.fill('#username', 'login_test_user');
    await page.fill('#email', 'login@example.com');
    await page.fill('#password', 'Password123');
    await page.fill('#confirmPassword', 'Password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*index\.html/);

    // 登出
    await page.goto('/index.html');
    await page.click('a[href="#"]:has-text("登出")');

    // 登录
    await page.goto('/login.html');
    await page.fill('#username', 'login_test_user');
    await page.fill('#password', 'Password123');
    await page.click('button[type="submit"]');

    // 验证登录成功
    await expect(page).toHaveURL(/.*index\.html/);
    await expect(page.locator('text=欢迎')).toBeVisible();
  });

  test('登录失败 - 错误密码', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'WrongPassword');
    await page.click('button[type="submit"]');

    const message = page.locator('.message.error');
    await expect(message).toBeVisible();
  });
});

test.describe('订阅管理流程', () => {
  test.beforeEach(async ({ page }) => {
    // 注册并登录
    await page.goto('/register.html');
    await page.fill('#username', 'sub_test_user');
    await page.fill('#email', 'sub@example.com');
    await page.fill('#password', 'Password123');
    await page.fill('#confirmPassword', 'Password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*index\.html/);
  });

  test('添加订阅', async ({ page }) => {
    // 选择类别
    await page.selectOption('#categorySelect', 'technology');
    await page.click('button:has-text("订阅")');

    // 等待订阅成功消息
    await expect(page.locator('.message:has-text("订阅成功")')).toBeVisible({ timeout: 5000 });

    // 验证订阅出现在列表中
    await expect(page.locator('.subscription-item:has-text("科技")')).toBeVisible();
  });

  test('取消订阅', async ({ page }) => {
    // 先添加订阅
    await page.selectOption('#categorySelect', 'sports');
    await page.click('button:has-text("订阅")');
    await page.waitForTimeout(500);

    // 取消订阅
    await page.click('.subscription-item:has-text("体育") button');

    // 确认取消（使用简单的模拟）
    page.on('dialog', dialog => dialog.accept());

    // 验证取消成功
    await expect(page.locator('.subscription-item:has-text("体育")')).not.toBeVisible({ timeout: 5000 });
  });

  test('重复订阅同一类别应该失败', async ({ page }) => {
    await page.selectOption('#categorySelect', 'health');
    await page.click('button:has-text("订阅")');
    await page.waitForTimeout(500);

    // 再次尝试订阅
    await page.selectOption('#categorySelect', 'health');
    await page.click('button:has-text("订阅")');

    // 应该看到错误消息
    await expect(page.locator('.message.error:has-text("已订阅")')).toBeVisible();
  });
});

test.describe('新闻浏览流程', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register.html');
    await page.fill('#username', 'news_test_user');
    await page.fill('#email', 'news@example.com');
    await page.fill('#password', 'Password123');
    await page.fill('#confirmPassword', 'Password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*index\.html/);
  });

  test('有订阅时显示新闻', async ({ page }) => {
    // 添加订阅
    await page.selectOption('#categorySelect', 'technology');
    await page.click('button:has-text("订阅")');
    await page.waitForTimeout(1000);

    // 刷新页面以获取新闻
    await page.reload();

    // 验证新闻加载
    await expect(page.locator('.news-item').first()).toBeVisible({ timeout: 10000 });
  });

  test('无订阅时显示提示', async ({ page }) => {
    // 确保没有订阅
    await page.goto('/index.html');

    // 应该显示提示消息
    await expect(page.locator('text=请先订阅新闻类别')).toBeVisible({ timeout: 5000 });
  });
});
