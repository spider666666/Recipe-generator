# 🍳 AI智能菜谱生成器 - 猫咪厨师版

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)

🐱 基于Claude AI的智能菜谱推荐系统，根据现有食材智能生成个性化菜谱 🐱

[功能特性](#功能特性) • [技术栈](#技术栈) • [快速开始](#快速开始) • [项目结构](#项目结构) • [API文档](#api文档)

</div>

---

## 📖 项目简介

AI智能菜谱生成器是一个基于Claude AI的智能烹饪助手，采用可爱的**猫咪主题设计**，帮助用户根据现有食材快速生成个性化菜谱。系统支持多种菜系、口味偏好和难度等级，让做饭变得更简单、更有趣！

### ✨ 核心亮点

- 🤖 **AI智能推荐**：集成Claude Sonnet 4.5，根据食材智能生成菜谱
- 🐱 **猫咪主题UI**：可爱的猫咪图标和动画，提升用户体验
- 🎯 **个性化定制**：支持菜系、口味、难度、烹饪时间等多维度筛选
- 📝 **详细步骤**：提供完整的食材清单和分步烹饪指导
- ⭐ **收藏管理**：收藏喜欢的菜谱，随时查看历史记录
- 💾 **组合保存**：保存常用食材组合，快速生成菜谱
- 🎨 **精美动画**：流畅的页面过渡和交互动画

---

## 🎯 功能特性

### 1. 智能菜谱生成

- 根据现有食材智能匹配菜谱
- 支持中餐、西餐、日韩料理、东南亚菜等多种菜系
- 可选择辣、甜、咸、酸、清淡等口味
- 新手、家常、大厨三种难度等级（配有对应的厨师猫图标）
- 自定义烹饪时间和份数
- 实时显示食材匹配度

### 2. 食材管理

- 92+种常见食材库
- 智能食材搜索（支持模糊匹配）
- 食材分类管理（蔬菜、肉类、海鲜、主食、调味料）
- 常用食材组合保存与快速加载
- 自定义食材添加

### 3. 购物清单

- 一键添加菜谱所需食材
- 智能合并相同食材用量
- 购买状态标记
- 分类展示（按食材类别）
- 导出购物清单

### 4. 收藏与历史

- 收藏喜欢的菜谱
- 查看生成历史
- 菜谱详情查看
- 菜谱导出功能（TXT格式）
- 删除不需要的菜谱

### 5. 用户系统

- JWT身份认证
- 用户注册/登录
- Token自动刷新（24小时有效期）
- 安全的密码加密存储

### 6. 猫咪主题设计 🐱

- **厨师猫**：页面标题装饰，带有跳动动画
- **困惑猫**：空状态提示，摇摆动画
- **开心猫**：生成按钮图标，旋转动画
- **搅拌锅小猫**：加载状态动画
- **难度猫咪图标**：
  - 新手厨师猫 🐱
  - 普通厨师猫 🐱‍🍳
  - 专业厨师猫 👨‍🍳
- **猫爪装饰**：背景浮动装饰，增添趣味性
- **渐变配色**：温暖的橙粉色系（#ff8c69 → #ff6b9d）

---

## 🛠 技术栈

### 后端技术

- **框架**: Spring Boot 3.2.2
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT
- **API文档**: Swagger/OpenAPI 3.0
- **AI集成**: Claude API (Sonnet 4.5)
- **构建工具**: Maven

### 前端技术

- **框架**: Vue 3 (Composition API)
- **UI组件**: Element Plus
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **样式**: CSS3 动画 + 渐变

### 数据库设计

- 用户表 (user)
- 食材表 (ingredient)
- 菜谱表 (recipe)
- 菜谱-食材关联表 (recipe_ingredient)
- 菜谱步骤表 (recipe_step)
- 收藏表 (favorite)
- 历史记录表 (history)
- 购物清单表 (shopping_list)
- 食材组合表 (ingredient_combo)

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+
- Claude API Key

### 后端启动

1. **克隆项目**

```bash
git clone https://github.com/yourusername/recipe-generator.git
cd recipe-generator
```

2. **配置数据库**

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE recipe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化脚本
mysql -u root -p recipe_db < recipe-generator-backend/init.sql
```

3. **配置Claude API**

编辑 `recipe-generator-backend/src/main/resources/application.yml`：

```yaml
claude:
  api-key: your-claude-api-key-here
  api-url: https://api.anthropic.com/v1/messages
  model: claude-sonnet-4-5-20250929
```

4. **启动后端服务**

```bash
cd recipe-generator-backend
mvn clean package
java -jar target/recipe-generator-backend-1.0.0.jar
```

后端服务将在 `http://localhost:8080/api` 启动

### 前端启动

1. **安装依赖**

```bash
cd recipe-generator-frontend
npm install
```

2. **配置API地址**

Vite已配置代理，无需修改。如需修改，编辑 `vite.config.js`：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

3. **启动前端服务**

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 默认账号

首次使用需要注册账号，或使用以下测试账号：

- 用户名: `test`
- 密码: `123456`

---

## 📁 项目结构

```
recipe-generator/
├── recipe-generator-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/recipe/
│   │   │   │   ├── controller/       # 控制器层
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── RecipeController.java
│   │   │   │   │   ├── IngredientController.java
│   │   │   │   │   ├── FavoriteController.java
│   │   │   │   │   ├── HistoryController.java
│   │   │   │   │   ├── ShoppingListController.java
│   │   │   │   │   └── ComboController.java
│   │   │   │   ├── service/          # 服务层
│   │   │   │   │   ├── ClaudeService.java
│   │   │   │   │   ├── RecipeService.java
│   │   │   │   │   └── ...
│   │   │   │   ├── mapper/           # 数据访问层
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   ├── config/           # 配置类
│   │   │   │   └── security/         # 安全配置
│   │   │   └── resources/
│   │   │       ├── application.yml   # 配置文件
│   │   │       └── mapper/           # MyBatis映射文件
│   │   └── test/                     # 测试代码
│   ├── init.sql                      # 数据库初始化脚本
│   └── pom.xml                       # Maven配置
│
├── recipe-generator-frontend/         # 前端项目
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   │   ├── HomePage.vue         # 首页（食材选择）
│   │   │   ├── RecipesPage.vue      # 菜谱列表（历史记录）
│   │   │   ├── FavoritesPage.vue    # 收藏页面
│   │   │   ├── ShoppingPage.vue     # 购物清单
│   │   │   ├── LoginPage.vue        # 登录页面
│   │   │   └── RegisterPage.vue     # 注册页面
│   │   ├── components/               # 公共组件
│   │   │   └── IngredientGrid.vue   # 食材网格组件
│   │   ├── assets/                   # 静态资源
│   │   │   └── images/              # 猫咪图标
│   │   │       ├── 厨师猫.png
│   │   │       ├── 开心猫.png
│   │   │       ├── 困惑猫.png
│   │   │       ├── 伤心猫.png
│   │   │       ├── 猫爪.png
│   │   │       ├── 厨师小猫在搅拌锅.png
│   │   │       ├── 新手厨师猫.png
│   │   │       ├── 普通厨师猫.png
│   │   │       └── 专业厨师猫.png
│   │   ├── utils/                    # 工具函数
│   │   │   ├── api.js               # API接口
│   │   │   └── ingredientsData.js   # 食材数据
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── index.html                    # HTML模板
│   ├── vite.config.js               # Vite配置
│   └── package.json                  # NPM配置
│
└── README.md                          # 项目文档
```

---

## 📡 API文档

### 认证接口

#### 用户注册

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "password",
  "email": "user@example.com"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "user",
      "email": "user@example.com"
    }
  }
}
```

#### 用户登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

### 菜谱接口

#### 生成菜谱

```http
POST /api/recipes/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "ingredients": [
    {
      "name": "土豆",
      "quantity": "2个"
    },
    {
      "name": "猪肉",
      "quantity": "200g"
    }
  ],
  "cuisineType": "CHINESE",
  "flavorTypes": ["SPICY"],
  "difficultyLevel": "HOME_COOKING",
  "cookingTime": 30
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": [
    {
      "id": 1,
      "name": "土豆炖猪肉",
      "cuisineType": "CHINESE",
      "cookingTime": 30,
      "difficultyLevel": "HOME_COOKING",
      "servings": 2,
      "ingredients": [
        {
          "name": "土豆",
          "quantity": "2个"
        }
      ],
      "steps": [
        {
          "stepNumber": 1,
          "description": "土豆去皮切块",
          "duration": 5
        }
      ]
    }
  ]
}
```

#### 删除菜谱

```http
DELETE /api/recipes/{id}
Authorization: Bearer {token}
```

### 食材接口

#### 获取所有食材

```http
GET /api/ingredients
Authorization: Bearer {token}
```

#### 搜索食材

```http
GET /api/ingredients/search?name=土豆
Authorization: Bearer {token}
```

### 购物清单接口

#### 添加购物清单项

```http
POST /api/shopping-list
Authorization: Bearer {token}
Content-Type: application/json

{
  "ingredientId": 1,
  "quantity": "2个",
  "note": "买新鲜的"
}
```

#### 获取购物清单

```http
GET /api/shopping-list
Authorization: Bearer {token}
```

#### 更新购买状态

```http
PUT /api/shopping-list/{itemId}/purchase?isPurchased=true
Authorization: Bearer {token}
```

#### 清空购物清单

```http
DELETE /api/shopping-list/clear
Authorization: Bearer {token}
```

### 收藏接口

#### 添加收藏

```http
POST /api/favorites/{recipeId}
Authorization: Bearer {token}
```

#### 获取收藏列表

```http
GET /api/favorites
Authorization: Bearer {token}
```

#### 取消收藏

```http
DELETE /api/favorites/{recipeId}
Authorization: Bearer {token}
```

### 历史记录接口

#### 获取历史记录

```http
GET /api/history
Authorization: Bearer {token}
```

#### 添加历史记录

```http
POST /api/history/{recipeId}
Authorization: Bearer {token}
```

### 食材组合接口

#### 保存组合

```http
POST /api/combos
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "常用组合",
  "ingredients": "[{\"name\":\"土豆\",\"amount\":\"2个\"}]"
}
```

#### 获取组合列表

```http
GET /api/combos
Authorization: Bearer {token}
```

#### 删除组合

```http
DELETE /api/combos/{id}
Authorization: Bearer {token}
```

完整API文档请访问：`http://localhost:8080/api/swagger-ui.html`

---

## 🎨 界面预览

### 首页 - 食材选择 🏠

- **左侧区域**：食材分类选择（蔬菜、肉类、海鲜、主食）
  - 搜索功能
  - 自定义食材添加
  - 常用组合快速加载
- **右侧区域**：已选食材和筛选条件
  - 菜系选择（中餐、西餐、日韩、东南亚）
  - 口味选择（辣、甜、咸、酸、清淡）
  - 烹饪时间（15分钟、30分钟、1小时）
  - 难度等级（新手、家常、大厨）
- **生成按钮**：带有开心猫图标，加载时显示搅拌锅小猫旋转动画

### 菜谱列表 - 历史记录 📖

- 网格布局展示所有生成的菜谱
- 每个卡片显示：
  - 菜谱名称
  - 菜系、时间、难度标签（带厨师猫图标）
  - 食材预览（前5种，显示是否具备）
  - 操作按钮：查看详情、删除
- 详情弹窗：
  - 完整食材清单（表格形式）
  - 分步烹饪指导（步骤组件）
  - 评分和评论（开发中）
  - 导出功能

### 收藏夹 ⭐

- 与菜谱列表类似的布局
- 显示收藏时间
- 取消收藏按钮
- 查看详情功能

### 购物清单 🛒

- 统计信息：总计、已购买、待购买
- 按食材分类展示
- 购买状态勾选
- 备注编辑
- 导出和清空功能

### 猫咪元素 🐱

- **动画效果**：
  - 标题猫咪跳动（bounce）
  - 空状态猫咪摇摆（wiggle）
  - 背景猫爪浮动（float）
  - 加载猫咪旋转（spin）
  - 卡片悬停上浮
- **配色方案**：
  - 主色：橙粉渐变（#ff8c69 → #ff6b9d）
  - 背景：温暖渐变（#fff8f0 → #ffe8f0 → #fff5e8）
  - 强调色：成功绿、警告橙、危险红

---

## 🔧 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key-change-in-production  # JWT密钥（生产环境请修改）
  expiration: 86400000                          # 访问令牌有效期（24小时）
  refresh-expiration: 604800000                 # 刷新令牌有效期（7天）
```

### Claude API配置

```yaml
claude:
  api-key: ${CLAUDE_API_KEY}                    # Claude API密钥（环境变量）
  api-url: https://api.anthropic.com/v1/messages
  model: claude-sonnet-4-5-20250929             # 使用的模型
  max-tokens: 4096                              # 最大token数
  temperature: 0.7                              # 温度参数（0-1）
  timeout: 60000                                # 超时时间（毫秒）
```

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/recipe_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

  # MyBatis-Plus配置
  mybatis-plus:
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### CORS配置

```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:5173
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

---

## 🐛 常见问题

### 1. 前端无法连接后端

**问题**：前端请求返回 CORS 错误或连接被拒绝

**解决方案**：

- 检查后端是否正常启动（`http://localhost:8080/api`）
- 确认 CORS 配置包含前端地址
- 检查防火墙设置

### 2. Claude API 调用失败

**问题**：生成菜谱时返回错误

**解决方案**：

- 确认 API Key 正确配置
- 检查网络连接（可能需要代理）
- 查看后端日志获取详细错误信息
- 确认 API 配额未超限

### 3. 数据库连接失败

**问题**：后端启动时报数据库连接错误

**解决方案**：

- 确认 MySQL 服务已启动
- 检查数据库名称、用户名、密码是否正确
- 确认数据库已创建并导入初始化脚本
- 检查 MySQL 版本（需要 8.0+）

### 4. 图片无法显示

**问题**：猫咪图标不显示

**解决方案**：

- 确认图片文件存在于 `src/assets/images/` 目录
- 检查图片文件名是否正确（中文文件名）
- 清除浏览器缓存并重新加载
- 检查 Vite 配置的静态资源处理

### 5. Token 过期

**问题**：操作时提示需要重新登录

**解决方案**：

- Token 默认有效期为 24 小时
- 重新登录获取新 Token
- 可在配置文件中调整有效期

---

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 贡献流程

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- **后端**：遵循阿里巴巴Java开发手册
- **前端**：使用 ESLint + Prettier
- **提交信息**：使用语义化提交信息
  - `feat`: 新功能
  - `fix`: 修复bug
  - `docs`: 文档更新
  - `style`: 代码格式调整
  - `refactor`: 重构
  - `test`: 测试相关
  - `chore`: 构建/工具相关

---

## 📝 开发计划

### 已完成 ✅

- [X]  基础菜谱生成功能
- [X]  用户认证系统
- [X]  食材管理
- [X]  收藏功能
- [X]  历史记录
- [X]  购物清单
- [X]  食材组合保存
- [X]  猫咪主题UI设计
- [X]  难度等级猫咪图标
- [X]  加载动画优化

### 进行中 🚧

- [ ]  菜谱评分和评论功能
- [ ]  营养成分分析

### 计划中 📋

- [ ]  支持更多菜系和食材
- [ ]  实现菜谱分享功能
- [ ]  支持图片上传和识别
- [ ]  移动端适配
- [ ]  多语言支持（中文/英文）
- [ ]  菜谱推荐算法优化
- [ ]  用户偏好学习
- [ ]  社交功能（关注、点赞）
- [ ]  菜谱视频教程
- [ ]  语音助手集成

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 作者

- **开发者** - *Initial work* - 基于 Claude AI 辅助开发

---

## 🙏 致谢

- [Claude AI](https://www.anthropic.com/) - 提供强大的AI能力
- [Spring Boot](https://spring.io/projects/spring-boot) - 优秀的Java框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Element Plus](https://element-plus.org/) - 优秀的Vue 3组件库
- [MyBatis-Plus](https://baomidou.com/) - 强大的MyBatis增强工具
- [Vite](https://vitejs.dev/) - 下一代前端构建工具

### 特别感谢

感谢所有为开源社区做出贡献的开发者们！🎉

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](https://github.com/yourusername/recipe-generator/issues)
- 发送邮件至：your.email@example.com
- 加入讨论群：[Discord/QQ群]

---

## 📊 项目统计

- **代码行数**：约 10,000+ 行
- **支持食材**：92+ 种
- **支持菜系**：4 种（中餐、西餐、日韩、东南亚）
- **难度等级**：3 种（新手、家常、大厨）
- **猫咪图标**：9 个

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star！⭐**

Made with ❤️ and 🐱 by AI-Powered Development

**让做饭变得简单又有趣！喵~ 🐱**

</div>
