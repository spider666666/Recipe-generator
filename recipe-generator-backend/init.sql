-- 创建数据库并指定字符集（避免中文乱码）
CREATE DATABASE IF NOT EXISTS recipe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE recipe_db;

-- 1. 创建食材表：存储食材基础信息，适配食谱生成的食材匹配、营养计算
DROP TABLE IF EXISTS ingredient;
CREATE TABLE ingredient (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '食材唯一ID',
                            name VARCHAR(50) NOT NULL COMMENT '食材名称（如西红柿、鸡肉）',
                            category VARCHAR(20) NOT NULL COMMENT '食材分类（VEGETABLE/MEAT/SEAFOOD/STAPLE/SEASONING）',
                            common_unit VARCHAR(10) NOT NULL COMMENT '常用单位（个/g/ml/根/瓣）',
                            calories INT NOT NULL DEFAULT 0 COMMENT '每单位食材热量（大卡）',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            UNIQUE KEY uk_name (name) COMMENT '食材名称唯一，避免重复'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食材基础信息表';

-- 2. 创建用户表：存储用户账号信息，适配登录、个性化食谱收藏
DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一ID',
                      username VARCHAR(50) NOT NULL COMMENT '登录用户名',
                      password VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密，如示例123456）',
                      email VARCHAR(100) DEFAULT '' COMMENT '用户邮箱（用于找回密码）',
                      nickname VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
                      enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账号是否启用（1-启用，0-禁用）',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息更新时间',
                      UNIQUE KEY uk_username (username) COMMENT '用户名唯一',
                      UNIQUE KEY uk_email (email) COMMENT '邮箱唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号信息表';

-- 插入测试食材数据
INSERT INTO ingredient (name, category, common_unit, calories) VALUES
-- 蔬菜类
('西红柿', 'VEGETABLE', '个', 20),
('土豆', 'VEGETABLE', '个', 77),
('白菜', 'VEGETABLE', 'g', 17),
('黄瓜', 'VEGETABLE', '根', 15),
('胡萝卜', 'VEGETABLE', '根', 41),
('洋葱', 'VEGETABLE', '个', 40),
('青椒', 'VEGETABLE', '个', 22),
('茄子', 'VEGETABLE', '个', 25),

-- 肉类
('鸡肉', 'MEAT', 'g', 167),
('猪肉', 'MEAT', 'g', 242),
('牛肉', 'MEAT', 'g', 250),
('羊肉', 'MEAT', 'g', 203),

-- 海鲜类
('鱼', 'SEAFOOD', 'g', 100),
('虾', 'SEAFOOD', 'g', 85),
('螃蟹', 'SEAFOOD', 'g', 95),

-- 主食类
('米饭', 'STAPLE', 'g', 116),
('面条', 'STAPLE', 'g', 137),
('饺子', 'STAPLE', '个', 40),
('鸡蛋', 'STAPLE', '个', 147),

-- 调味料
('盐', 'SEASONING', 'g', 0),
('酱油', 'SEASONING', 'ml', 63),
('醋', 'SEASONING', 'ml', 18),
('糖', 'SEASONING', 'g', 387),
('白糖', 'SEASONING', 'g', 387),
('料酒', 'SEASONING', 'ml', 0),
('葱', 'SEASONING', '根', 30),
('姜', 'SEASONING', 'g', 41),
('蒜', 'SEASONING', '瓣', 128),
('大蒜', 'SEASONING', '瓣', 128),
('食用油', 'SEASONING', 'ml', 884),
('淀粉', 'SEASONING', 'g', 346),
('干辣椒', 'SEASONING', '个', 40),
('花椒', 'SEASONING', 'g', 258),
('八角', 'SEASONING', '个', 20),
('香叶', 'SEASONING', '片', 10);

-- 3. 创建食谱表：存储生成的食谱基本信息
DROP TABLE IF EXISTS recipe;
CREATE TABLE recipe (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '食谱唯一ID',
                        name VARCHAR(100) NOT NULL COMMENT '菜品名称',
                        cuisine_type VARCHAR(20) NOT NULL COMMENT '菜系类型（CHINESE/WESTERN/JAPANESE_KOREAN/SOUTHEAST_ASIAN）',
                        flavor_types VARCHAR(100) COMMENT '口味类型（多个用逗号分隔：SPICY,SWEET,SALTY,SOUR,MILD）',
                        cooking_time INT NOT NULL COMMENT '烹饪时间（分钟）',
                        difficulty_level VARCHAR(20) NOT NULL COMMENT '难度等级（BEGINNER/HOME_COOKING/CHEF）',
                        description TEXT COMMENT '菜品描述',
                        image_url VARCHAR(255) COMMENT '菜品图片URL',
                        servings INT DEFAULT 2 COMMENT '份数',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱基本信息表';

-- 4. 创建食谱-食材关联表：存储食谱所需的食材及用量
DROP TABLE IF EXISTS recipe_ingredient;
CREATE TABLE recipe_ingredient (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联唯一ID',
                                   recipe_id BIGINT NOT NULL COMMENT '食谱ID',
                                   ingredient_id BIGINT NOT NULL COMMENT '食材ID',
                                   quantity VARCHAR(20) NOT NULL COMMENT '用量（如2个、100g）',
                                   is_required TINYINT(1) DEFAULT 1 COMMENT '是否必需（1-必需，0-可选）',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   KEY idx_recipe_id (recipe_id) COMMENT '食谱ID索引',
                                   KEY idx_ingredient_id (ingredient_id) COMMENT '食材ID索引',
                                   FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE,
                                   FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱-食材关联表';

-- 5. 创建食谱步骤表：存储烹饪步骤详情
DROP TABLE IF EXISTS recipe_step;
CREATE TABLE recipe_step (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '步骤唯一ID',
                             recipe_id BIGINT NOT NULL COMMENT '食谱ID',
                             step_number INT NOT NULL COMMENT '步骤序号',
                             description TEXT NOT NULL COMMENT '步骤描述',
                             image_url VARCHAR(255) COMMENT '步骤图片URL',
                             duration INT COMMENT '步骤耗时（分钟）',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             KEY idx_recipe_id (recipe_id) COMMENT '食谱ID索引',
                             FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱步骤表';

-- 6. 创建收藏表：存储用户收藏的食谱
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏唯一ID',
                          user_id BIGINT NOT NULL COMMENT '用户ID',
                          recipe_id BIGINT NOT NULL COMMENT '食谱ID',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                          KEY idx_user_id (user_id) COMMENT '用户ID索引',
                          KEY idx_recipe_id (recipe_id) COMMENT '食谱ID索引',
                          UNIQUE KEY uk_user_recipe (user_id, recipe_id) COMMENT '用户-食谱唯一',
                          FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                          FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 7. 创建历史记录表：存储用户生成食谱的历史
DROP TABLE IF EXISTS history;
CREATE TABLE history (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录唯一ID',
                         user_id BIGINT NOT NULL COMMENT '用户ID',
                         recipe_id BIGINT NOT NULL COMMENT '食谱ID',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
                         KEY idx_user_id (user_id) COMMENT '用户ID索引',
                         KEY idx_recipe_id (recipe_id) COMMENT '食谱ID索引',
                         FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                         FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生成历史记录表';

-- 8. 创建购物清单表：存储用户的购物清单
DROP TABLE IF EXISTS shopping_list;
CREATE TABLE shopping_list (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '���单项唯一ID',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               ingredient_id BIGINT NOT NULL COMMENT '食材ID',
                               quantity VARCHAR(20) NOT NULL COMMENT '购买数量',
                               is_purchased TINYINT(1) DEFAULT 0 COMMENT '是否已购买（1-已购买，0-未购买）',
                               note VARCHAR(200) COMMENT '备注',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               KEY idx_user_id (user_id) COMMENT '用户ID索引',
                               KEY idx_ingredient_id (ingredient_id) COMMENT '食材ID索引',
                               FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                               FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物清单表';

-- 插入测试用户（密码均为123456，BCrypt加密）
INSERT INTO user (username, password, email, nickname, enabled) VALUES
                                                                    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@recipe.com', '管理员', true),
                                                                    ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@recipe.com', '测试用户', true);

-- 9. 创建食材组合表：存储用户保存的常用食材组合
DROP TABLE IF EXISTS ingredient_combo;
CREATE TABLE ingredient_combo (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '组合唯一ID',
                                   user_id BIGINT NOT NULL COMMENT '用户ID',
                                   name VARCHAR(50) NOT NULL COMMENT '组合名称',
                                   ingredients TEXT NOT NULL COMMENT '食材列表（JSON格式）',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   KEY idx_user_id (user_id) COMMENT '用户ID索引',
                                   FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食材组合表';

-- 提交事务
COMMIT;