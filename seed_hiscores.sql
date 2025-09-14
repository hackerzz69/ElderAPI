USE elder_main;

-- 1. Create user in core_members if not exists
INSERT IGNORE INTO core_members (member_id, name, members_pass_hash, joined, last_visit)
VALUES (1, 'testplayer', 'FAKEHASH', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()); 

-- 2. Delete old hiscores for testplayer
DELETE FROM skill_hiscores WHERE username='testplayer';

-- 3. Insert all skills at level 99
INSERT INTO skill_hiscores (userid, username, mode, xp_mode, skill_id, skill_name, level, experience)
VALUES
(1, 'testplayer', 0, 0, 0,  'Attack',      99, 13034431), 
(1, 'testplayer', 0, 0, 1,  'Defence',     99, 13034431), 
(1, 'testplayer', 0, 0, 2,  'Strength',    99, 13034431), 
(1, 'testplayer', 0, 0, 3,  'Hitpoints',   99, 13034431), 
(1, 'testplayer', 0, 0, 4,  'Ranged',      99, 13034431), 
(1, 'testplayer', 0, 0, 5,  'Prayer',      99, 13034431), 
(1, 'testplayer', 0, 0, 6,  'Magic',       99, 13034431), 
(1, 'testplayer', 0, 0, 7,  'Cooking',     99, 13034431), 
(1, 'testplayer', 0, 0, 8,  'Woodcutting', 99, 13034431), 
(1, 'testplayer', 0, 0, 9,  'Fletching',   99, 13034431), 
(1, 'testplayer', 0, 0, 10, 'Fishing',     99, 13034431), 
(1, 'testplayer', 0, 0, 11, 'Firemaking',  99, 13034431), 
(1, 'testplayer', 0, 0, 12, 'Crafting',    99, 13034431), 
(1, 'testplayer', 0, 0, 13, 'Smithing',    99, 13034431), 
(1, 'testplayer', 0, 0, 14, 'Mining',      99, 13034431), 
(1, 'testplayer', 0, 0, 15, 'Herblore',    99, 13034431), 
(1, 'testplayer', 0, 0, 16, 'Agility',     99, 13034431), 
(1, 'testplayer', 0, 0, 17, 'Thieving',    99, 13034431), 
(1, 'testplayer', 0, 0, 18, 'Slayer',      99, 13034431), 
(1, 'testplayer', 0, 0, 19, 'Farming',     99, 13034431), 
(1, 'testplayer', 0, 0, 20, 'Runecraft',   99, 13034431), 
(1, 'testplayer', 0, 0, 21, 'Hunter',      99, 13034431), 
(1, 'testplayer', 0, 0, 22, 'Construction',99, 13034431), 
(1, 'testplayer', 0, 0, 25, 'Total',       2277, 299791431); -- sum of 23 skills 
