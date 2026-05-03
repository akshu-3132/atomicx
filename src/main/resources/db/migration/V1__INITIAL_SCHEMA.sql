create table account
(
    account_id uuid         not null
        primary key,
    email      varchar(255),
    first_name varchar(255) not null,
    user_name  varchar(255) not null
        constraint ukf6xpj7h12wr185bqhfi1hqlbr
            unique
);

create table transaction
(
    transaction_id  uuid                        not null
        primary key,
    amount          numeric(19, 4)              not null,
    created_at      timestamp(6) with time zone not null,
    receiver        uuid                        not null,
    sender          uuid                        not null,
    status          varchar(255)
        constraint transaction_status_check
            check ((status)::text = ANY
        ((ARRAY ['COMPLETED'::character varying, 'FAILED'::character varying, 'PENDING'::character varying])::text[])),
    idempotency_key varchar(64)
);

create table ledger_entry
(
    id               uuid                        not null
        primary key,
    account_id       uuid                        not null,
    amount           numeric(19, 4)              not null,
    created_at       timestamp(6) with time zone not null,
    transaction_type varchar(255)
        constraint ledger_entry_transaction_type_check
            check ((transaction_type)::text = ANY
        ((ARRAY ['CREDIT'::character varying, 'DEBIT'::character varying])::text[])),
    transaction_id   uuid                        not null
        constraint fkbui4ybykuwnkd4cacsnh58ogp
            references transaction
);

