USE [contact_db];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1, 1) NOT NULL,
        first_name NVARCHAR(255) NOT NULL,
        last_name NVARCHAR(255) NULL,
        email NVARCHAR(255) NOT NULL,
        phone_number NVARCHAR(255) NULL,
        password_hash NVARCHAR(255) NOT NULL,
        created_at DATETIME2(6) NULL,
        CONSTRAINT pk_users PRIMARY KEY (id)
    );
END;
GO

IF COL_LENGTH(N'dbo.users', N'first_name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD first_name NVARCHAR(255) NULL;

    IF COL_LENGTH(N'dbo.users', N'full_name') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            UPDATE dbo.users
            SET first_name =
                CASE
                    WHEN CHARINDEX(N'' '', LTRIM(RTRIM(full_name))) > 0
                        THEN LEFT(LTRIM(RTRIM(full_name)), CHARINDEX(N'' '', LTRIM(RTRIM(full_name))) - 1)
                    ELSE LTRIM(RTRIM(full_name))
                END;';
    END;

    EXEC sp_executesql N'
        UPDATE dbo.users
        SET first_name = N''Unknown''
        WHERE first_name IS NULL OR LTRIM(RTRIM(first_name)) = N'''';';

    ALTER TABLE dbo.users ALTER COLUMN first_name NVARCHAR(255) NOT NULL;
END;
GO

IF COL_LENGTH(N'dbo.users', N'last_name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD last_name NVARCHAR(255) NULL;

    IF COL_LENGTH(N'dbo.users', N'full_name') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            UPDATE dbo.users
            SET last_name =
                NULLIF(
                    LTRIM(SUBSTRING(
                        LTRIM(RTRIM(full_name)),
                        CHARINDEX(N'' '', LTRIM(RTRIM(full_name)) + N'' '') + 1,
                        LEN(LTRIM(RTRIM(full_name))))),
                    N'''');';
    END;
END;
GO

IF COL_LENGTH(N'dbo.users', N'password_hash') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD password_hash NVARCHAR(255) NULL;

    IF COL_LENGTH(N'dbo.users', N'password') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'UPDATE dbo.users SET password_hash = password;';
    END;

    EXEC sp_executesql N'
        UPDATE dbo.users
        SET password_hash = N''PASSWORD_RESET_REQUIRED''
        WHERE password_hash IS NULL OR password_hash = N'''';';

    ALTER TABLE dbo.users ALTER COLUMN password_hash NVARCHAR(255) NOT NULL;
END;
GO

IF COL_LENGTH(N'dbo.users', N'full_name') IS NOT NULL
BEGIN
    ALTER TABLE dbo.users DROP COLUMN full_name;
END;
GO

IF COL_LENGTH(N'dbo.users', N'password') IS NOT NULL
BEGIN
    ALTER TABLE dbo.users DROP COLUMN password;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.users')
      AND name = N'ux_users_email'
)
BEGIN
    CREATE UNIQUE INDEX ux_users_email ON dbo.users (email);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.users')
      AND name = N'ux_users_phone_number'
)
BEGIN
    CREATE UNIQUE INDEX ux_users_phone_number
        ON dbo.users (phone_number)
        WHERE phone_number IS NOT NULL;
END;
GO

IF OBJECT_ID(N'dbo.contacts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.contacts (
        id BIGINT IDENTITY(1, 1) NOT NULL,
        user_id BIGINT NOT NULL,
        first_name NVARCHAR(255) NULL,
        last_name NVARCHAR(255) NULL,
        title NVARCHAR(255) NULL,
        company NVARCHAR(255) NULL,
        address NVARCHAR(255) NULL,
        notes NVARCHAR(255) NULL,
        created_at DATETIME2(6) NULL,
        updated_at DATETIME2(6) NULL,
        CONSTRAINT pk_contacts PRIMARY KEY (id),
        CONSTRAINT fk_contacts_users
            FOREIGN KEY (user_id) REFERENCES dbo.users (id)
            ON DELETE CASCADE
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.contacts')
      AND name = N'ix_contacts_user_id'
)
BEGIN
    CREATE INDEX ix_contacts_user_id ON dbo.contacts (user_id);
END;
GO

IF OBJECT_ID(N'dbo.contact_emails', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.contact_emails (
        id BIGINT IDENTITY(1, 1) NOT NULL,
        contact_id BIGINT NOT NULL,
        email NVARCHAR(255) NOT NULL,
        label NVARCHAR(255) NULL,
        is_primary BIT NULL,
        CONSTRAINT pk_contact_emails PRIMARY KEY (id),
        CONSTRAINT fk_contact_emails_contacts
            FOREIGN KEY (contact_id) REFERENCES dbo.contacts (id)
            ON DELETE CASCADE
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.contact_emails')
      AND name = N'ix_contact_emails_contact_id'
)
BEGIN
    CREATE INDEX ix_contact_emails_contact_id ON dbo.contact_emails (contact_id);
END;
GO

IF OBJECT_ID(N'dbo.contact_phones', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.contact_phones (
        id BIGINT IDENTITY(1, 1) NOT NULL,
        contact_id BIGINT NOT NULL,
        phone NVARCHAR(255) NOT NULL,
        label NVARCHAR(255) NULL,
        is_primary BIT NULL,
        CONSTRAINT pk_contact_phones PRIMARY KEY (id),
        CONSTRAINT fk_contact_phones_contacts
            FOREIGN KEY (contact_id) REFERENCES dbo.contacts (id)
            ON DELETE CASCADE
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.contact_phones')
      AND name = N'ix_contact_phones_contact_id'
)
BEGIN
    CREATE INDEX ix_contact_phones_contact_id ON dbo.contact_phones (contact_id);
END;
GO
