-- ============================================
-- 测试数据初始化脚本（适配当前表结构）
-- ============================================

-- 清空旧数据（按外键顺序）
DELETE FROM wrong_questions;
DELETE FROM user_progress;
DELETE FROM questions;
DELETE FROM categories;
DELETE FROM users WHERE username NOT IN ('admin');

-- ============================================
-- 1. 插入分类
-- ============================================
INSERT INTO categories (name, description) VALUES
('Java基础', 'Java核心基础知识，包括面向对象、集合、异常等'),
('Spring框架', 'Spring Boot/Cloud框架相关面试题'),
('数据库', 'MySQL、PostgreSQL等数据库相关面试题'),
('中间件', 'Redis、RabbitMQ、Elasticsearch等中间件'),
('系统设计', '架构设计、分布式系统、微服务设计'),
('算法与数据结构', '常用算法、数据结构、LeetCode题型'),
('项目经验', '项目实战、技术选型、问题排查经验');

-- ============================================
-- 2. 插入题目
-- ============================================
INSERT INTO questions (title, content, answer, category_id, difficulty, type, created_at, updated_at) VALUES
(
  'Java中HashMap的底层实现原理',
  '请解释HashMap的底层数据结构、put和get流程、扩容机制。',
  '1. 底层数据结构：数组+链表+红黑树（JDK8+）
2. put流程：
   - 计算key的hash值
   - 通过(n-1)&hash确定桶位置
   - 若桶为空，直接插入
   - 若桶不为空，遍历链表/红黑树
   - 若找到相同key，覆盖value
   - 否则插入尾部（JDK8尾插法）
3. 扩容机制：
   - 默认容量16，负载因子0.75
   - 当size > threshold时扩容为2倍
   - 扩容后rehash重新分配位置
4. 红黑树化：链表长度>=8且数组长度>=64时转为红黑树',
  (SELECT id FROM categories WHERE name = 'Java基础'),
  'MEDIUM', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'ConcurrentHashMap如何保证线程安全？',
  '请说明ConcurrentHashMap在JDK7和JDK8中的实现区别。',
  'JDK7实现（分段锁）：
- 使用Segment数组，继承ReentrantLock
- 默认16个Segment，每个独立加锁
- put操作锁当前Segment
- 并发度16

JDK8实现（CAS+synchronized）：
- 移除Segment，使用Node数组
- put操作：
  1. 若桶为空，CAS无锁插入
  2. 若桶不为空，synchronized锁头节点
  3. 链表/红黑树操作
- 扩容：多线程协助迁移（transfer）
- size()：使用CounterCell计数，减少冲突

优势：JDK8粒度更细（单个桶），并发性能更好',
  (SELECT id FROM categories WHERE name = 'Java基础'),
  'HARD', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'Spring Boot自动配置原理',
  '请解释@SpringBootApplication注解和自动配置的工作机制。',
  '1. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan

2. 自动配置核心机制：
   - spring.factories文件（META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports）
   - 列出所有自动配置类
   - @Conditional条件注解控制生效

3. 关键条件注解：
   - @ConditionalOnClass：类路径存在某类
   - @ConditionalOnMissingBean：没有指定Bean
   - @ConditionalOnProperty：配置属性存在
   - @ConditionalOnWebApplication：Web环境

4. 执行流程：
   - 启动时加载AutoConfiguration.imports
   - 根据@Conditional条件判断
   - 满足条件则创建Bean
   - 用户可自定义Bean覆盖默认配置',
  (SELECT id FROM categories WHERE name = 'Spring框架'),
  'MEDIUM', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'Spring Boot中事务失效的场景',
  '列举常见的@Transactional注解失效场景及原因。',
  '常见失效场景：

1. 方法内部调用（this调用）
   - A类的a()方法调用b()方法，b上有@Transactional
   - 原因：this调用不走代理
   - 解决：注入自身Bean，或拆到不同类

2. private/protected方法
   - 原因：CGLIB代理无法重写私有方法
   - 解决：改为public

3. 异常被catch吞掉
   - 不会触发回滚
   - 解决：抛出异常

4. 异常类型不匹配
   - 默认只回滚RuntimeException和Error
   - 受检异常（Exception）不回滚
   - 解决：rollbackFor = Exception.class

5. 不同数据源
   - 事务管理器未配置多数据源
   - 没有指定transactionManager

6. 传播属性设置不当
   - REQUIRES_NEW：挂起当前事务
   - NEVER：存在事务则抛异常',
  (SELECT id FROM categories WHERE name = 'Spring框架'),
  'HARD', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'MySQL中索引失效的场景',
  '请列举哪些情况会导致索引失效，如何避免。',
  '索引失效的常见场景：

1. 违反最左前缀原则
   - 组合索引(a,b,c)，查询条件只用了b
   - 解决：按索引顺序使用条件

2. 隐式类型转换
   - WHERE varchar_col = 123（字符串列与数字比较）
   - 解决：类型匹配

3. 对索引列使用函数
   - WHERE DATE(create_time) = ''2024-01-01''
   - 解决：create_time >= ''2024-01-01'' AND create_time < ''2024-01-02''

4. 使用LIKE模糊匹配以%开头
   - WHERE name LIKE ''%keyword''
   - 解决：改为name LIKE ''keyword%''（后缀匹配可用索引）

5. OR条件导致全表扫描
   - WHERE a = 1 OR b = 2
   - 解决：拆成两个查询UNION ALL

6. NOT IN/!=操作符
   - 不会走索引
   - 解决：改用范围查询或ES

7. 数据区分度太低
   - 性别列（男/女）建立索引效果差',
  (SELECT id FROM categories WHERE name = '数据库'),
  'MEDIUM', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'MySQL的InnoDB和MyISAM区别',
  '请比较两种存储引擎的特点和适用场景。',
  'InnoDB（默认）：
1. 支持事务（ACID）
2. 行级锁（高并发）
3. 支持外键
4. 聚簇索引（数据和索引一起）
5. 支持MVCC（多版本并发控制）
6. 支持崩溃恢复（redo log）
7. 全文索引（MySQL 5.6+）
适用：OLTP、高并发、事务场景

MyISAM：
1. 不支持事务
2. 表级锁（并发低）
3. 不支持外键
4. 非聚簇索引（数据和索引分离）
5. 支持全文索引（原生）
6. 文件小，性能好
7. 支持压缩表
适用：OLAP、读多写少、数仓报表

InnoDB推荐度：★★★★★
MyISAM推荐度：★★☆☆☆',
  (SELECT id FROM categories WHERE name = '数据库'),
  'EASY', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'Redis的数据结构及使用场景',
  '请列举Redis支持的数据结构，并说明每种的实际应用场景。',
  '1. String（字符串）
   场景：缓存、计数器、分布式锁（SETNX）
   命令：GET/SET/INCR/DECR

2. Hash（哈希）
   场景：对象缓存、购物车
   命令：HGET/HSET/HGETALL

3. List（列表）
   场景：消息队列、最新消息列表、时间轴
   命令：LPUSH/RPUSH/LPOP/RPOP/LLEN

4. Set（集合）
   场景：去重、共同好友、标签系统
   命令：SADD/SMEMBERS/SINTER/SUNION

5. Sorted Set（有序集合）
   场景：排行榜、延时队列、限流
   命令：ZADD/ZRANGE/ZREVRANGE/ZSCORE

6. HyperLogLog
   场景：UV统计
   命令：PFADD/PFCOUNT

7. Bitmap
   场景：签到统计、在线用户
   命令：SETBIT/GETBIT/BITCOUNT

8. GEO
   场景：附近的人、LBS应用
   命令：GEOADD/GEORADIUS',
  (SELECT id FROM categories WHERE name = '中间件'),
  'MEDIUM', 'SHORT_ANSWER', NOW(), NOW()
),
(
  'Redis缓存穿透、缓存击穿、缓存雪崩的区别及解决方案',
  '请解释三种缓存问题的概念和应对策略。',
  '1. 缓存穿透
概念：查询不存在的数据，每次都要查DB
原因：恶意攻击/非法key
解决：
  - 缓存空值（设置短TTL）
  - 布隆过滤器（Bloom Filter）
  - 参数校验

2. 缓存击穿
概念：热点key过期，大量请求打到DB
原因：缓存失效 + 高并发
解决：
  - 互斥锁（SETNX）
  - 逻辑过期（不设物理过期时间）
  - 热点数据永不过期

3. 缓存雪崩
概念：大量key同时过期，DB被打垮
原因：批量key过期 / Redis宕机
解决：
  - 过期时间打散（随机值）
  - 多级缓存（本地缓存+Redis）
  - Redis集群/哨兵高可用
  - 限流降级
  - 提前预热',
  (SELECT id FROM categories WHERE name = '中间件'),
  'HARD', 'SHORT_ANSWER', NOW(), NOW()
),
(
  '设计一个短链接系统',
  '请设计一个高并发短链接系统，包括架构设计、存储方案、重定向流程。',
  '1. 需求分析
   - 长链接转短链接
   - 访问短链接重定向到长链接
   - 高并发读（重定向）
   - 支持过期时间

2. 发号器方案（ID生成）
   - 雪花算法（Snowflake）：分布式ID
   - 自增ID + Base62编码（62^6=568亿，6位足够）
   - 预取号（批量生成，提升性能）

3. 存储设计
   - 关系型：短码 → 长链接 + 过期时间
   - 缓存层：Redis缓存热点短链接
   - 分库分表：按短码hash分片

4. 接口设计
   POST /api/shorten
   { url: ''https://...'', expireIn: 86400 }
   → { shortUrl: ''https://s.dd/abc123'' }

   GET /{shortCode}
   → 301/302 重定向到长链接

5. 高性能优化
   - CDN加速静态页面
   - DNS负载均衡
   - 预加载缓存
   - 异步刷盘

6. 高可用
   - 多机房部署
   - 读写分离
   - 降级方案',
  (SELECT id FROM categories WHERE name = '系统设计'),
  'HARD', 'SHORT_ANSWER', NOW(), NOW()
),
(
  '反转链表（LeetCode 206）',
  '请实现单链表反转，要求迭代和递归两种方式。',
  '迭代法：
public ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}

递归法：
public ListNode reverseList(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode newHead = reverseList(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
}

复杂度：
- 时间：O(n)
- 空间：迭代O(1)，递归O(n)',
  (SELECT id FROM categories WHERE name = '算法与数据结构'),
  'MEDIUM', 'CODING', NOW(), NOW()
),
(
  '如何排查Java线上OOM问题？',
  '请描述线上OOM的排查思路和常用工具。',
  '排查步骤：

1. 确认OOM
   - 查看日志：java.lang.OutOfMemoryError
   - 确定是哪类OOM（堆/元空间/栈）

2. 获取Heap Dump
   - 启动参数：-XX:+HeapDumpOnOutOfMemoryError
   - jmap命令：jmap -dump:format=b,file=heap.hprof <pid>

3. 分析Heap Dump
   - 工具：MAT(Eclipse Memory Analyzer)、JProfiler
   - 查看大对象、GC Root引用链
   - 分析内存泄漏点

4. 常见原因
   - 集合类未释放（HashMap无限增长）
   - 线程池未正确配置
   - 大对象频繁创建
   - 第三方库内存泄漏

5. 预防措施
   - JVM参数合理配置（Xms=Xmx）
   - 代码Review
   - 压力测试
   - 监控告警（Prometheus+Grafana）

常用命令：
jps -l          # 查看Java进程
jinfo <pid>     # 查看JVM配置
jstat -gc <pid> # 查看GC情况
jstack <pid>    # 查看线程栈',
  (SELECT id FROM categories WHERE name = '项目经验'),
  'HARD', 'SHORT_ANSWER', NOW(), NOW()
);

-- ============================================
-- 3. 创建测试用户
-- ============================================
INSERT INTO users (username, email, password, nickname, enabled, role_id) VALUES
('test', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '测试用户', true, 2),
('demo', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '演示用户', true, 2),
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', true, 1);

-- ============================================
-- 4. 插入学习进度
-- ============================================
INSERT INTO user_progress (user_id, question_id, status, is_correct, is_favorite, review_count, last_reviewed_at)
SELECT u.id, q.id, 'MASTERED', true, true, 5, NOW() - INTERVAL '1 day'
FROM (SELECT id FROM users WHERE username = 'test') u
CROSS JOIN (SELECT id FROM questions LIMIT 3 OFFSET 0) q;

INSERT INTO user_progress (user_id, question_id, status, is_correct, is_favorite, review_count, last_reviewed_at)
SELECT u.id, q.id, 'LEARNING', true, false, 2, NOW() - INTERVAL '3 hours'
FROM (SELECT id FROM users WHERE username = 'test') u
CROSS JOIN (SELECT id FROM questions LIMIT 2 OFFSET 3) q;

INSERT INTO user_progress (user_id, question_id, status, is_favorite, review_count)
SELECT u.id, q.id, 'NEW', false, 0
FROM (SELECT id FROM users WHERE username = 'test') u
CROSS JOIN (SELECT id FROM questions ORDER BY id DESC LIMIT 2) q;

-- ============================================
-- 5. 插入错题记录
-- ============================================
INSERT INTO wrong_questions (user_id, question_id, user_answer, correct_answer, is_correct, wrong_count, last_wrong_at)
VALUES
((SELECT id FROM users WHERE username = 'test'),
 (SELECT id FROM questions ORDER BY id LIMIT 1 OFFSET 4),
 '不太清楚哈希扩容的具体流程...',
 (SELECT answer FROM questions ORDER BY id LIMIT 1 OFFSET 4),
 false, 2, NOW() - INTERVAL '2 days'),

((SELECT id FROM users WHERE username = 'test'),
 (SELECT id FROM questions ORDER BY id LIMIT 1 OFFSET 5),
 '写反了ACID的特性',
 (SELECT answer FROM questions ORDER BY id LIMIT 1 OFFSET 5),
 false, 1, NOW() - INTERVAL '1 day');
