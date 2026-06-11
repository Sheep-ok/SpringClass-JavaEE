-- 添加 worse_time 列到 attendance_task 表
ALTER TABLE attendance_task
ADD COLUMN worse_time DATETIME NULL;

-- 更新已有记录的 worse_time（end_time + 10分钟）
UPDATE attendance_task
SET worse_time = DATEADD(MINUTE, 10, end_time)
WHERE worse_time IS NULL AND end_time IS NOT NULL;
