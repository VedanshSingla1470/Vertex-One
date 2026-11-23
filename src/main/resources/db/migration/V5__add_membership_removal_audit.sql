ALTER TABLE app.memberships
    ADD COLUMN removed_at TIMESTAMPTZ,
    ADD COLUMN removed_by_user_id UUID;

ALTER TABLE app.memberships
    ADD CONSTRAINT fk_memberships_removed_by
        FOREIGN KEY (removed_by_user_id)
            REFERENCES app.users(id);