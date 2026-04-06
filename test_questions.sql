-- 测试面试题 SQL 脚本
-- 生成时间: 2026-04-05

-- 插入分类
INSERT INTO categories (name, description) VALUES
('功能测试', '功能测试相关面试题，包括需求分析、测试用例设计等'),
('接口测试', 'API测试、RESTful接口测试相关面试题'),
('自动化测试', 'Selenium、Appium等自动化测试框架相关面试题'),
('性能测试', 'JMeter、LoadRunner等性能测试工具相关面试题'),
('数据库测试', 'SQL查询、数据验证等数据库相关面试题'),
('Linux命令', 'Linux系统操作相关面试题'),
('测试基础', '软件测试基本概念、方法论相关面试题')
ON CONFLICT (name) DO NOTHING;

-- 插入题目: 功能测试
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    '什么是测试用例？测试用例包含哪些要素？',
    '请描述测试用例的定义，并列出测试用例的主要组成部分。',
    '测试用例是对特定软件功能的测试场景描述，包含以下要素：
1. 用例ID：唯一标识
2. 用例标题：简洁描述测试目标
3. 测试目的：说明要验证的功能点
4. 前置条件：执行前需要满足的条件
5. 测试步骤：详细的操作步骤
6. 预期结果：期望的输出或行为
7. 优先级：P0/P1/P2/P3
8. 测试数据：使用的输入数据',
    (SELECT id FROM categories WHERE name = '功能测试'),
    'EASY',
    NOW(),
    NOW()
),
(
    '等价类划分和边界值分析的区别与联系',
    '请解释等价类划分和边界值分析两种测试方法，并说明它们如何结合使用。',
    '等价类划分：将输入域划分为有效等价类和无效等价类，在每类中选取代表性数据进行测试。

边界值分析：针对边界条件进行测试，通常选取边界值、边界值左右两侧的值进行测试。

联系：
1. 边界值分析是等价类划分的补充
2. 通常在有效等价类的边界处重点测试
3. 结合使用可以减少冗余，提高测试效率

例如：输入范围 1-100
- 等价类：有效[1-100]，无效(<1, >100)
- 边界值：0, 1, 2, 99, 100, 101',
    (SELECT id FROM categories WHERE name = '功能测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '如何设计测试用例？有哪些设计方法？',
    '请列举常用的测试用例设计方法，并说明每种方法的适用场景。',
    '常用测试用例设计方法：

1. 等价类划分法
   - 适用：输入条件较多的情况
   - 优点：减少冗余，提高效率

2. 边界值分析法
   - 适用：输入范围有明确边界的情况
   - 优点：发现边界相关的bug

3. 判定表法
   - 适用：多个输入条件组合，有复杂业务逻辑
   - 优点：覆盖所有条件组合

4. 因果图法
   - 适用：输入之间存在因果关系
   - 优点：系统化处理复杂条件

5. 正交试验法
   - 适用：多因素多水平测试
   - 优点：用最少的用例覆盖最多的组合

6. 场景法
   - 适用：业务流程测试
   - 优点：贴近用户实际使用场景

7. 错误推测法
   - 适用：基于经验发现潜在问题
   - 优点：针对性强',
    (SELECT id FROM categories WHERE name = '功能测试'),
    'MEDIUM',
    NOW(),
    NOW()
);

-- 插入题目: 接口测试
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    '接口测试的流程是什么？',
    '请描述完整的接口测试流程，从需求分析到测试报告输出。',
    '接口测试流程：

1. 需求分析
   - 理解接口文档
   - 明确接口功能、输入输出
   - 确认业务规则

2. 接口文档评审
   - 评审接口设计的合理性
   - 确认参数定义、返回值格式

3. 设计测试用例
   - 正常场景用例
   - 异常场景用例（参数错误、空值、边界值）
   - 业务场景用例

4. 准备测试数据
   - 创建必要的预置数据
   - 准备正向/逆向测试数据

5. 执行接口测试
   - 使用工具（Postman、Jmeter）或代码（Python/Java）
   - 验证响应状态码
   - 验证响应数据结构和内容
   - 验证数据库变更

6. 缺陷管理
   - 记录bug，跟踪修复

7. 测试报告
   - 总结测试结果
   - 分析覆盖率',
    (SELECT id FROM categories WHERE name = '接口测试'),
    'EASY',
    NOW(),
    NOW()
),
(
    'HTTP状态码有哪些？分别代表什么含义？',
    '请列举常见的HTTP状态码及其含义，重点说明2xx、4xx、5xx系列。',
    '常见HTTP状态码：

1xx - 信息响应
- 100 Continue
- 101 Switching Protocols

2xx - 成功
- 200 OK：请求成功
- 201 Created：资源创建成功
- 204 No Content：成功但无返回内容

3xx - 重定向
- 301 Moved Permanently：永久重定向
- 302 Found：临时重定向
- 304 Not Modified：资源未修改

4xx - 客户端错误
- 400 Bad Request：请求语法错误
- 401 Unauthorized：未认证
- 403 Forbidden：权限不足
- 404 Not Found：资源不存在
- 405 Method Not Allowed：方法不允许
- 422 Unprocessable Entity：参数校验失败
- 429 Too Many Requests：请求过于频繁

5xx - 服务器错误
- 500 Internal Server Error：服务器内部错误
- 502 Bad Gateway：网关错误
- 503 Service Unavailable：服务不可用
- 504 Gateway Timeout：网关超时',
    (SELECT id FROM categories WHERE name = '接口测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    'GET和POST请求的区别是什么？',
    '请从多个角度说明GET和POST请求的区别。',
    'GET和POST的区别：

1. 参数位置
   - GET：参数在URL查询字符串中
   - POST：参数在请求体中

2. 安全性
   - GET：参数暴露在URL中，不适合传输敏感数据
   - POST：参数在请求体中，相对更安全

3. 数据长度限制
   - GET：受URL长度限制（约2048字符）
   - POST：无限制

4. 缓存
   - GET：可以被缓存
   - POST：不会被缓存

5. 幂等性
   - GET：幂等（多次请求结果相同）
   - POST：非幂等

6. 书签
   - GET：可以被收藏为书签
   - POST：不可以

7. 数据类型
   - GET：只支持ASCII字符
   - POST：无限制，支持二进制数据

使用场景：
- GET：查询、获取数据
- POST：提交表单、创建资源',
    (SELECT id FROM categories WHERE name = '接口测试'),
    'EASY',
    NOW(),
    NOW()
),
(
    '接口测试中如何处理登录接口的Token？',
    '在测试需要认证的接口时，如何获取和管理Token？',
    '处理Token的常用方法：

1. 先调用登录接口获取Token
   - 调用登录API，获取access_token
   - 将Token保存到变量中
   - 在后续请求的Header中添加：
     Authorization: Bearer {token}

2. 使用全局变量/环境变量
   - Postman：设置全局变量 {{token}}
   - JMeter：使用CSV Data Set Config

3. 在Pre-request Script中获取
   // 登录后设置全局变量
   pm.globals.set("token", response.json().access_token);

4. 使用Token的注意事项
   - 检查Token是否过期
   - 过期后需要重新登录
   - 区分access_token和refresh_token

5. 常见认证方式
   - Bearer Token (JWT)
   - Basic Auth
   - OAuth 2.0',
    (SELECT id FROM categories WHERE name = '接口测试'),
    'MEDIUM',
    NOW(),
    NOW()
);

-- 插入题目: 自动化测试
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    'Selenium中driver.findElement()和driver.findElements()的区别？',
    '请解释findElement和findElements方法的区别和使用场景。',
    '区别：

1. 返回值类型
   - findElement()：返回单个WebElement
   - findElements()：返回List<WebElement>

2. 找不到元素时
   - findElement()：抛出NoSuchElementException
   - findElements()：返回空列表，不抛异常

3. 使用场景
   - findElement()：确定元素唯一或只需获取第一个
   - findElements()：需要操作多个相同类型元素（如列表项）

代码示例：
// findElement - 找一个元素
WebElement button = driver.findElement(By.id("submit"));

// findElements - 找多个元素
List<WebElement> items = driver.findElements(By.className("item"));
for (WebElement item : items) {
    // 遍历操作每个元素
}

定位策略优先级：
1. ID（最稳定）
2. Name
3. CSS Selector（推荐）
4. XPath（最灵活但最慢）',
    (SELECT id FROM categories WHERE name = '自动化测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '如何处理Selenium中的iframe？',
    '当元素位于iframe内时，如何进行操作？',
    '处理iframe的方法：

1. 切换到iframe
   // 通过WebElement切换
   driver.switchTo().frame(driver.findElement(By.id("frameId")));

   // 通过name或id切换
   driver.switchTo().frame("frameName");
   driver.switchTo().frame("frameId");

   // 通过索引切换
   driver.switchTo().frame(0);

2. 操作iframe内元素
   切换后直接定位操作即可

3. 切出iframe
   // 回到主文档
   driver.switchTo().defaultContent();

   // 回到上一级iframe
   driver.switchTo().parentFrame();

注意事项：
- 嵌套iframe需要多次切换
- 切换后记得切回主文档
- 隐式等待对frame切换无效',
    (SELECT id FROM categories WHERE name = '自动化测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    'Selenium的隐式等待和显式等待有什么区别？',
    '请解释两种等待方式的原理和使用场景。',
    '隐式等待（Implicit Wait）：
- 设置一次，对所有findElement生效
- 适用于元素加载不确定的情况
- 整个WebDriver生命周期有效
driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

显式等待（Explicit Wait）：
- 针对特定元素设置
- 可以自定义等待条件
- 更灵活，推荐使用
WebDriverWait wait = new WebDriverWait(driver, 10);
wait.until(ExpectedConditions.elementToBeClickable(By.id("btn")));

常用等待条件：
- elementToBeClickable
- presenceOfElementLocated
- visibilityOfElementLocated
- textToBePresentInElement

sleep（强制等待）：
- 固定等待时间
- 不推荐使用，影响效率

建议：使用显式等待为主，更精确控制等待时间',
    (SELECT id FROM categories WHERE name = '自动化测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '自动化测试用例如何设计？有哪些分层？',
    '请说明自动化测试的分层架构和每层的职责。',
    '经典三层架构：

1. Object Repository层（对象库层）
   - 页面元素的定位表达式
   - Page Factory模式
   - 页面元素封装

2. Business Layer层（业务逻辑层）
   - 页面操作方法
   - 业务流程封装
   - 业务逻辑处理

3. Test Layer层（测试用例层）
   - 测试数据管理
   - 测试步骤编排
   - 断言和报告

示例结构：
src/
├── pageobjects/          # 页面对象
│   ├── LoginPage.java    # 登录页面对象
│   └── HomePage.java     # 首页对象
├── actions/             # 业务操作
│   └── UserActions.java # 用户相关操作
├── testdata/            # 测试数据
│   └── login_data.xlsx
├── tests/               # 测试用例
│   └── LoginTest.java
└── utils/               # 工具类

设计原则：
- POM（Page Object Model）模式
- DRY（Don''t Repeat Yourself）
- 单一职责原则',
    (SELECT id FROM categories WHERE name = '自动化测试'),
    'HARD',
    NOW(),
    NOW()
);

-- 插入题目: 性能测试
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    '性能测试的指标有哪些？',
    '请列举常见的性能测试指标及其含义。',
    '常见性能测试指标：

1. 响应时间（Response Time）
   - 指从发起请求到收到响应的时间
   - 包括：网络时间 + 服务器处理时间 + 前端渲染时间
   - 行业标准：2/5/10秒原则

2. 吞吐量（TPS/QPS）
   - TPS：Transactions Per Second，每秒事务数
   - QPS：Queries Per Second，每秒查询数
   - 衡量系统处理能力

3. 并发用户数
   - 同时发起请求的用户数量
   - 区分在线用户和并发用户

4. 资源利用率
   - CPU使用率
   - 内存使用率
   - 磁盘I/O
   - 网络带宽

5. 错误率
   - 请求失败的比例
   - 通常要求 < 1%

6. 思考时间（Think Time）
   - 用户操作间隔时间

7. 性能计数器
   - Java：GC次数、线程数
   - 数据库：连接池、慢查询',
    (SELECT id FROM categories WHERE name = '性能测试'),
    'EASY',
    NOW(),
    NOW()
),
(
    'JMeter中如何实现参数化？',
    '请说明JMeter中实现参数化的几种方法。',
    'JMeter参数化方法：

1. CSV Data Set Config（最常用）
   - 准备CSV文件
   - 添加CSV Data Set Config元件
   - 配置：文件名、变量名、分隔符等
   - 使用：${变量名}

2. 用户定义的变量
   - 添加 User Defined Variables 元件
   - 定义键值对
   - 全局变量适用

3. 函数助手
   - __Random：生成随机数
   - __time：当前时间戳
   - __CSVRead：读取CSV
   - ${__Random(1,100,)}

4. 正则表达式提取
   - 从上一个请求的响应中提取数据
   - 使用：${变量名}

5. JSON Extractor
   - 从JSON响应中提取值
   - $.data.id

6. BeanShell PreProcessor
   - 编写代码生成参数',
    (SELECT id FROM categories WHERE name = '性能测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '性能测试、负载测试、压力测试的区别？',
    '请解释三种测试类型的定义和区别。',
    '区别：

1. 性能测试（Performance Testing）
   - 定义：在预期负载下验证系统性能指标
   - 目标：验证系统是否达到预设性能目标
   - 负载：正常预期负载

2. 负载测试（Load Testing）
   - 定义：逐步增加负载，测试系统性能变化
   - 目标：找到系统性能拐点
   - 负载：从轻到重逐步递增
   - 关注：TPS响应时间曲线

3. 压力测试（Stress Testing）
   - 定义：在极限负载或异常条件下测试
   - 目标：测试系统的稳定性和容错能力
   - 负载：超过正常负载
   - 关注：系统在极限下的表现

4. 稳定性测试（Endurance Testing）
   - 长时间运行，观察系统稳定性
   - 通常使用正常负载的80%

总结：
- 性能测试：验证指标
- 负载测试：找到拐点
- 压力测试：测试极限',
    (SELECT id FROM categories WHERE name = '性能测试'),
    'MEDIUM',
    NOW(),
    NOW()
);

-- 插入题目: 数据库测试
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    '什么是关联查询？有哪些类型？',
    '请解释SQL中关联查询的概念和常用类型。',
    '关联查询（JOIN）：将多张表基于关联条件组合起来的查询。

类型：

1. INNER JOIN（内连接）
   SELECT * FROM A INNER JOIN B ON A.id = B.a_id;
   - 只返回两表匹配的记录

2. LEFT JOIN（左连接）
   SELECT * FROM A LEFT JOIN B ON A.id = B.a_id;
   - 返回A表所有记录，B表无匹配则为NULL

3. RIGHT JOIN（右连接）
   SELECT * FROM A RIGHT JOIN B ON A.id = B.a_id;
   - 返回B表所有记录，A表无匹配则为NULL

4. FULL OUTER JOIN（全外连接）
   SELECT * FROM A FULL JOIN B ON A.id = B.a_id;
   - 返回两表所有记录，无匹配则为NULL

5. CROSS JOIN（交叉连接）
   SELECT * FROM A CROSS JOIN B;
   - 返回笛卡尔积

6. 自连接
   SELECT * FROM A a1, A a2 WHERE a1.manager_id = a2.id;
   - 表自身连接',
    (SELECT id FROM categories WHERE name = '数据库测试'),
    'EASY',
    NOW(),
    NOW()
),
(
    '数据库索引的原理？有哪些类型？',
    '请解释数据库索引的工作原理和常见类型。',
    '索引原理：
- 类似书的目录，加快数据检索
- 使用数据结构（B-Tree、Hash等）存储索引
- 减少全表扫描，提高查询效率

索引类型：

1. 主键索引（Primary Key）
   - 值唯一，不允许NULL
   - 一个表只有一个主键

2. 唯一索引（Unique Index）
   - 值唯一，可以有多个
   - 允许NULL

3. 普通索引（Index）
   - 无唯一性限制
   - 最常见

4. 组合索引（Composite Index）
   - 多个列组成的索引
   - 遵循最左前缀原则

5. 全文索引（Full-text Index）
   - 用于文本搜索
   - MySQL InnoDB支持

索引原则：
- WHERE条件列可建索引
- 区分度高的列优先
- 避免在索引列上使用函数
- 避免过多索引（影响写入性能）',
    (SELECT id FROM categories WHERE name = '数据库测试'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '数据库事务的ACID特性？',
    '请解释数据库事务的ACID特性及其含义。',
    'ACID特性：

1. Atomicity（原子性）
   - 事务是最小执行单位
   - 要么全部成功，要么全部失败回滚
   - 使用undo log实现

2. Consistency（一致性）
   - 事务执行前后，数据库状态保持一致
   - 约束、触发器等保证数据有效性
   - 数据库从一个一致状态变到另一个一致状态

3. Isolation（隔离性）
   - 并发事务之间相互隔离
   - 通过锁机制实现
   - 隔离级别：读未提交、读已提交、可重复读、串行化

4. Durability（持久性）
   - 事务提交后，对数据库的修改永久保存
   - 使用redo log实现
   - 即使系统崩溃也能恢复

事务隔离级别：
- READ UNCOMMITTED：最低，可能脏读
- READ COMMITTED：防止脏读
- REPEATABLE READ（MySQL默认）：防止脏读、不可重复读
- SERIALIZABLE：最高，完全串行执行',
    (SELECT id FROM categories WHERE name = '数据库测试'),
    'MEDIUM',
    NOW(),
    NOW()
);

-- 插入题目: Linux命令
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    'Linux中如何查看进程和杀死进程？',
    '请说明查看进程和结束进程的相关命令。',
    '查看进程：

1. ps命令
   ps aux                  # 查看所有进程
   ps -ef                  # 查看完整格式进程
   ps aux | grep nginx     # 查找特定进程

2. top命令
   top                     # 动态查看进程
   top -u user             # 查看特定用户进程
   - 按P按CPU排序，按M按内存排序

3. pgrep命令
   pgrep -f nginx          # 按进程名查找PID

结束进程：

1. kill命令
   kill PID                # 正常终止
   kill -9 PID             # 强制杀死
   kill -15 PID            # 优雅终止（默认）

2. killall命令
   killall nginx           # 按进程名杀死

3. pkill命令
   pkill -f nginx          # 按进程名杀死

常用信号：
- SIGTERM(15)：请求终止，优雅退出
- SIGKILL(9)：强制杀死
- SIGHUP(1)：重载配置',
    (SELECT id FROM categories WHERE name = 'Linux命令'),
    'EASY',
    NOW(),
    NOW()
),
(
    '如何查看日志文件？有哪些常用命令？',
    '请说明Linux中查看日志文件的常用命令及用法。',
    '常用日志查看命令：

1. cat命令
   cat access.log                    # 查看全部内容
   cat -n access.log                # 显示行号

2. tail命令（最常用）
   tail -f access.log               # 实时追踪
   tail -100 access.log             # 查看最后100行
   tail -n 50 access.log            # 同上

3. head命令
   head -20 access.log              # 查看前20行

4. grep命令
   grep "ERROR" access.log           # 搜索关键词
   grep -i "error" access.log        # 忽略大小写
   grep -n "ERROR" access.log        # 显示行号
   grep -C 5 "ERROR" access.log      # 显示前后5行

5. sed命令
   sed -n '100,200p' access.log     # 查看100-200行
   sed -i '/ERROR/d' access.log      # 删除包含ERROR的行

6. less命令
   less access.log                   # 分页查看
   - 按/搜索，按q退出

7. wc命令
   wc -l access.log                  # 统计行数',
    (SELECT id FROM categories WHERE name = 'Linux命令'),
    'EASY',
    NOW(),
    NOW()
),
(
    'Linux中如何排查CPU和内存占用高的进程？',
    '请说明定位和排查性能问题的步骤。',
    '排查步骤：

1. 查看CPU占用
   top                     # 动态查看
   top -c                  # 显示完整命令
   ps aux --sort=-%cpu    # 按CPU排序

2. 查看内存占用
   top -o %MEM            # 按内存排序
   ps aux --sort=-%mem    # 按内存排序
   free -h                # 查看内存使用情况

3. 查看IO情况
   iotop                  # 查看IO占用
   iostat -x 1            # 每秒报告IO统计

4. 查看进程详情
   pidstat -p PID         # 查看进程资源使用
   cat /proc/PID/status   # 查看进程详细状态

5. 查看进程树
   pstree -p PID          # 查看进程树

6. 追踪进程调用
   strace -p PID          # 追踪系统调用
   top -H -p PID          # 查看线程

常见问题处理：
- CPU高：检查死循环、频繁GC
- 内存高：检查内存泄漏、大数据加载',
    (SELECT id FROM categories WHERE name = 'Linux命令'),
    'MEDIUM',
    NOW(),
    NOW()
);

-- 插入题目: 测试基础
INSERT INTO questions (title, content, answer, category_id, difficulty, created_at, updated_at) VALUES
(
    '软件测试的目的是什么？',
    '请阐述软件测试的主要目的和意义。',
    '软件测试的目的：

1. 发现缺陷（Bug）
   - 尽早发现软件中的错误
   - 减少软件中的缺陷

2. 验证质量
   - 验证软件满足需求
   - 确认软件达到预期质量标准

3. 提供信息
   - 向开发团队反馈质量信息
   - 为决策提供依据

4. 降低风险
   - 减少软件发布后的风险
   - 减少因缺陷导致的损失

5. 保证用户满意度
   - 确保软件稳定可用
   - 提供良好的用户体验

测试与调试的区别：
- 测试：验证程序是否满足需求，发现问题
- 调试：定位问题根源，修复代码

重要原则：
- 测试不能证明程序没有错误
- 只能证明存在错误
- 尽早测试，持续测试',
    (SELECT id FROM categories WHERE name = '测试基础'),
    'EASY',
    NOW(),
    NOW()
),
(
    '黑盒测试和白盒测试的区别？',
    '请解释黑盒测试和白盒测试的概念、方法和适用场景。',
    '黑盒测试（功能测试）：

定义：
- 不关注内部实现
- 只关注输入输出
- 站在用户角度测试

方法：
- 等价类划分
- 边界值分析
- 判定表法
- 场景法
- 正交试验法

适用场景：
- 功能测试
- 验收测试
- 系统测试

白盒测试（结构测试）：

定义：
- 了解内部结构
- 基于代码测试
- 关注实现逻辑

方法：
- 语句覆盖
- 判定覆盖
- 条件覆盖
- 路径覆盖
- 分支覆盖

适用场景：
- 单元测试
- 集成测试
- 代码审查

灰盒测试：
- 介于黑盒和白盒之间
- 了解部分内部结构
- 常用语集成测试',
    (SELECT id FROM categories WHERE name = '测试基础'),
    'EASY',
    NOW(),
    NOW()
),
(
    '测试左移和测试右移是什么？',
    '请解释测试左移和测试右移的概念和实践。',
    '测试左移（Shift Left Testing）：

概念：
- 将测试活动提前到开发周期早期
- 在需求阶段就介入

实践：
- 参与需求评审，提前发现需求问题
- 测试用例前置设计
- 代码审查参与
- 单元测试推动
- 静态测试

好处：
- 更早发现问题，成本更低
- 减少后期返工

测试右移（Shift Right Testing）：

概念：
- 将测试延伸到生产环境
- 监控真实用户使用情况

实践：
- 生产环境监控
- 灰度发布
- A/B测试
- 用户反馈收集
- 日志分析
- 异常监控

好处：
- 发现测试环境无法复现的问题
- 真实场景验证
- 持续改进产品质量

目标：在整个软件生命周期中持续测试',
    (SELECT id FROM categories WHERE name = '测试基础'),
    'MEDIUM',
    NOW(),
    NOW()
),
(
    '测试计划包括哪些内容？',
    '请说明测试计划文档的主要组成部分。',
    '测试计划主要内容包括：

1. 测试背景和目标
   - 项目背景
   - 测试目的和范围
   - 测试通过标准

2. 测试资源
   - 人力资源（测试人员分工）
   - 环境资源（测试环境配置）
   - 工具资源（测试工具）

3. 测试范围
   - 需要测试的功能
   - 不需要测试的功能
   - 测试重点

4. 测试策略
   - 测试类型（功能、性能、安全等）
   - 测试方法
   - 测试级别

5. 测试进度
   - 测试里程碑
   - 排期计划
   - 交付时间

6. 风险评估
   - 识别风险
   - 风险等级
   - 应对策略

7. 测试准入准出标准
   -准入：测试开始条件
   -准出：测试完成条件

8. 测试交付物
   - 测试用例
   - 测试报告
   - 缺陷报告',
    (SELECT id FROM categories WHERE name = '测试基础'),
    'MEDIUM',
    NOW(),
    NOW()
);
