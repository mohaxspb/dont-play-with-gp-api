ALTER TABLE users
    DROP COLUMN IF EXISTS name_first,
    DROP COLUMN IF EXISTS name_second,
    DROP COLUMN IF EXISTS name_third;