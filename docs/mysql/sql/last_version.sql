create database if not exists relics default character set utf8mb4 collate utf8mb4_0900_ai_ci;
create  database IF NOT EXISTS relics;
use relics;

-- ----------------------------
--  Table structure for `users` (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
                         `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                         `username` VARCHAR(32) NOT NULL COMMENT '用户唯一标识',
                         `nickname` VARCHAR(64) NULL COMMENT '昵称',
                         `full_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '真实姓名',
                         `password` VARCHAR(256) NOT NULL COMMENT '密码 (应加密存储)',
                         `email` VARCHAR(32) NULL DEFAULT NULL COMMENT '邮箱',
                         `phone_number` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号码',
                         `avatar_url` VARCHAR(256) NULL DEFAULT NULL COMMENT '头像URL',
                         `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '用户状态 (例如: 1=正常, 0=禁用)',
                         `role` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '角色 (例如: user, expert)',
                         `title` VARCHAR(255) NULL DEFAULT NULL COMMENT '头衔/职位',
                         `permission` INT NULL DEFAULT 0 COMMENT '权限级别或位掩码',
                         `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_user_name` (`username`),
                         UNIQUE KEY `uk_email` (`email`),
                         UNIQUE KEY `uk_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for location (位置表)
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
                            `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                            `name` VARCHAR(128) NOT NULL COMMENT '位置名称 (如: "一号展厅A区3号展柜")',
                            `description` TEXT NULL COMMENT '位置详细描述',
                            `parent_id` INT NULL DEFAULT NULL COMMENT '父级位置ID，用于构建层级关系，NULL表示顶级位置',
                            `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='位置表';

-- ----------------------------
-- Table structure for relics (文物表)
-- ----------------------------
DROP TABLE IF EXISTS `relics`;
CREATE TABLE `relics` (
                          `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                          `relics_id` VARCHAR(64) NOT NULL COMMENT '文物业务ID (如馆藏编号)，全局唯一',
                          `name` VARCHAR(128) NOT NULL COMMENT '文物名称',
                          `description` TEXT NULL COMMENT '文物详细描述',
                          `preservation` TINYINT NOT NULL COMMENT '保护等级 (如: 1=一级, 2=二级, 3=珍贵)',
                          `category` VARCHAR(64) NULL DEFAULT NULL COMMENT '类别 (如: 青铜器, 瓷器, 书画)',
                          `era` VARCHAR(32) NULL DEFAULT NULL COMMENT '所属年代 (如: 商代, 明朝)',
                          `material` VARCHAR(64) NULL DEFAULT NULL COMMENT '主要材质 (如: 青铜, 陶瓷)',
                          `image_url` VARCHAR(256) NULL DEFAULT NULL COMMENT '文物图片链接',
                          `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 (0: 库房存储, 1: 展出中, 2: 修复中)',
                          `location_id` INT NULL DEFAULT NULL COMMENT '所在位置ID，关联location表',
                          `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_relics_id` (`relics_id`),
                          KEY `idx_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文物表';

INSERT INTO `relics` (`relics_id`, `name`, `description`, `preservation`, `category`, `era`, `material`, `image_url`, `status`, `location_id`) VALUES
-- 汉朝文物（6件）
('2001', '长信宫灯', '西汉青铜鎏金灯具，高48厘米。宫女跪坐持灯造型，右臂为烟道可将废气导入体内水槽。灯盘可转动调节光照，衣纹线条流畅，1968年河北满城汉墓出土。', 1, '青铜器', '汉', '青铜', 'https://n.sinaimg.cn/sinakd20123/603/w605h798/20200423/6e18-isqivxh6722573.jpg', 1, NULL),
('2002', '金缕玉衣', '西汉中山靖王刘胜殓服，长188厘米。用2498片和田青玉片以金丝编缀而成，分头罩、上衣、手套等六部分。玉片四角钻孔，金丝纯度达96%，体现汉代葬玉制度巅峰。', 1, '玉器', '汉', '和田玉', 'https://d.ifengimg.com/q100/img1.ugc.ifeng.com/newugc/20201024/17/wemedia/3082c57930a82e8986f1ccf633981f8720890820_size188_w1000_h666.jpg', 0, NULL),
('2003', '马踏飞燕', '东汉青铜器，高34.5厘米。奔马三足腾空，右后足踏飞鸟，完美解决力学平衡。马首左扬，尾梢打结，展现河西良种马特征。1969年甘肃武威雷台汉墓出土。', 1, '青铜器', '汉', '青铜', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.B5JkpBX1tqUDrXk8zI6uPQHaFj?w=190&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('2004', 'T型帛画', '西汉帛画珍品，长205厘米。上绘日、月、升天、人间、地下三界，女娲、金乌、蟾蜍等神话元素。色彩以朱砂、石青为主，1972年湖南长沙马王堆一号汉墓出土。', 1, '帛画', '汉', '丝绢', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.0HOc-6KGaZhoa18_bPdlgwAAAA?o=7&cb=thvnextc1rm=3&rs=1&pid=ImgDetMain&o=7&rm=3', 2, NULL),
('2005', '错金银云纹铜犀尊', '西汉酒器，高34.1厘米。犀牛造型写实，通体饰错金银云纹，背部活盖可注酒，嘴部流孔倒酒。肌肉线条精准，眼珠以蓝琉璃镶嵌，1963年陕西兴平出土。', 2, '青铜器', '汉', '青铜', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.EMcbcLYtjWqtM5eyKpfwUgHaEm?w=311&h=193&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('2008', '角形玉杯', '南越国玉器，高18.4厘米。青白玉雕犀牛角形，外壁浮雕卷云纹。口沿阴刻绶带纹，底部缠绕螭龙，1983年广州象岗南越王墓出土。', 1, '玉器', '汉', '和田玉', 'https://th.bing.com/th/id/OIP.RlOXjPm4HJi_sOwUXgikPgHaIP?w=155&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.3&pid=1.7&rm=3', 2, NULL),

-- 唐朝文物（6件）
('3001', '唐三彩骆驼载乐俑', '唐代陶俑，高58厘米。双峰驼背驮平台，七名胡汉乐俑奏琵琶、箜篌等乐器。釉色以黄、绿、褐为主，骆驼仰首嘶鸣，展现丝绸之路文化交流。1959年陕西西安出土。', 1, '陶俑', '唐', '陶土', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.eQuYU2C_OTogifIPz4VA_QHaFE?w=265&h=181&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('3002', '秘色瓷八棱净瓶', '唐代越窑贡瓷，高21.7厘米。通体青绿色釉如湖水，瓶身八棱形，颈细长，圈足外撇。釉面均匀无开片，为法门寺地宫出土秘色瓷标准器。', 1, '瓷器', '唐', '瓷土', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.sWPHAyGC3giFASQhpuUaYAHaJ4?w=203&h=271&c=7&r=0&o=5&cb=thvnextc1&dpr=1.5&pid=1.7', 0, NULL),
('3003', '鎏金舞马衔杯银壶', '唐代银器，高18.5厘米。壶身锤揲双马衔杯纹，马颈系彩带作舞蹈状。印证玄宗时期舞马祝寿史实，1970年陕西西安何家村窖藏出土。', 1, '金银器', '唐', '白银', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.T0egvcZKtljCvSEcVyH-XAHaIp?w=186&h=217&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('3004', '颜勤礼碑拓本', '唐代书法碑刻原拓，高175厘米。颜真卿71岁所书楷书，笔力雄浑，结体开张。碑文记述其曾祖父颜勤礼生平，为颜体成熟期代表作。北宋初年西安出土。', 2, '碑帖', '唐', '纸张', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.yluvg6ECUxc69XuIuZkL0gHaGP?w=253&h=213&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),
('3005', '螺钿紫檀五弦琵琶', '唐代宫廷乐器，长108厘米。紫檀木制，镶嵌螺钿骆驼胡人纹，腹板贴玳瑁拨片。五弦制式罕见，存世唯一完整唐琵琶，日本正仓院旧藏。', 1, '乐器', '唐', '紫檀木', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.2f670T-RiFgVV5viHmNeWgHaMW?w=193&h=322&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('3006', '花鸟纹八瓣银杯', '唐代金银器，高5.4厘米。杯身八曲葵口，每瓣錾刻不同花鸟。圈足饰联珠纹，内底双鱼纹，1970年西安何家村窖藏出土。', 2, '金银器', '唐', '白银', 'https://th.bing.com/th/id/OIP.JqxFn3jOW0hsj62pAgmNngHaE8?w=302&h=202&c=7&r=0&o=7&cb=thvnextc1&dpr=1.3&pid=1.7&rm=3', 1, NULL),

-- 宋朝文物（6件）
('4001', '汝窑天青釉洗', '北宋汝官窑瓷器，口径13厘米。通体天青色釉，釉面开细碎冰裂纹。圈足裹釉支烧，底留三芝麻钉痕，为宋代“雨过天青”审美典范。', 1, '瓷器', '宋', '瓷土', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.olFBW5zGGZ0jIKbBVKpDxAHaEc?w=268&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('4002', '《清明上河图》摹本', '北宋风俗画摹本，长528厘米。张择端原作的宋代摹本，描绘汴京城乡风貌，绘有814人、28船、60余牲畜。采用散点透视法，绢本设色。', 1, '书画', '宋', '绢帛', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.8BtQFs1uw5Ur5BIuniYvAQHaEK?w=293&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 2, NULL),
('4003', '定窑白釉孩儿枕', '北宋定窑瓷枕，长30厘米。孩儿伏卧造型，双目炯炯，身着长衫。釉色牙白，积釉处呈泪痕状，体现定窑“白如玉”特征。', 2, '瓷器', '宋', '瓷土', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.XgjJzrMSpn0T0r7xbNhXHAHaFd?w=225&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('4004', '哥窑鱼耳炉', '南宋哥窑瓷器，高8.8厘米。双鱼形耳，釉面满布金丝铁线开片。紫口铁足，釉色米黄泛灰青，为宋代文人书房陈设雅器。', 1, '瓷器', '宋', '瓷土', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.ouBbZM2tprH2lyAT-lsEswHaEI?w=321&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),
('4005', '《淳化阁帖》刻石', '北宋法帖原石，每石高30厘米。宋太宗命摹刻历代名家书法，共十卷420帖。刻工精良保留王羲之《兰亭序》等珍迹，1842年西安碑林重刻。', 1, '碑刻', '宋', '青石', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.JxRoOqPYVjBmj7gI8DzFEgHaIH?w=125&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('4006', '木雕观音坐像', '宋代佛教造像，高118厘米。观音右舒坐，袒胸饰璎珞。衣纹如水波流动，面部丰腴含笑，典型宋代人性化宗教艺术表现。', 1, '木雕', '宋', '楠木', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.KhhCRFKKN-Yxr7EIIntyNAHaJm?w=190&h=247&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 2, NULL),

-- 元朝文物（6件）
('5001', '青花萧何月下追韩信梅瓶', '元代青花瓷，高44.1厘米。瓶身绘萧何策马追韩信场景，人物衣纹飘逸。苏麻离青料发色靛蓝，有铁锈斑，层多达九层，1950年江苏南京出土。', 1, '瓷器', '元', '瓷土', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.HGSS0alk66wVVW5w11x22wHaFK?w=282&h=196&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('5002', '《富春山居图》剩山图', '元代黄公望纸本水墨，纵33厘米。描绘富春江初秋景色，用披麻皴绘山石，墨色层次丰富。此卷为原画前段，明末火殉后残存。', 1, '书画', '元', '纸张', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.8HEgAhyClfpxjGPd19nINwHaHv?w=180&h=188&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 2, NULL),
('5003', '银槎杯', '元代银器，高18厘米。整体作桧柏根槎造型，道人倚槎读书。槎身镌“朱碧山造”款，为元代银工名家唯一传世品。', 1, '金银器', '元', '白银', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.3feTRW2kMTH6cq7Xx_o_dQHaHa?w=170&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),
('5004', '蓝釉白龙纹梅瓶', '元代景德镇瓷，高33厘米。通体施钴蓝釉，瓶身刻白龙赶珠纹。龙三爪怒张，釉色如蓝宝石，存世仅三件，1980年江苏扬州出土。', 1, '瓷器', '元', '瓷土', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.p38n4jH2LhB5gfRDYL4MTwHaLP?w=186&h=284&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('5005', '渎山大玉海', '元代玉瓮，高70厘米。重3.5吨，墨玉质。外壁浮雕海龙、海马等祥瑞，内膛可贮酒三十石。元世祖忽必烈宴饮所用，现存北京北海团城。', 1, '玉器', '元', '墨玉', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.AJLeu42UMqOJf0RUNfKoNwHaFN?w=223&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('5006', '釉里红缠枝菊纹玉壶春瓶', '元代瓷器，高28.6厘米。铜红料绘缠枝菊纹，发色鲜红带绿斑。釉面开冰裂纹，为釉里红初创期稀有完整器。', 1, '瓷器', '元', '瓷土', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.K1UumfS5ZGa9if9PU7MhEwHaKS?w=143&h=198&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),

-- 明朝文物（6件）
('6001', '斗彩鸡缸杯', '明成化瓷器，高3.4厘米。外壁绘子母鸡群，青花勾勒轮廓，填以红、黄、绿彩。胎薄如纸，底书“大明成化年制”青花款。', 1, '瓷器', '明', '瓷土', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.fyT1_hMSxvA37Nyo_CTt6gHaEj?w=264&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('6002', '孝端皇后凤冠', '明代礼冠，高27厘米。九龙九凤金丝点翠，嵌红蓝宝石128块。冠胎以漆竹编成，珍珠4414颗，1957年北京定陵出土。', 1, '金银器', '明', '黄金', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.tHoYxdlYJXRJjmoDFMtnGgHaE8?w=209&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),
('6003', '宣德炉', '明宣宗时期铜炉，高8.3厘米。风磨铜铸造，栗壳皮色。底款“大明宣德年制”楷书，器型仿《宣和博古图》鼎彝。', 2, '铜器', '明', '黄铜', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.H34yh_ggKrzUlX3GmsGBdAHaE8?w=294&h=196&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('6004', '《永乐大典》嘉靖抄本', '明代典籍，纵50厘米。朱丝栏白棉纸本，楷书工整。此册为卷2609“台”字册，收录历代御史台资料，现存43卷。', 1, '古籍', '明', '纸张', 'https://tse3-mm.cn.bing.net/th/id/OIP-C.I0s0iHhJwQULdzSsZQ46gwHaNN?w=186&h=331&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 2, NULL),
('6005', '缂丝十二章衮服', '明万历帝龙袍，长136厘米。缂丝工艺织成，饰日、月、星辰等十二章纹。金线织团龙16条，共用彩绒20余色。', 1, '织绣', '明', '蚕丝', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.9nduZnaJap3zTt2_3unsbwHaFj?w=201&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('6006', '宣德青花海水龙纹罐', '明宣德官窑瓷，高48.5厘米。腹部绘五爪龙穿梭海浪，苏麻离青料浓艳带铁斑。口沿书"大明宣德年制"楷书款。', 1, '瓷器', '明', '瓷土', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.1HOejio-pjS3Iev_9Y9CyAHaHA?w=221&h=209&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),


-- 清朝文物（6件）
('7001', '翠玉白菜', '清代玉雕，长18.7厘米。巧用翡色雕菜叶，白色作菜帮。叶脉纹理清晰，菜叶伏螽斯与蝗虫，象征多子多福。台北故宫博物院藏。', 1, '玉器', '清', '翡翠', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.cAlnp3yhlSogukxC-d-s7AAAAA?w=149&h=199&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('7002', '珐琅彩芙蓉雉鸡碗', '清代官窑瓷，口径15厘米。外壁绘芙蓉雉鸡图，题“青扶承露蕊，红妥出阑枝”诗句。胎薄釉润，底书“雍正年制”蓝料款。', 1, '瓷器', '清', '瓷土', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.s9DzhgyPP_bzklxV5vUVfgHaFu?w=256&h=197&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 0, NULL),
('7003', '大禹治水图玉山', '清代玉雕，高224厘米。重5吨，青玉雕大禹治水场景。山岩错落，人物逾百，底镌乾隆御制诗，扬州工匠十年制成。', 1, '玉器', '清', '青玉', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.J8vrxcRku3iJ-ymu8xVEHQHaM9?w=115&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('7004', '金瓯永固杯', '清代礼器，高12.5厘米。黄金铸杯身，嵌珍珠红蓝宝石。三象首足，杯耳夔龙，为元旦开笔仪式御用，底铸“乾隆年制”。', 1, '金银器', '清', '黄金', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.VxCWRW0svnBj-KESy6fGAwHaI1?w=146&h=180&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL),
('7005', '《圆明园四十景图咏》', '清代绢本彩绘，纵64厘米。乾隆年间宫廷画师绘圆明园建筑景观，每景配御制诗。1860年英法联军劫掠后散佚，现存法国国家图书馆。', 2, '书画', '清', '绢帛', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.2rLHaOeDxn7OcbuoNdE59QHaEH?w=328&h=182&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 2, NULL),
('7006', '竹雕西园雅集笔筒', '清初竹刻，高15.7厘米。全景浮雕苏轼等文士雅集场景，松石间错落21人。刀法深峻，层次达七重，吴之璠"薄地阳文"代表作。', 2, '竹雕', '清', '毛竹', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.oDAxYKZu-Qz7tUMld4W6MQHaH0?w=199&h=210&c=7&r=0&o=7&cb=thvnextc1&dpr=1.5&pid=1.7&rm=3', 1, NULL);


-- ----------------------------
-- Table structure for sensor_data (传感器原始数据表)
-- ----------------------------

DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE sensor_data (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                             sensor_id VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
                             type VARCHAR(32) NOT NULL COMMENT '传感器类型 (temperature, humidity, light等)',
                             value DOUBLE NOT NULL COMMENT '传感器读数值',
                             unit VARCHAR(16) NULL DEFAULT NULL COMMENT '单位 (如: °C, %, lux)',
                             location_id INT NULL DEFAULT NULL COMMENT '位置ID',
                             relic_id INT NULL DEFAULT NULL COMMENT '关联的文物ID',
                             timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
                             is_abnormal TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否异常数据 (0: 正常, 1: 异常)',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (id,timestamp),
                             KEY idx_sensor_id (sensor_id),
                             KEY idx_timestamp (timestamp),
                             KEY idx_type (type),
                             KEY idx_location_id (location_id),
                             KEY idx_relic_id (relic_id),
                             KEY idx_is_abnormal (is_abnormal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器原始数据表'
    PARTITION BY RANGE COLUMNS(timestamp)
        (
        PARTITION p_before VALUES LESS THAN ('2025-06-01'),
        PARTITION p202506 VALUES LESS THAN ('2025-07-01'),
        PARTITION p202507 VALUES LESS THAN ('2025-08-01'),
        PARTITION p202508 VALUES LESS THAN ('2025-09-01'),
        PARTITION p202509 VALUES LESS THAN ('2025-10-01'),
        PARTITION p202510 VALUES LESS THAN ('2025-11-01'),
        PARTITION p202511 VALUES LESS THAN ('2025-12-01'),
        PARTITION p202512 VALUES LESS THAN ('2026-01-01'),
        PARTITION p_future VALUES LESS THAN (MAXVALUE)
        );


-- ----------------------------
-- Table structure for sensor_data_hourly (传感器数据小时聚合表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data_hourly`;
CREATE TABLE `sensor_data_hourly` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                      `sensor_id` VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
                                      `type` VARCHAR(32) NOT NULL COMMENT '传感器类型',
                                      `min_value` DOUBLE NOT NULL COMMENT '最小值',
                                      `max_value` DOUBLE NOT NULL COMMENT '最大值',
                                      `avg_value` DOUBLE NOT NULL COMMENT '平均值',
                                      `std_dev` DOUBLE NULL DEFAULT NULL COMMENT '标准差',
                                      `sample_count` INT NOT NULL COMMENT '样本数量',
                                      `unit` VARCHAR(16) NULL DEFAULT NULL COMMENT '单位',
                                      `location_id` INT NULL DEFAULT NULL COMMENT '位置ID',
                                      `relic_id` INT NULL DEFAULT NULL COMMENT '关联的文物ID',
                                      `hour_timestamp` TIMESTAMP NOT NULL COMMENT '小时时间戳 (整点)',
                                      `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_sensor_hour` (`sensor_id`, `type`, `hour_timestamp`),
                                      KEY `idx_hour_timestamp` (`hour_timestamp`),
                                      KEY `idx_sensor_type` (`sensor_id`, `type`),
                                      KEY `idx_location_id` (`location_id`),
                                      KEY `idx_relic_id` (`relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器数据小时聚合表';

-- ----------------------------
-- Table structure for sensor_data_daily (传感器数据天聚合表)
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data_daily`;
CREATE TABLE `sensor_data_daily` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                     `sensor_id` VARCHAR(64) NOT NULL COMMENT '传感器唯一标识',
                                     `type` VARCHAR(32) NOT NULL COMMENT '传感器类型',
                                     `min_value` DOUBLE NOT NULL COMMENT '最小值',
                                     `max_value` DOUBLE NOT NULL COMMENT '最大值',
                                     `avg_value` DOUBLE NOT NULL COMMENT '平均值',
                                     `std_dev` DOUBLE NULL DEFAULT NULL COMMENT '标准差',
                                     `sample_count` INT NOT NULL COMMENT '样本数量',
                                     `unit` VARCHAR(16) NULL DEFAULT NULL COMMENT '单位',
                                     `location_id` INT NULL DEFAULT NULL COMMENT '位置ID',
                                     `relic_id` INT NULL DEFAULT NULL COMMENT '关联的文物ID',
                                     `day_timestamp` DATE NOT NULL COMMENT '日期',
                                     `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_sensor_day` (`sensor_id`, `type`, `day_timestamp`),
                                     KEY `idx_day_timestamp` (`day_timestamp`),
                                     KEY `idx_sensor_type` (`sensor_id`, `type`),
                                     KEY `idx_location_id` (`location_id`),
                                     KEY `idx_relic_id` (`relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='传感器数据天聚合表';

-- 创建文物评论表
CREATE TABLE `relics_comment` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `relics_id` bigint NOT NULL COMMENT '文物ID',
                                  `username` varchar(50) NOT NULL COMMENT '用户名',
                                  `content` varchar(500) NOT NULL COMMENT '评论内容',
                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                  `update_time` datetime NOT NULL COMMENT '更新时间',
                                  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_relics_id` (`relics_id`),
                                  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物评论表';

-- 添加外键约束（可选，根据实际需求决定是否添加）
-- ALTER TABLE `relics_comment` ADD CONSTRAINT `fk_relics_comment_relics` FOREIGN KEY (`relics_id`) REFERENCES `relics` (`id`);
-- ALTER TABLE `relics_comment` ADD CONSTRAINT `fk_relics_comment_user` FOREIGN KEY (`username`) REFERENCES `user` (`username`);

-- 创建文物收藏表
CREATE TABLE IF NOT EXISTS `favorites` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                           `relics_id` BIGINT NOT NULL COMMENT '文物ID',
                                           `username` VARCHAR(32) NOT NULL COMMENT '用户名',
                                           `create_time` DATETIME NOT NULL COMMENT '创建时间',
                                           PRIMARY KEY (`id`),
                                           INDEX `idx_username` (`username`),
                                           INDEX `idx_relics_id` (`relics_id`),
                                           UNIQUE INDEX `uk_username_relics` (`username`, `relics_id`) COMMENT '确保用户对同一文物只能收藏一次',
                                           CONSTRAINT `fk_favorites_username` FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物收藏表';

-- 添加告警记录表
CREATE TABLE IF NOT EXISTS `alert_record` (
                                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `alert_id` varchar(64) NOT NULL COMMENT '告警ID',
                                              `sensor_id` varchar(64) NOT NULL COMMENT '传感器ID',
                                              `alert_type` varchar(32) NOT NULL COMMENT '告警类型',
                                              `severity` varchar(16) NOT NULL COMMENT '告警级别：INFO、WARNING、CRITICAL',
                                              `message` varchar(255) NOT NULL COMMENT '告警消息',
                                              `relics_id` bigint(20) DEFAULT NULL COMMENT '文物ID',
                                              `location_id` bigint(20) DEFAULT NULL COMMENT '位置ID',
                                              `current_value` double DEFAULT NULL COMMENT '当前读数',
                                              `threshold` double DEFAULT NULL COMMENT '阈值',
                                              `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '告警状态：ACTIVE、RESOLVED',
                                              `timestamp` datetime NOT NULL COMMENT '告警时间',
                                              `resolved_time` datetime DEFAULT NULL COMMENT '解决时间',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_alert_id` (`alert_id`),
                                              KEY `idx_sensor_id` (`sensor_id`),
                                              KEY `idx_alert_type` (`alert_type`),
                                              KEY `idx_status` (`status`),
                                              KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';