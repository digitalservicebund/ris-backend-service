CREATE
OR REPLACE FUNCTION uuid_generate_v7 () RETURNS UUID AS $$
DECLARE
    unix_ts_ms BYTEA;
    uuid_bytes BYTEA;
BEGIN
    unix_ts_ms = substring(int8send(floor(extract(EPOCH FROM clock_timestamp()) * 1000)::BIGINT) FROM 3);

    -- use random v4 uuid as starting point (which has the same variant we need)
    uuid_bytes = uuid_send(gen_random_uuid());

    -- overlay timestamp
    uuid_bytes = overlay(uuid_bytes PLACING unix_ts_ms FROM 1 FOR 6);

    -- set version 7
    uuid_bytes = set_byte(uuid_bytes, 6, (b'0111' || get_byte(uuid_bytes, 6)::BIT(4))::BIT(8)::INT);

    RETURN encode(uuid_bytes, 'hex')::UUID;
END
$$ LANGUAGE plpgsql VOLATILE;
