-- ============================================
-- 测试数据初始化脚本（适配当前表结构）
-- ============================================

-- 清空旧数据（按外键顺序）
DELETE FROM wrong_questions;
DELETE FROM user_progress;
DELETE FROM questions;
DELETE FROM categories;
DELETE FROM comment_likes;
DELETE FROM comments;
DELETE FROM article_tags;
DELETE FROM article_likes;
DELETE FROM bookmarks;
DELETE FROM blockers;
DELETE FROM notifications;
DELETE FROM article_daily_views;
DELETE FROM series;
DELETE FROM follows;
DELETE FROM articles;
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

-- ============================================
-- 6. 补充多题型题目
-- ============================================
INSERT INTO questions (title, content, answer, category_id, difficulty, type, created_at, updated_at) VALUES
(
  'Java中哪个关键字可以实现线程同步？',
  '以下哪个关键字可以保证线程安全？',
  'A. synchronized\nB. volatile\nC. transient\nD. final',
  (SELECT id FROM categories WHERE name = 'Java基础'),
  'EASY', 'SINGLE_CHOICE', NOW(), NOW()
),
(
  '下列哪些是 Spring Boot 的 Starter？',
  '以下哪些属于 Spring Boot 官方提供的 Starter？（多选）',
  'A. spring-boot-starter-web\nB. spring-boot-starter-data-jpa\nC. spring-boot-starter-dubbo\nD. spring-boot-starter-security',
  (SELECT id FROM categories WHERE name = 'Spring框架'),
  'MEDIUM', 'MULTIPLE_CHOICE', NOW(), NOW()
),
(
  'HashMap 是线程安全的吗？',
  '判断：HashMap 在多线程环境下可以安全使用。',
  'FALSE\nHashMap 不是线程安全的，多线程环境应使用 ConcurrentHashMap。',
  (SELECT id FROM categories WHERE name = 'Java基础'),
  'EASY', 'TRUE_FALSE', NOW(), NOW()
),
(
  '实现 LRU 缓存',
  '设计并实现一个 LRU（最近最少使用）缓存机制，支持 get 和 put 操作，要求时间复杂度 O(1)。',
  'class LRUCache extends LinkedHashMap<Integer, Integer> {\n    private final int capacity;\n    public LRUCache(int capacity) {\n        super(capacity, 0.75f, true);\n        this.capacity = capacity;\n    }\n    public int get(int key) {\n        return super.getOrDefault(key, -1);\n    }\n    public void put(int key, int value) {\n        super.put(key, value);\n    }\n    @Override\n    protected boolean removeEldestEntry(Map.Entry eldest) {\n        return size() > capacity;\n    }\n}',
  (SELECT id FROM categories WHERE name = '算法与数据结构'),
  'HARD', 'CODING', NOW(), NOW()
),
(
  '下列哪些是 Redis 支持的淘汰策略？',
  '以下哪些属于 Redis 的内存淘汰策略？（多选）',
  'A. noeviction\nB. allkeys-lru\nC. volatile-ttl\nD. allkeys-random',
  (SELECT id FROM categories WHERE name = '中间件'),
  'MEDIUM', 'MULTIPLE_CHOICE', NOW(), NOW()
),
(
  'PostgreSQL 支持 JSON 数据类型吗？',
  '判断：PostgreSQL 原生支持 JSON 和 JSONB 数据类型。',
  'TRUE\nPostgreSQL 支持 JSON 和 JSONB 两种类型，JSONB 支持索引和更高效的查询。',
  (SELECT id FROM categories WHERE name = '数据库'),
  'EASY', 'TRUE_FALSE', NOW(), NOW()
);

-- ============================================
-- 7. 社区数据：文章
-- ============================================
INSERT INTO articles (title, content, user_id, topic_id, view_count, comment_count, thumbs_up_count, status, created_at, updated_at) VALUES
(
  '深入理解 Java 内存模型',
  '## 前言\n\nJava内存模型（JMM）是理解并发编程的基础。本文将从底层原理出发，详细解析JMM的核心概念。\n\n## 主内存与工作内存\n\n每个线程都有自己的工作内存，线程对变量的操作必须在工作内存中进行，不能直接操作主内存。\n\n## volatile 关键字\n\nvolatile 保证了可见性和禁止指令重排，但不保证原子性。\n\n## happens-before 原则\n\n1. 程序顺序规则\n2. 监视器锁规则\n3. volatile 变量规则\n4. 传递性\n\n## 总结\n\n理解JMM是写好并发代码的基础，建议结合 JUC 源码深入学习。',
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM topics WHERE topic_name = 'Java'),
  120, 3, 15, 'PUBLISHED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'
),
(
  'Spring Boot 微服务实战总结',
  '## 项目背景\n\n最近完成了一个基于 Spring Boot 的微服务项目，总结一些经验。\n\n## 技术栈\n\n- Spring Boot 3.2\n- Spring Cloud Gateway\n- Nacos 注册中心\n- Sentinel 限流\n\n## 踩坑记录\n\n### 1. 服务间调用超时\n配置合理的超时时间，避免雪崩。\n\n### 2. 分布式事务\n使用 Seata AT 模式解决。\n\n## 总结\n\n微服务架构并非银弹，需要根据业务规模合理选择。',
  (SELECT id FROM users WHERE username = 'demo'),
  (SELECT id FROM topics WHERE topic_name = 'Spring Boot'),
  85, 5, 22, 'PUBLISHED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
),
(
  'Vue 3 组合式 API 最佳实践',
  '## 为什么要用组合式 API？\n\n相比 Options API，组合式 API 更适合大型项目的逻辑复用。\n\n## 常用模式\n\n### 1. 自定义 Composable\n```js\nexport function useCounter(initial = 0) {\n  const count = ref(initial)\n  const increment = () => count.value++\n  return { count, increment }\n}\n```\n\n### 2. 响应式解耦\n使用 `toRefs` 解构 props 保持响应性。\n\n## 总结\n\n组合式 API 是 Vue 3 的核心优势，善用它可以让代码更清晰。',
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM topics WHERE topic_name = 'Vue.js'),
  200, 8, 30, 'PUBLISHED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
),
(
  'PostgreSQL 性能优化指南',
  '## 索引优化\n\n### B-tree 索引\n最常用的索引类型，适合等值查询和范围查询。\n\n### 部分索引\n```sql\nCREATE INDEX idx_active_users ON users(email) WHERE active = true;\n```\n\n## 查询优化\n\n使用 EXPLAIN ANALYZE 分析执行计划。\n\n## 连接池配置\n\n推荐使用 PgBouncer，配置合理的连接池大小。',
  (SELECT id FROM users WHERE username = 'demo'),
  (SELECT id FROM topics WHERE topic_name = '数据库'),
  65, 2, 10, 'PUBLISHED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
),
(
  'Redis 集群搭建与运维实战',
  '## 集群架构\n\nRedis Cluster 采用哈希槽分片，共 16384 个槽。\n\n## 搭建步骤\n\n1. 准备 6 个节点（3主3从）\n2. 执行 `redis-cli --cluster create`\n3. 验证集群状态\n\n## 常见问题\n\n### 脑裂问题\n配置 `min-replicas-to-write` 防止。\n\n### 大 Key 处理\n使用 `UNLINK` 异步删除。',
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM topics WHERE topic_name = 'Java'),
  150, 4, 18, 'PUBLISHED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'
);

-- ============================================
-- 8. 社区数据：标签关联
-- ============================================
INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a, tags t
WHERE a.title = '深入理解 Java 内存模型' AND t.tag_name IN ('java');

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a, tags t
WHERE a.title = 'Spring Boot 微服务实战总结' AND t.tag_name IN ('spring', '微服务');

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a, tags t
WHERE a.title = 'Vue 3 组合式 API 最佳实践' AND t.tag_name IN ('vue');

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a, tags t
WHERE a.title = 'PostgreSQL 性能优化指南' AND t.tag_name IN ('postgresql');

INSERT INTO article_tags (article_id, tag_id)
SELECT a.id, t.id FROM articles a, tags t
WHERE a.title = 'Redis 集群搭建与运维实战' AND t.tag_name IN ('redis');

-- ============================================
-- 9. 社区数据：评论（含嵌套回复）
-- ============================================
INSERT INTO comments (content, article_id, user_id, parent_id, like_count, created_at) VALUES
(
  '写得很清晰，JMM这部分终于看懂了！',
  (SELECT id FROM articles WHERE title = '深入理解 Java 内存模型'),
  (SELECT id FROM users WHERE username = 'demo'),
  NULL, 3, NOW() - INTERVAL '4 days'
),
(
  '谢谢，有什么不理解的地方可以留言讨论',
  (SELECT id FROM articles WHERE title = '深入理解 Java 内存模型'),
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM comments WHERE content = '写得很清晰，JMM这部分终于看懂了！'), 1, NOW() - INTERVAL '4 days' + INTERVAL '1 hour'
),
(
  '微服务踩坑那段太真实了，我们项目也遇到了类似问题',
  (SELECT id FROM articles WHERE title = 'Spring Boot 微服务实战总结'),
  (SELECT id FROM users WHERE username = 'test'),
  NULL, 5, NOW() - INTERVAL '2 days'
),
(
  'Seata AT 模式的性能开销如何？有做过压测吗？',
  (SELECT id FROM articles WHERE title = 'Spring Boot 微服务实战总结'),
  (SELECT id FROM users WHERE username = 'demo'),
  NULL, 2, NOW() - INTERVAL '2 days' + INTERVAL '2 hours'
),
(
  'toRefs 这个技巧很实用，收藏了',
  (SELECT id FROM articles WHERE title = 'Vue 3 组合式 API 最佳实践'),
  (SELECT id FROM users WHERE username = 'demo'),
  NULL, 4, NOW() - INTERVAL '1 day'
);

-- ============================================
-- 10. 社区数据：点赞
-- ============================================
INSERT INTO article_likes (article_id, user_id)
SELECT a.id, u.id FROM articles a, users u
WHERE a.title = '深入理解 Java 内存模型' AND u.username = 'demo';

INSERT INTO article_likes (article_id, user_id)
SELECT a.id, u.id FROM articles a, users u
WHERE a.title = 'Vue 3 组合式 API 最佳实践' AND u.username = 'demo';

INSERT INTO article_likes (article_id, user_id)
SELECT a.id, u.id FROM articles a, users u
WHERE a.title = 'Spring Boot 微服务实战总结' AND u.username = 'test';

-- ============================================
-- 11. 社区数据：关注关系
-- ============================================
INSERT INTO follows (user_id, following_id) VALUES
((SELECT id FROM users WHERE username = 'test'), (SELECT id FROM users WHERE username = 'demo')),
((SELECT id FROM users WHERE username = 'demo'), (SELECT id FROM users WHERE username = 'test'));

-- ============================================
-- 12. 社区数据：系列
-- ============================================
INSERT INTO series (user_id, title, description, article_count, created_at) VALUES
(
  (SELECT id FROM users WHERE username = 'test'),
  'Java 并发编程系列',
  '从基础到进阶，系统学习 Java 并发编程',
  2, NOW() - INTERVAL '6 days'
),
(
  (SELECT id FROM users WHERE username = 'demo'),
  '微服务架构实战',
  '记录微服务项目的踩坑与经验',
  1, NOW() - INTERVAL '4 days'
);

-- 文章关联系列
UPDATE articles SET series_id = (SELECT id FROM series WHERE title = 'Java 并发编程系列'), series_order = 1
WHERE title = '深入理解 Java 内存模型';

UPDATE articles SET series_id = (SELECT id FROM series WHERE title = 'Java 并发编程系列'), series_order = 2
WHERE title = 'Redis 集群搭建与运维实战';

UPDATE articles SET series_id = (SELECT id FROM series WHERE title = '微服务架构实战'), series_order = 1
WHERE title = 'Spring Boot 微服务实战总结';

-- ============================================
-- 13. 社区数据：通知
-- ============================================
INSERT INTO notifications (user_id, type, data, is_read, from_user_id, target_id, summary, created_at) VALUES
(
  (SELECT id FROM users WHERE username = 'test'),
  'COMMENT',
  '{"articleTitle": "深入理解 Java 内存模型"}',
  true,
  (SELECT id FROM users WHERE username = 'demo'),
  (SELECT id FROM articles WHERE title = '深入理解 Java 内存模型'),
  'demo 评论了你的文章',
  NOW() - INTERVAL '4 days'
),
(
  (SELECT id FROM users WHERE username = 'demo'),
  'LIKE',
  '{"articleTitle": "Spring Boot 微服务实战总结"}',
  false,
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM articles WHERE title = 'Spring Boot 微服务实战总结'),
  'test 赞了你的文章',
  NOW() - INTERVAL '3 days'
),
(
  (SELECT id FROM users WHERE username = 'test'),
  'FOLLOW',
  '{}',
  false,
  (SELECT id FROM users WHERE username = 'demo'),
  NULL,
  'demo 关注了你',
  NOW() - INTERVAL '2 days'
),
(
  (SELECT id FROM users WHERE username = 'demo'),
  'COMMENT',
  '{"articleTitle": "Vue 3 组合式 API 最佳实践"}',
  false,
  (SELECT id FROM users WHERE username = 'test'),
  (SELECT id FROM articles WHERE title = 'Vue 3 组合式 API 最佳实践'),
  'test 评论了你的文章',
  NOW() - INTERVAL '1 day'
);

-- ============================================
-- 14. 社区数据：书签/收藏
-- ============================================
INSERT INTO bookmarks (user_id, article_id, created_at) VALUES
((SELECT id FROM users WHERE username = 'test'), (SELECT id FROM articles WHERE title = 'Spring Boot 微服务实战总结'), NOW() - INTERVAL '2 days'),
((SELECT id FROM users WHERE username = 'demo'), (SELECT id FROM articles WHERE title = 'Vue 3 组合式 API 最佳实践'), NOW() - INTERVAL '1 day'),
((SELECT id FROM users WHERE username = 'demo'), (SELECT id FROM articles WHERE title = '深入理解 Java 内存模型'), NOW() - INTERVAL '3 days');
